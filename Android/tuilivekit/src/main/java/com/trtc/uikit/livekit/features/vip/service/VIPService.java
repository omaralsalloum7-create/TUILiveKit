package com.trtc.uikit.livekit.features.vip.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trtc.uikit.livekit.features.vip.manager.VipManager;
import com.trtc.uikit.livekit.features.vip.model.VipInfo;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;
import com.trtc.uikit.livekit.features.vip.model.VipState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * VIPService - Centralized VIP System Service
 *
 * This is the single source of truth for all VIP-related operations.
 * It handles:
 * - Real-time VIP state validation
 * - Automatic lifecycle management
 * - Permission checking
 * - State synchronization across the app
 *
 * DESIGN PRINCIPLE:
 * - All VIP state comes from this service
 * - UI components depend on this service, not on local state
 * - State is calculated in real-time, not cached statically
 */
public class VIPService {
    private static final String TAG = "VIPService";
    private static final long VALIDATION_INTERVAL_SECONDS = 60; // Validate every minute

    private static VIPService instance;
    private final Context context;
    private final VipManager vipManager;
    private final VipStateManager stateManager;
    private final VipPermissionManager permissionManager;
    private final VipStateValidator stateValidator;

    // Real-time state tracking
    private final Map<String, MutableLiveData<VipState>> userVipStates;
    private final MutableLiveData<Map<String, VipState>> allVipStates;
    private final MutableLiveData<Boolean> isServiceReady;

    // Background validation scheduler
    private ScheduledExecutorService validationScheduler;
    private boolean isRunning = false;

    private VIPService(Context context) {
        this.context = context.getApplicationContext();
        this.vipManager = VipManager.getInstance();
        this.userVipStates = new ConcurrentHashMap<>();
        this.allVipStates = new MutableLiveData<>(new HashMap<>());
        this.isServiceReady = new MutableLiveData<>(false);

        // Initialize managers
        this.stateManager = new VipStateManager();
        this.permissionManager = new VipPermissionManager();
        this.stateValidator = new VipStateValidator();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new VIPService(context);
        }
    }

    public static VIPService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VIPService not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Start the VIP service
     * Must be called after initialization
     */
    public void start() {
        if (isRunning) {
            Log.w(TAG, "VIPService is already running");
            return;
        }

        Log.d(TAG, "Starting VIPService");
        isRunning = true;

        // Start background validation scheduler
        validationScheduler = Executors.newScheduledThreadPool(1);
        validationScheduler.scheduleAtFixedRate(
                this::performBackgroundValidation,
                VALIDATION_INTERVAL_SECONDS,
                VALIDATION_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        isServiceReady.setValue(true);
    }

    /**
     * Stop the VIP service
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        Log.d(TAG, "Stopping VIPService");
        isRunning = false;
        isServiceReady.setValue(false);

        if (validationScheduler != null) {
            validationScheduler.shutdownNow();
        }

        cleanup();
    }

    /**
     * Get VIP state for a user (real-time, always current)
     * This is the primary method for checking VIP status
     */
    public LiveData<VipState> getVipState(String userId) {
        if (!userVipStates.containsKey(userId)) {
            MutableLiveData<VipState> stateData = new MutableLiveData<>();

            // Fetch and validate VIP state
            refreshVipState(userId, state -> {
                stateData.setValue(state);
            });

            userVipStates.put(userId, stateData);
        }

        return userVipStates.get(userId);
    }

    /**
     * Refresh VIP state for a user (force re-validation)
     */
    public void refreshVipState(String userId, OnVipStateRefreshListener listener) {
        vipManager.getVipInfo(userId).observeForever(vipInfo -> {
            if (vipInfo == null) {
                if (listener != null) {
                    listener.onStateRefreshed(VipState.INACTIVE);
                }
                updateUserVipState(userId, VipState.INACTIVE);
                return;
            }

            // Validate and calculate state
            VipState calculatedState = calculateVipState(vipInfo);

            // If state changed (e.g., expired), update in service
            if (calculatedState.status == VipState.Status.EXPIRED && vipInfo.vipLevel > 0) {
                handleVipExpiration(userId, vipInfo);
            }

            updateUserVipState(userId, calculatedState);

            if (listener != null) {
                listener.onStateRefreshed(calculatedState);
            }
        });
    }

    /**
     * Check if user has active VIP (synchronous check)
     */
    public boolean isVipActive(String userId) {
        LiveData<VipState> stateLiveData = getVipState(userId);
        VipState state = stateLiveData.getValue();
        return state != null && state.status == VipState.Status.ACTIVE;
    }

    /**
     * Get VIP level for user (returns current level)
     */
    public VipLevel getVipLevel(String userId) {
        LiveData<VipState> stateLiveData = getVipState(userId);
        VipState state = stateLiveData.getValue();
        if (state != null && state.status == VipState.Status.ACTIVE) {
            return state.vipLevel;
        }
        return VipLevel.NONE;
    }

    /**
     * Check permission for a specific action
     */
    public boolean checkPermission(String userId, VipPermission permission) {
        VipLevel level = getVipLevel(userId);
        return permissionManager.hasPermission(level, permission);
    }

    /**
     * Check multiple permissions
     */
    public boolean checkAllPermissions(String userId, VipPermission... permissions) {
        for (VipPermission permission : permissions) {
            if (!checkPermission(userId, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check any permission
     */
    public boolean checkAnyPermission(String userId, VipPermission... permissions) {
        for (VipPermission permission : permissions) {
            if (checkPermission(userId, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get days remaining until VIP expires
     */
    public long getDaysRemaining(String userId) {
        LiveData<VipState> stateLiveData = getVipState(userId);
        VipState state = stateLiveData.getValue();
        if (state != null && state.status == VipState.Status.ACTIVE) {
            return state.daysRemaining;
        }
        return 0;
    }

    /**
     * Grant VIP to user
     */
    public void grantVip(String userId, int level, int daysValid, OnVipOperationListener listener) {
        vipManager.grantVip(userId, level, daysValid, new VipManager.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                // Refresh state immediately
                refreshVipState(userId, state -> {
                    Log.d(TAG, "VIP granted to " + userId + ": " + state);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                    // Notify all observers
                    notifyStateChange(userId);
                });
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
     * Revoke VIP from user
     */
    public void revokeVip(String userId, OnVipOperationListener listener) {
        vipManager.revokeVip(userId, new VipManager.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                updateUserVipState(userId, VipState.INACTIVE);
                Log.d(TAG, "VIP revoked for " + userId);
                if (listener != null) {
                    listener.onSuccess();
                }
                notifyStateChange(userId);
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
     * Is service ready?
     */
    public LiveData<Boolean> isServiceReady() {
        return isServiceReady;
    }

    /**
     * Calculate VIP state based on VipInfo
     * This is where real-time validation happens
     */
    private VipState calculateVipState(VipInfo vipInfo) {
        return stateValidator.validateAndCalculateState(vipInfo);
    }

    /**
     * Handle VIP expiration
     */
    private void handleVipExpiration(String userId, VipInfo vipInfo) {
        Log.d(TAG, "VIP expired for user " + userId);

        // Automatically reset to inactive
        updateUserVipState(userId, VipState.EXPIRED);

        // Optional: Auto-reset in Firebase
        vipManager.revokeVip(userId, new VipManager.OnVipOperationListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Expired VIP reset in Firebase for " + userId);
                notifyStateChange(userId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Error resetting expired VIP", e);
            }
        });
    }

    /**
     * Update user VIP state internally
     */
    private void updateUserVipState(String userId, VipState state) {
        MutableLiveData<VipState> userState = userVipStates.get(userId);
        if (userState != null) {
            userState.setValue(state);
        } else {
            MutableLiveData<VipState> newState = new MutableLiveData<>(state);
            userVipStates.put(userId, newState);
        }

        // Update global state map
        Map<String, VipState> currentStates = allVipStates.getValue();
        if (currentStates != null) {
            currentStates.put(userId, state);
            allVipStates.setValue(currentStates);
        }
    }

    /**
     * Notify all listeners of state change
     */
    private void notifyStateChange(String userId) {
        refreshVipState(userId, null);
    }

    /**
     * Perform background validation of all tracked users
     */
    private void performBackgroundValidation() {
        if (!isRunning) {
            return;
        }

        Log.d(TAG, "Performing background VIP validation for " + userVipStates.size() + " users");

        for (String userId : userVipStates.keySet()) {
            refreshVipState(userId, null);
        }
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        userVipStates.clear();
        if (allVipStates.getValue() != null) {
            allVipStates.getValue().clear();
        }
    }

    /**
     * Get all tracked VIP states
     */
    public LiveData<Map<String, VipState>> getAllVipStates() {
        return allVipStates;
    }

    /**
     * Listener for VIP state refresh
     */
    public interface OnVipStateRefreshListener {
        void onStateRefreshed(VipState state);
    }

    /**
     * Listener for VIP operations
     */
    public interface OnVipOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
