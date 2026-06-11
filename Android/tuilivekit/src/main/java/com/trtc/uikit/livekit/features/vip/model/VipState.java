package com.trtc.uikit.livekit.features.vip.model;

/**
 * VipState - Represents the current state of a user's VIP status
 *
 * This is calculated in real-time based on VipInfo and current timestamp.
 * It is never hardcoded - it's always derived from live data.
 */
public class VipState {
    public enum Status {
        ACTIVE,    // VIP is currently active
        EXPIRED,   // VIP has expired
        INACTIVE   // User never had VIP or it was revoked
    }

    public String userId;
    public Status status;
    public VipLevel vipLevel;
    public long vipExpireDate;
    public long daysRemaining;
    public long calculatedAt; // When this state was calculated

    /**
     * Create an INACTIVE VIP state
     */
    public static VipState createInactive() {
        VipState state = new VipState();
        state.status = Status.INACTIVE;
        state.vipLevel = VipLevel.NONE;
        state.daysRemaining = 0;
        state.calculatedAt = System.currentTimeMillis();
        return state;
    }

    /**
     * Create an ACTIVE VIP state
     */
    public static VipState createActive(String userId, VipLevel level, long expireDate) {
        VipState state = new VipState();
        state.userId = userId;
        state.status = Status.ACTIVE;
        state.vipLevel = level;
        state.vipExpireDate = expireDate;
        state.daysRemaining = calculateDaysRemaining(expireDate);
        state.calculatedAt = System.currentTimeMillis();
        return state;
    }

    /**
     * Create an EXPIRED VIP state
     */
    public static VipState createExpired(String userId, long expireDate) {
        VipState state = new VipState();
        state.userId = userId;
        state.status = Status.EXPIRED;
        state.vipLevel = VipLevel.NONE;
        state.vipExpireDate = expireDate;
        state.daysRemaining = 0;
        state.calculatedAt = System.currentTimeMillis();
        return state;
    }

    /**
     * Check if this state is fresh (calculated recently)
     */
    public boolean isFresh(long maxAgeMillis) {
        long age = System.currentTimeMillis() - calculatedAt;
        return age < maxAgeMillis;
    }

    /**
     * Recalculate days remaining (for display purposes)
     */
    public void recalculateDaysRemaining() {
        if (status == Status.ACTIVE) {
            this.daysRemaining = calculateDaysRemaining(vipExpireDate);
        } else {
            this.daysRemaining = 0;
        }
    }

    private static long calculateDaysRemaining(long expireDate) {
        long diffMillis = expireDate - System.currentTimeMillis();
        if (diffMillis <= 0) {
            return 0;
        }
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    @Override
    public String toString() {
        return "VipState{" +
                "userId='" + userId + '\'' +
                ", status=" + status +
                ", vipLevel=" + vipLevel +
                ", daysRemaining=" + daysRemaining +
                ", calculatedAt=" + calculatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VipState vipState = (VipState) o;

        if (status != vipState.status) return false;
        return vipLevel == vipState.vipLevel;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (vipLevel != null ? vipLevel.hashCode() : 0);
        return result;
    }
}
