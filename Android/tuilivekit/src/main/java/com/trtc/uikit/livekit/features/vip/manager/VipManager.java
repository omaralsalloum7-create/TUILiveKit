package com.trtc.uikit.livekit.features.vip.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.trtc.uikit.livekit.features.vip.model.VipInfo;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VipManager - High-level VIP system manager
 * Combines Firebase, local caching, and business logic
 */
public class VipManager {
    private static final String TAG = "VipManager";
    private static final String PREF_NAME = "vip_system_prefs";
    private static final String PREF_VIP_CACHE_PREFIX = "vip_cache_";

    private static VipManager instance;
    private final VipRepository repository;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final Map<String, MutableLiveData<VipLevel>> cachedVipLevels;

    private VipManager(Context context) {
        this.repository = VipRepository.getInstance();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.cachedVipLevels = new ConcurrentHashMap<>();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new VipManager(context);
        }
    }

    public static VipManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VipManager not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Get VIP level for a user as LiveData
     * Returns cached data immediately, updates when new data arrives
     */
    public LiveData<VipLevel> getVipLevel(String userId) {
        if (!cachedVipLevels.containsKey(userId)) {
            MutableLiveData<VipLevel> liveData = new MutableLiveData<>(VipLevel.NONE);

            // Set cached value if available
            VipLevel cachedLevel = getCachedVipLevel(userId);
            if (cachedLevel != VipLevel.NONE) {
                liveData.setValue(cachedLevel);
            }

            // Subscribe to repository updates
            repository.getVipInfoLiveData(userId).observeForever(vipInfo -> {
                if (vipInfo != null && vipInfo.isActive()) {
                    VipLevel level = vipInfo.getVipLevelEnum();
                    liveData.setValue(level);
                    cacheVipLevel(userId, level);
                } else {
                    liveData.setValue(VipLevel.NONE);
                    clearCachedVipLevel(userId);
                }
            });

            cachedVipLevels.put(userId, liveData);
        }
        return cachedVipLevels.get(userId);
    }

    /**
     * Get VIP info for a user as LiveData
     */
    public LiveData<VipInfo> getVipInfo(String userId) {
        return repository.getVipInfoLiveData(userId);
    }

    /**
     * Check if user has active VIP status
     */
    public void isVipActive(String userId, OnVipCheckListener listener) {
        repository.fetchVipInfo(userId, new VipRepository.OnVipFetchListener() {
            @Override
            public void onSuccess(VipInfo vipInfo) {
                listener.onResult(vipInfo.isActive());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking VIP status", e);
                listener.onResult(false);
            }
        });
    }

    /**
     * Grant VIP status to user
     */
    public void grantVip(String userId, int level, int daysValid, OnVipOperationListener listener) {
        if (level < 1 || level > 5) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("VIP level must be 1-5"));
            }
            return;
        }

        long expireDate = System.currentTimeMillis() + (daysValid * 24 * 60 * 60 * 1000L);

        repository.setVipStatus(userId, level, expireDate, new VipRepository.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                cacheVipLevel(userId, VipLevel.fromLevel(level));
                Log.d(TAG, "VIP granted to " + userId + ": level " + level + " for " + daysValid + " days");
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error granting VIP", e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Revoke VIP status
     */
    public void revokeVip(String userId, OnVipOperationListener listener) {
        repository.clearVipStatus(userId, new VipRepository.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                clearCachedVipLevel(userId);
                Log.d(TAG, "VIP revoked for " + userId);
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error revoking VIP", e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Extend VIP duration
     */
    public void extendVip(String userId, int additionalDays, OnVipOperationListener listener) {
        repository.extendVipByDays(userId, additionalDays, new VipRepository.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "VIP extended for " + userId + " by " + additionalDays + " days");
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error extending VIP", e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Upgrade VIP level
     */
    public void upgradeVip(String userId, int newLevel, OnVipOperationListener listener) {
        if (newLevel < 1 || newLevel > 5) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("VIP level must be 1-5"));
            }
            return;
        }

        repository.upgradeVipLevel(userId, newLevel, new VipRepository.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                cacheVipLevel(userId, VipLevel.fromLevel(newLevel));
                Log.d(TAG, "VIP upgraded for " + userId + " to level " + newLevel);
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error upgrading VIP", e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Get VIP info for multiple users
     */
    public void fetchBatchVipInfo(List<String> userIds, OnBatchVipCheckListener listener) {
        repository.fetchBatchVipInfo(userIds, new VipRepository.OnBatchVipFetchListener() {
            @Override
            public void onSuccess(Map<String, VipInfo> vipInfoMap) {
                Map<String, VipLevel> vipLevels = new HashMap<>();
                for (Map.Entry<String, VipInfo> entry : vipInfoMap.entrySet()) {
                    VipLevel level = entry.getValue().isActive() ?
                            entry.getValue().getVipLevelEnum() : VipLevel.NONE;
                    vipLevels.put(entry.getKey(), level);
                }
                if (listener != null) {
                    listener.onSuccess(vipLevels);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching batch VIP info", e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Cache VIP level locally
     */
    private void cacheVipLevel(String userId, VipLevel level) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_VIP_CACHE_PREFIX + userId, level.level);
        editor.apply();
    }

    /**
     * Get cached VIP level
     */
    private VipLevel getCachedVipLevel(String userId) {
        int level = sharedPreferences.getInt(PREF_VIP_CACHE_PREFIX + userId, 0);
        return VipLevel.fromLevel(level);
    }

    /**
     * Clear cached VIP level
     */
    private void clearCachedVipLevel(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_VIP_CACHE_PREFIX + userId);
        editor.apply();
    }

    /**
     * Clean up all resources
     */
    public void cleanup() {
        repository.cleanup();
        cachedVipLevels.clear();
    }

    /**
     * Listener for VIP check operations
     */
    public interface OnVipCheckListener {
        void onResult(boolean isVipActive);
    }

    /**
     * Listener for VIP write operations
     */
    public interface OnVipOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Listener for batch VIP checks
     */
    public interface OnBatchVipCheckListener {
        void onSuccess(Map<String, VipLevel> vipLevels);
        void onFailure(Exception e);
    }
}
