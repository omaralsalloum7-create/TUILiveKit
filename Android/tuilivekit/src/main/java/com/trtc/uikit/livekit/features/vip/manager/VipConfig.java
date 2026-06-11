package com.trtc.uikit.livekit.features.vip.manager;

/**
 * VipConfig - Configuration settings for VIP system
 */
public class VipConfig {
    /**
     * Enable/disable VIP frame animations
     */
    public boolean enableFrameAnimations = true;

    /**
     * Enable/disable particle effects for VIP frames
     */
    public boolean enableParticleEffects = true;

    /**
     * Enable/disable AI frame generation
     */
    public boolean enableAiFrameGeneration = false;

    /**
     * AI API endpoint (if AI generation is enabled)
     */
    public String aiApiEndpoint = "";

    /**
     * AI API key (if AI generation is enabled)
     */
    public String aiApiKey = "";

    /**
     * Cache VIP data locally
     */
    public boolean enableLocalCaching = true;

    /**
     * VIP frame glow intensity (0.0 - 1.0)
     */
    public float glowIntensity = 0.7f;

    /**
     * VIP badge display position in participant list
     */
    public BadgePosition badgePosition = BadgePosition.NEXT_TO_NAME;

    /**
     * Whether to show VIP frame in chat messages
     */
    public boolean showVipInChat = true;

    /**
     * Whether to show VIP frame in participant list
     */
    public boolean showVipInParticipantList = true;

    /**
     * Whether to show VIP frame in speaker seats
     */
    public boolean showVipInSpeakerSeats = true;

    public enum BadgePosition {
        NEXT_TO_NAME,
        ABOVE_AVATAR,
        BELOW_AVATAR,
        FLOATING_CORNER
    }

    /**
     * Create default VIP configuration
     */
    public static VipConfig createDefault() {
        return new VipConfig();
    }

    /**
     * Create minimal VIP configuration (no effects)
     */
    public static VipConfig createMinimal() {
        VipConfig config = new VipConfig();
        config.enableFrameAnimations = false;
        config.enableParticleEffects = false;
        config.glowIntensity = 0.3f;
        return config;
    }

    /**
     * Create premium VIP configuration (all effects enabled)
     */
    public static VipConfig createPremium() {
        VipConfig config = new VipConfig();
        config.enableFrameAnimations = true;
        config.enableParticleEffects = true;
        config.glowIntensity = 1.0f;
        return config;
    }

    @Override
    public String toString() {
        return "VipConfig{" +
                "enableFrameAnimations=" + enableFrameAnimations +
                ", enableParticleEffects=" + enableParticleEffects +
                ", enableAiFrameGeneration=" + enableAiFrameGeneration +
                ", glowIntensity=" + glowIntensity +
                ", badgePosition=" + badgePosition +
                '}';
    }
}
