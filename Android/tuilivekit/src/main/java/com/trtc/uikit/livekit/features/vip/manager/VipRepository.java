package com.trtc.uikit.livekit.features.vip.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.trtc.uikit.livekit.features.vip.model.VipInfo;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VipRepository - Manages VIP data from Firebase Firestore
 * Handles read/write operations and real-time updates
 */
public class VipRepository {
    private static final String TAG = "VipRepository";
    private static final String VIP_COLLECTION = "users_vip"; // Collection name in Firestore
    private static final String VIP_FIELD_LEVEL = "vipLevel";
    private static final String VIP_FIELD_EXPIRE = "vipExpireDate";
    private static final String VIP_FIELD_RENEWAL = "renewalDate";

    private static VipRepository instance;
    private final FirebaseFirestore firestore;
    private final Map<String, MutableLiveData<VipInfo>> vipLiveDataCache;
    private final Map<String, ListenerRegistration> listenerRegistrations;
    private final MutableLiveData<Boolean> isInitialized;

    private VipRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.vipLiveDataCache = new ConcurrentHashMap<>();
        this.listenerRegistrations = new ConcurrentHashMap<>();
        this.isInitialized = new MutableLiveData<>(true);
    }

    public static synchronized VipRepository getInstance() {
        if (instance == null) {
            instance = new VipRepository();
        }
        return instance;
    }

    /**
     * Get VIP info for a specific user as LiveData
     * Automatically listens for real-time updates
     */
    public LiveData<VipInfo> getVipInfoLiveData(String userId) {
        if (!vipLiveDataCache.containsKey(userId)) {
            MutableLiveData<VipInfo> liveData = new MutableLiveData<>(new VipInfo(userId, 0, 0));
            vipLiveDataCache.put(userId, liveData);
            listenToVipUpdates(userId, liveData);
        }
        return vipLiveDataCache.get(userId);
    }

    /**
     * Listen for real-time VIP updates from Firestore
     */
    private void listenToVipUpdates(String userId, MutableLiveData<VipInfo> liveData) {
        // Remove existing listener if any
        if (listenerRegistrations.containsKey(userId)) {
            listenerRegistrations.get(userId).remove();
        }

        // Set up new listener
        ListenerRegistration registration = firestore
                .collection(VIP_COLLECTION)
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to VIP updates for " + userId, error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        VipInfo vipInfo = snapshot.toObject(VipInfo.class);
                        if (vipInfo != null) {
                            vipInfo.userId = userId;
                            liveData.setValue(vipInfo);
                            Log.d(TAG, "VIP info updated for " + userId + ": " + vipInfo);
                        }
                    } else {
                        // User has no VIP record
                        VipInfo defaultVipInfo = new VipInfo(userId, 0, 0);
                        liveData.setValue(defaultVipInfo);
                    }
                });

        listenerRegistrations.put(userId, registration);
    }

    /**
     * Fetch VIP info once (non-LiveData)
     */
    public void fetchVipInfo(String userId, OnVipFetchListener listener) {
        firestore
                .collection(VIP_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        VipInfo vipInfo = snapshot.toObject(VipInfo.class);
                        if (vipInfo != null) {
                            vipInfo.userId = userId;
                            listener.onSuccess(vipInfo);
                        } else {
                            listener.onSuccess(new VipInfo(userId, 0, 0));
                        }
                    } else {
                        listener.onSuccess(new VipInfo(userId, 0, 0));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching VIP info for " + userId, e);
                    listener.onFailure(e);
                });
    }

    /**
     * Update VIP level and expiration date
     */
    public void setVipStatus(String userId, int vipLevel, long expireDate, OnVipOperationListener listener) {
        if (vipLevel < 0 || vipLevel > 5) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("Invalid VIP level: " + vipLevel));
            }
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(VIP_FIELD_LEVEL, vipLevel);
        data.put(VIP_FIELD_EXPIRE, expireDate);
        data.put(VIP_FIELD_RENEWAL, calculateRenewalDate(expireDate));

        firestore
                .collection(VIP_COLLECTION)
                .document(userId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "VIP status updated for " + userId + ": level=" + vipLevel);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating VIP status for " + userId, e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Clear VIP status for a user
     */
    public void clearVipStatus(String userId, OnVipOperationListener listener) {
        setVipStatus(userId, 0, 0, listener);
    }

    /**
     * Extend VIP duration by days
     */
    public void extendVipByDays(String userId, int days, OnVipOperationListener listener) {
        fetchVipInfo(userId, new OnVipFetchListener() {
            @Override
            public void onSuccess(VipInfo vipInfo) {
                if (vipInfo.isActive()) {
                    long newExpireDate = vipInfo.vipExpireDate + (days * 24 * 60 * 60 * 1000L);
                    setVipStatus(userId, vipInfo.vipLevel, newExpireDate, listener);
                } else {
                    if (listener != null) {
                        listener.onFailure(new Exception("User does not have active VIP"));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Upgrade VIP level
     */
    public void upgradeVipLevel(String userId, int newLevel, OnVipOperationListener listener) {
        fetchVipInfo(userId, new OnVipFetchListener() {
            @Override
            public void onSuccess(VipInfo vipInfo) {
                if (newLevel > vipInfo.vipLevel) {
                    setVipStatus(userId, newLevel, vipInfo.vipExpireDate, listener);
                } else {
                    if (listener != null) {
                        listener.onFailure(new Exception("New level must be higher than current level"));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Get batch VIP info for multiple users
     */
    public void fetchBatchVipInfo(java.util.List<String> userIds, OnBatchVipFetchListener listener) {
        if (userIds == null || userIds.isEmpty()) {
            if (listener != null) {
                listener.onSuccess(new HashMap<>());
            }
            return;
        }

        Map<String, VipInfo> results = new HashMap<>();
        int[] completedCount = {0};

        for (String userId : userIds) {
            fetchVipInfo(userId, new OnVipFetchListener() {
                @Override
                public void onSuccess(VipInfo vipInfo) {
                    results.put(userId, vipInfo);
                    completedCount[0]++;
                    if (completedCount[0] == userIds.size() && listener != null) {
                        listener.onSuccess(results);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    results.put(userId, new VipInfo(userId, 0, 0));
                    completedCount[0]++;
                    if (completedCount[0] == userIds.size() && listener != null) {
                        listener.onSuccess(results);
                    }
                }
            });
        }
    }

    /**
     * Stop listening to VIP updates for a user
     */
    public void unsubscribeVipUpdates(String userId) {
        if (listenerRegistrations.containsKey(userId)) {
            listenerRegistrations.get(userId).remove();
            listenerRegistrations.remove(userId);
        }
        vipLiveDataCache.remove(userId);
    }

    /**
     * Clean up all listeners
     */
    public void cleanup() {
        for (ListenerRegistration registration : listenerRegistrations.values()) {
            registration.remove();
        }
        listenerRegistrations.clear();
        vipLiveDataCache.clear();
    }

    /**
     * Calculate renewal date (30 days before expiration)
     */
    private long calculateRenewalDate(long expireDate) {
        return expireDate - (30 * 24 * 60 * 60 * 1000L);
    }

    /**
     * Listener for single VIP fetch operations
     */
    public interface OnVipFetchListener {
        void onSuccess(VipInfo vipInfo);
        void onFailure(Exception e);
    }

    /**
     * Listener for VIP write operations
     */
    public interface OnVipOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Listener for batch VIP fetch operations
     */
    public interface OnBatchVipFetchListener {
        void onSuccess(Map<String, VipInfo> vipInfoMap);
        void onFailure(Exception e);
    }
}
