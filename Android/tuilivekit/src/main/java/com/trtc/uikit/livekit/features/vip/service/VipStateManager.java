package com.trtc.uikit.livekit.features.vip.service;

import android.util.Log;

import com.trtc.uikit.livekit.features.vip.model.VipState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * VipStateManager - Manages VIP state transitions and lifecycle
 *
 * Handles:
 * - State transitions (ACTIVE → EXPIRED → INACTIVE, etc.)
 * - Lifecycle events (activation, expiration, revocation)
 * - State change notifications
 */
public class VipStateManager {
    private static final String TAG = "VipStateManager";

    private final List<OnVipStateChangeListener> stateChangeListeners;
    private final List<OnVipExpirationListener> expirationListeners;
    private final List<OnVipActivationListener> activationListeners;

    public VipStateManager() {
        this.stateChangeListeners = new CopyOnWriteArrayList<>();
        this.expirationListeners = new CopyOnWriteArrayList<>();
        this.activationListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Handle state transition
     */
    public void transitionState(String userId, VipState oldState, VipState newState) {
        if (oldState != null && oldState.equals(newState)) {
            // No state change
            return;
        }

        Log.d(TAG, "State transition for " + userId + 
                ": " + (oldState != null ? oldState.status : "null") + 
                " → " + newState.status);

        // Handle specific transitions
        if (oldState != null) {
            if (isStateChange(oldState, newState, VipState.Status.ACTIVE, VipState.Status.EXPIRED)) {
                handleExpiration(userId, newState);
            } else if (isStateChange(oldState, newState, VipState.Status.INACTIVE, VipState.Status.ACTIVE)) {
                handleActivation(userId, newState);
            } else if (isStateChange(oldState, newState, VipState.Status.ACTIVE, VipState.Status.INACTIVE)) {
                handleRevocation(userId, newState);
            }
        } else if (newState.status == VipState.Status.ACTIVE) {
            handleActivation(userId, newState);
        }

        // Notify all listeners
        notifyStateChange(userId, oldState, newState);
    }

    /**
     * Check if state changed from X to Y
     */
    private boolean isStateChange(VipState oldState, VipState newState, 
                                   VipState.Status expectedOld, VipState.Status expectedNew) {
        return oldState.status == expectedOld && newState.status == expectedNew;
    }

    /**
     * Handle VIP activation
     */
    private void handleActivation(String userId, VipState state) {
        Log.d(TAG, "VIP ACTIVATED for " + userId + " - Level: " + state.vipLevel.level);
        notifyActivation(userId, state);
    }

    /**
     * Handle VIP expiration
     */
    private void handleExpiration(String userId, VipState state) {
        Log.d(TAG, "VIP EXPIRED for " + userId);
        notifyExpiration(userId, state);
    }

    /**
     * Handle VIP revocation
     */
    private void handleRevocation(String userId, VipState state) {
        Log.d(TAG, "VIP REVOKED for " + userId);
        // Similar to expiration in terms of cleanup
        notifyExpiration(userId, state);
    }

    /**
     * Register listener for state changes
     */
    public void addStateChangeListener(OnVipStateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    /**
     * Register listener for expiration events
     */
    public void addExpirationListener(OnVipExpirationListener listener) {
        expirationListeners.add(listener);
    }

    /**
     * Register listener for activation events
     */
    public void addActivationListener(OnVipActivationListener listener) {
        activationListeners.add(listener);
    }

    /**
     * Remove listener
     */
    public void removeStateChangeListener(OnVipStateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }

    /**
     * Remove expiration listener
     */
    public void removeExpirationListener(OnVipExpirationListener listener) {
        expirationListeners.remove(listener);
    }

    /**
     * Remove activation listener
     */
    public void removeActivationListener(OnVipActivationListener listener) {
        activationListeners.remove(listener);
    }

    /**
     * Notify state change
     */
    private void notifyStateChange(String userId, VipState oldState, VipState newState) {
        for (OnVipStateChangeListener listener : stateChangeListeners) {
            try {
                listener.onStateChanged(userId, oldState, newState);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying state change listener", e);
            }
        }
    }

    /**
     * Notify expiration
     */
    private void notifyExpiration(String userId, VipState state) {
        for (OnVipExpirationListener listener : expirationListeners) {
            try {
                listener.onVipExpired(userId, state);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying expiration listener", e);
            }
        }
    }

    /**
     * Notify activation
     */
    private void notifyActivation(String userId, VipState state) {
        for (OnVipActivationListener listener : activationListeners) {
            try {
                listener.onVipActivated(userId, state);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying activation listener", e);
            }
        }
    }

    /**
     * Listener for VIP state changes
     */
    public interface OnVipStateChangeListener {
        void onStateChanged(String userId, VipState oldState, VipState newState);
    }

    /**
     * Listener for VIP expiration
     */
    public interface OnVipExpirationListener {
        void onVipExpired(String userId, VipState state);
    }

    /**
     * Listener for VIP activation
     */
    public interface OnVipActivationListener {
        void onVipActivated(String userId, VipState state);
    }
}
