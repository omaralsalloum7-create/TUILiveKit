package com.trtc.uikit.livekit.features.wallet.model;

/**
 * TransactionType - Enum for all possible coin transaction types
 */
public enum TransactionType {
    // Earning coins
    EARN_REWARD("Reward", true),
    EARN_DAILY_LOGIN("Daily Login Bonus", true),
    EARN_GIFT_RECEIVED("Gift Received", true),
    EARN_LIVE_BONUS("Live Broadcasting Bonus", true),
    EARN_TASK_COMPLETION("Task Completion", true),
    EARN_REFERRAL("Referral Bonus", true),
    EARN_ADMIN_GRANT("Admin Grant", true),
    EARN_VIP_BONUS("VIP Bonus", true),

    // Spending coins
    SPEND_GIFT("Gift Purchase", false),
    SPEND_PREMIUM_FEATURE("Premium Feature", false),
    SPEND_ROOM_ENTRY("Room Entry Fee", false),
    SPEND_STREAM_ENHANCEMENT("Stream Enhancement", false),
    SPEND_MARKETPLACE("Marketplace Purchase", false),
    SPEND_ADMIN_DEDUCT("Admin Deduction", false),

    // Special
    REFUND("Refund", true),
    TRANSFER_SEND("Transfer Out", false),
    TRANSFER_RECEIVE("Transfer In", true);

    private final String displayName;
    private final boolean isIncome; // true if adds coins, false if removes

    TransactionType(String displayName, boolean isIncome) {
        this.displayName = displayName;
        this.isIncome = isIncome;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public boolean isExpense() {
        return !isIncome;
    }
}
