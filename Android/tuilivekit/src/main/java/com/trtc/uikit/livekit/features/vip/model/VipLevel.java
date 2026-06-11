package com.trtc.uikit.livekit.features.vip.model;

import androidx.annotation.DrawableRes;

import com.trtc.uikit.livekit.R;

/**
 * VIP Level Enum with properties for each level
 */
public enum VipLevel {
    NONE(0, "Regular User", R.drawable.ic_vip_none),
    VIP_1(1, "VIP 1", R.drawable.ic_vip_1),
    VIP_2(2, "VIP 2", R.drawable.ic_vip_2),
    VIP_3(3, "VIP 3", R.drawable.ic_vip_3),
    VIP_4(4, "VIP 4", R.drawable.ic_vip_4),
    VIP_5(5, "VIP 5", R.drawable.ic_vip_5);

    public final int    level;
    public final String displayName;
    @DrawableRes
    public final int    badgeIconRes;

    VipLevel(int level, String displayName, @DrawableRes int badgeIconRes) {
        this.level = level;
        this.displayName = displayName;
        this.badgeIconRes = badgeIconRes;
    }

    /**
     * Get VipLevel from level number
     */
    public static VipLevel fromLevel(int level) {
        for (VipLevel vipLevel : values()) {
            if (vipLevel.level == level) {
                return vipLevel;
            }
        }
        return NONE;
    }

    /**
     * Check if user has valid VIP status
     */
    public boolean isVip() {
        return this != NONE;
    }

    /**
     * Get Lottie animation resource ID for this VIP level
     */
    public String getLottieAssetName() {
        switch (this) {
            case VIP_1:
                return "lottie_vip_1_glowing_border.json";
            case VIP_2:
                return "lottie_vip_2_golden_sparks.json";
            case VIP_3:
                return "lottie_vip_3_neon_frame.json";
            case VIP_4:
                return "lottie_vip_4_royal_light.json";
            case VIP_5:
                return "lottie_vip_5_crown_premium.json";
            default:
                return null;
        }
    }

    /**
     * Get frame color accent for this VIP level
     */
    public int getFrameColorAccent() {
        switch (this) {
            case VIP_1:
                return 0xFFD4AF37; // Gold
            case VIP_2:
                return 0xFFFFD700; // Bright Gold
            case VIP_3:
                return 0xFF00D4FF; // Cyan
            case VIP_4:
                return 0xFF9D4EDD; // Purple Royal
            case VIP_5:
                return 0xFFFF1493; // Deep Pink
            default:
                return 0xFF808080; // Gray
        }
    }

    /**
     * Get frame brightness multiplier
     */
    public float getFrameBrightness() {
        switch (this) {
            case VIP_1:
                return 0.8f;
            case VIP_2:
                return 1.0f;
            case VIP_3:
                return 1.2f;
            case VIP_4:
                return 1.4f;
            case VIP_5:
                return 1.6f;
            default:
                return 0.5f;
        }
    }

    /**
     * Check if this level has particle effects
     */
    public boolean hasParticleEffects() {
        return this.level >= 3;
    }

    /**
     * Check if this level has crown animation (VIP 5 only)
     */
    public boolean hasCrownAnimation() {
        return this == VIP_5;
    }

    /**
     * Get frame glow radius in pixels
     */
    public int getFrameGlowRadius() {
        switch (this) {
            case VIP_1:
                return 4;
            case VIP_2:
                return 6;
            case VIP_3:
                return 8;
            case VIP_4:
                return 10;
            case VIP_5:
                return 12;
            default:
                return 2;
        }
    }
}
