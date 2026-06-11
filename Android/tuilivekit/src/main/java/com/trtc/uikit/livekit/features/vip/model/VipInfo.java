package com.trtc.uikit.livekit.features.vip.model;

import com.google.gson.annotations.SerializedName;

/**
 * VipInfo - Represents VIP status of a user
 */
public class VipInfo {
    @SerializedName("userId")
    public String userId;

    @SerializedName("vipLevel")
    public int vipLevel = 0; // 0 = NONE, 1-5 = VIP levels

    @SerializedName("vipExpireDate")
    public long vipExpireDate = 0; // Unix timestamp in milliseconds

    @SerializedName("renewalDate")
    public long renewalDate = 0; // Unix timestamp when renewal is due

    public VipInfo() {
    }

    public VipInfo(String userId, int vipLevel, long vipExpireDate) {
        this.userId = userId;
        this.vipLevel = vipLevel;
        this.vipExpireDate = vipExpireDate;
    }

    /**
     * Check if VIP status is currently active
     */
    public boolean isActive() {
        if (vipLevel <= 0) {
            return false;
        }
        return System.currentTimeMillis() < vipExpireDate;
    }

    /**
     * Get days remaining until VIP expires
     */
    public long getDaysRemaining() {
        if (!isActive()) {
            return 0;
        }
        long diffMillis = vipExpireDate - System.currentTimeMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    /**
     * Get VipLevel enum
     */
    public VipLevel getVipLevelEnum() {
        return VipLevel.fromLevel(vipLevel);
    }

    /**
     * Set VIP level and expiration date
     */
    public void setVipStatus(int level, long expireDate) {
        this.vipLevel = level;
        this.vipExpireDate = expireDate;
    }

    /**
     * Clear VIP status
     */
    public void clearVipStatus() {
        this.vipLevel = 0;
        this.vipExpireDate = 0;
    }

    /**
     * Check if VIP is expiring soon (within 7 days)
     */
    public boolean isExpiringsoon() {
        return isActive() && getDaysRemaining() <= 7;
    }

    @Override
    public String toString() {
        return "VipInfo{" +
                "userId='" + userId + '\'' +
                ", vipLevel=" + vipLevel +
                ", vipExpireDate=" + vipExpireDate +
                ", isActive=" + isActive() +
                ", daysRemaining=" + getDaysRemaining() +
                '}';
    }
}
