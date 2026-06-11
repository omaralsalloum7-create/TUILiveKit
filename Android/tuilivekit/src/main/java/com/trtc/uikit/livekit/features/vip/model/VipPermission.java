package com.trtc.uikit.livekit.features.vip.model;

/**
 * VipPermission - Defines all VIP-gated permissions
 *
 * These permissions are checked dynamically based on VIP level.
 */
public enum VipPermission {
    // Access permissions
    ACCESS_PRIVATE_ROOMS(1),          // Can access VIP-only rooms
    ACCESS_EXCLUSIVE_FEATURES(1),     // Can use exclusive VIP features

    // Seat/Role permissions
    CLAIM_PRIORITY_SEAT(2),           // Can claim priority speaking seats
    CLAIM_VIP_SEAT(3),                // Can claim VIP-exclusive seats
    SPEAKING_WITHOUT_APPROVAL(2),     // Can speak without moderator approval

    // Message permissions
    SEND_SPECIAL_MESSAGES(1),         // Can send special formatted messages
    USE_VIP_EMOJIS(2),                // Can use VIP-exclusive emoji packs
    SEND_LARGE_GIFTS(2),              // Can send expensive gifts

    // Stream permissions
    STREAM_WITH_CUSTOM_BITRATE(3),    // Can stream with custom bitrate
    STREAM_4K(4),                      // Can stream in 4K
    UNLIMITED_STREAM_DURATION(3),      // Stream without time limits

    // Leaderboard permissions
    BYPASS_LEADERBOARD_COOLDOWN(2),   // Bypass leaderboard update cooldown
    FEATURED_ON_LEADERBOARD(3),       // Get featured status on leaderboard

    // Other permissions
    CREATE_VIP_ROOM(4),               // Can create VIP-exclusive rooms
    CUSTOM_BADGES(3),                 // Can have custom badges
    PROFILE_CUSTOMIZATION(2),         // Can customize profile more
    PRIORITY_SUPPORT(2);              // Gets priority support

    private final int minimumVipLevel;

    VipPermission(int minimumVipLevel) {
        this.minimumVipLevel = minimumVipLevel;
    }

    public int getMinimumVipLevel() {
        return minimumVipLevel;
    }

    /**
     * Check if a VIP level has this permission
     */
    public boolean isSatisfiedBy(VipLevel level) {
        return level.level >= minimumVipLevel;
    }

    @Override
    public String toString() {
        return name() + " (requires VIP " + minimumVipLevel + "+)";
    }
}
