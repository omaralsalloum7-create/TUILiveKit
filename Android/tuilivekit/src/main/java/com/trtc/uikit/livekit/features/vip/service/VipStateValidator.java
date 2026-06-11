package com.trtc.uikit.livekit.features.vip.service;

import android.util.Log;

import com.trtc.uikit.livekit.features.vip.model.VipInfo;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;
import com.trtc.uikit.livekit.features.vip.model.VipState;

/**
 * VipStateValidator - Validates and calculates VIP state in real-time
 *
 * This is the core logic that determines if a VIP is ACTIVE, EXPIRED, or INACTIVE
 * based on the current time and VipInfo data.
 *
 * IMPORTANT: This is called every time we need to know VIP status.
 * It NEVER returns cached or stale data.
 */
public class VipStateValidator {
    private static final String TAG = "VipStateValidator";

    /**
     * Validate and calculate VIP state based on VipInfo
     *
     * This is the ONLY way VIP state is determined.
     * It's stateless - same input always produces same output.
     *
     * Rules:
     * 1. If vipLevel <= 0 → INACTIVE
     * 2. If current time < vipExpireDate → ACTIVE
     * 3. If current time >= vipExpireDate → EXPIRED
     */
    public VipState validateAndCalculateState(VipInfo vipInfo) {
        if (vipInfo == null) {
            Log.w(TAG, "VipInfo is null, returning INACTIVE state");
            return VipState.createInactive();
        }

        // Rule 1: No VIP level
        if (vipInfo.vipLevel <= 0) {
            return VipState.createInactive();
        }

        long currentTime = System.currentTimeMillis();
        long expireDate = vipInfo.vipExpireDate;

        // Rule 2: VIP is still active
        if (currentTime < expireDate) {
            Log.d(TAG, "VIP is ACTIVE for " + vipInfo.userId + " (level " + vipInfo.vipLevel + ")");
            VipLevel level = VipLevel.fromLevel(vipInfo.vipLevel);
            return VipState.createActive(vipInfo.userId, level, expireDate);
        }

        // Rule 3: VIP has expired
        Log.d(TAG, "VIP has EXPIRED for " + vipInfo.userId + " (expired " + 
                ((currentTime - expireDate) / 1000) + " seconds ago)");
        return VipState.createExpired(vipInfo.userId, expireDate);
    }

    /**
     * Check if VIP is expiring soon (within N days)
     */
    public boolean isExpiringWithinDays(VipInfo vipInfo, int days) {
        if (vipInfo == null || vipInfo.vipLevel <= 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long expiringThreshold = currentTime + (days * 24 * 60 * 60 * 1000L);

        return vipInfo.vipExpireDate <= expiringThreshold && vipInfo.vipExpireDate > currentTime;
    }

    /**
     * Validate multiple users' VIP states
     */
    public VipState[] validateBatch(VipInfo[] vipInfos) {
        VipState[] states = new VipState[vipInfos.length];
        for (int i = 0; i < vipInfos.length; i++) {
            states[i] = validateAndCalculateState(vipInfos[i]);
        }
        return states;
    }

    /**
     * Get debug info about VIP state
     */
    public String getDebugInfo(VipInfo vipInfo) {
        if (vipInfo == null) {
            return "VipInfo is null";
        }

        VipState state = validateAndCalculateState(vipInfo);
        long currentTime = System.currentTimeMillis();
        long timeDiff = vipInfo.vipExpireDate - currentTime;
        long daysDiff = timeDiff / (1000 * 60 * 60 * 24);

        return String.format(
                "VIP State Debug:\n" +
                        "  userId: %s\n" +
                        "  vipLevel: %d\n" +
                        "  status: %s\n" +
                        "  currentTime: %d\n" +
                        "  expireDate: %d\n" +
                        "  timeDiff: %d ms (%.1f hours)\n" +
                        "  daysDiff: %d days\n" +
                        "  isActive: %b",
                vipInfo.userId,
                vipInfo.vipLevel,
                state.status,
                currentTime,
                vipInfo.vipExpireDate,
                timeDiff,
                timeDiff / (1000.0 * 60 * 60),
                daysDiff,
                state.status == VipState.Status.ACTIVE
        );
    }
}
