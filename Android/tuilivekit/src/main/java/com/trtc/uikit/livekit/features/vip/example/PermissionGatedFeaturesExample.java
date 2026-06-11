package com.trtc.uikit.livekit.features.vip.example;

import android.content.Context;
import android.widget.Toast;

import com.trtc.uikit.livekit.features.vip.model.VipPermission;
import com.trtc.uikit.livekit.features.vip.service.VIPService;

/**
 * PermissionGatedFeatures - Examples of how to gate features based on VIP permissions
 *
 * These are examples of how to implement permission checks at critical points
 * in the application where VIP status matters.
 *
 * IMPORTANT: All permission checks use VIPService, which always provides current state.
 */
public class PermissionGatedFeatures {
    private static final String TAG = "PermissionGatedFeatures";

    /**
     * Example: Check permission before entering a private room
     */
    public static boolean canUserEnterPrivateRoom(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        // Check permission dynamically
        boolean hasAccess = vipService.checkPermission(userId, VipPermission.ACCESS_PRIVATE_ROOMS);

        if (!hasAccess) {
            Toast.makeText(context, "VIP required to enter private rooms", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission before claiming a priority seat
     */
    public static boolean canUserClaimPrioritySeat(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        if (!vipService.isVipActive(userId)) {
            Toast.makeText(context, "VIP status required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!vipService.checkPermission(userId, VipPermission.CLAIM_PRIORITY_SEAT)) {
            Toast.makeText(context, "VIP 2+ required for priority seats", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission before claiming exclusive seat
     */
    public static boolean canUserClaimExclusiveSeat(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        if (!vipService.checkPermission(userId, VipPermission.CLAIM_VIP_SEAT)) {
            int requiredLevel = 3;
            Toast.makeText(context, "VIP " + requiredLevel + "+ required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission before sending special message
     */
    public static boolean canUserSendSpecialMessage(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        if (!vipService.checkPermission(userId, VipPermission.SEND_SPECIAL_MESSAGES)) {
            Toast.makeText(context, "VIP 1+ required for special messages", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission before sending large gift
     */
    public static boolean canUserSendLargeGift(String userId, int giftValue, Context context) {
        VIPService vipService = VIPService.getInstance();

        // Large gifts (over 1000 coins) require VIP 2+
        if (giftValue > 1000 && !vipService.checkPermission(userId, VipPermission.SEND_LARGE_GIFTS)) {
            Toast.makeText(context, "VIP 2+ required for expensive gifts", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission for 4K streaming
     */
    public static boolean canUserStream4K(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        if (!vipService.checkPermission(userId, VipPermission.STREAM_4K)) {
            Toast.makeText(context, "VIP 4+ required for 4K streaming", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check permission before creating VIP room
     */
    public static boolean canUserCreateVipRoom(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        if (!vipService.checkPermission(userId, VipPermission.CREATE_VIP_ROOM)) {
            Toast.makeText(context, "VIP 4+ required to create VIP rooms", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check multiple permissions at once
     */
    public static boolean canUserPerformPremiumActions(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        // Check all these permissions are available
        boolean canPerformAll = vipService.checkAllPermissions(
                userId,
                VipPermission.ACCESS_EXCLUSIVE_FEATURES,
                VipPermission.CUSTOM_BADGES,
                VipPermission.PROFILE_CUSTOMIZATION
        );

        if (!canPerformAll) {
            Toast.makeText(context, "VIP 2+ required for premium features", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Example: Check if user can speak without approval
     */
    public static boolean canUserSpeakWithoutApproval(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        return vipService.checkPermission(userId, VipPermission.SPEAKING_WITHOUT_APPROVAL);
    }

    /**
     * Example: Get user's available permissions
     */
    public static void displayUserPermissions(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        vipService.getVipState(userId).observeForever(state -> {
            if (state.status == com.trtc.uikit.livekit.features.vip.model.VipState.Status.ACTIVE) {
                StringBuilder permissions = new StringBuilder("Available VIP Permissions:\n");

                VipPermission[] allPermissions = VipPermission.values();
                for (VipPermission perm : allPermissions) {
                    if (vipService.checkPermission(userId, perm)) {
                        permissions.append("✓ ").append(perm.name()).append("\n");
                    }
                }

                Toast.makeText(context, permissions.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "User is not VIP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Example: Get days remaining and show expiration warning
     */
    public static void checkVipExpiration(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        vipService.getVipState(userId).observeForever(state -> {
            if (state.status == com.trtc.uikit.livekit.features.vip.model.VipState.Status.ACTIVE) {
                long daysRemaining = vipService.getDaysRemaining(userId);

                if (daysRemaining <= 7 && daysRemaining > 0) {
                    // VIP expiring soon
                    Toast.makeText(context, 
                            "Your VIP expires in " + daysRemaining + " days", 
                            Toast.LENGTH_LONG).show();
                } else if (daysRemaining <= 0) {
                    // VIP expired
                    Toast.makeText(context, "Your VIP has expired", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Example: Permission-aware UI update
     * Update UI elements based on user's current permissions
     */
    public static void updateUIBasedOnPermissions(String userId, 
                                                    android.widget.Button speakButton,
                                                    android.widget.Button giftButton,
                                                    android.widget.Button settingsButton,
                                                    Context context) {
        VIPService vipService = VIPService.getInstance();

        vipService.getVipState(userId).observeForever(state -> {
            if (state.status == com.trtc.uikit.livekit.features.vip.model.VipState.Status.ACTIVE) {
                // User is VIP - enable features
                if (vipService.checkPermission(userId, VipPermission.SPEAKING_WITHOUT_APPROVAL)) {
                    speakButton.setEnabled(true);
                    speakButton.setText("Speak (Priority)");
                }

                if (vipService.checkPermission(userId, VipPermission.SEND_LARGE_GIFTS)) {
                    giftButton.setEnabled(true);
                    giftButton.setText("Send Expensive Gift");
                }

                if (vipService.checkPermission(userId, VipPermission.PROFILE_CUSTOMIZATION)) {
                    settingsButton.setEnabled(true);
                    settingsButton.setText("Customize Profile (VIP)");
                }
            } else {
                // User is not VIP - disable premium features
                speakButton.setEnabled(false);
                speakButton.setText("Speak (Need VIP 2)");

                giftButton.setEnabled(true);
                giftButton.setText("Send Gift");

                settingsButton.setEnabled(false);
                settingsButton.setText("Profile Settings (VIP 2)");
            }
        });
    }

    /**
     * Example: Complete VIP state check at room entry
     */
    public static void validateUserBeforeRoomEntry(String userId, Context context) {
        VIPService vipService = VIPService.getInstance();

        vipService.getVipState(userId).observeForever(state -> {
            switch (state.status) {
                case ACTIVE:
                    // User is VIP - all systems go
                    context.startActivity(new android.content.Intent(context, android.app.Activity.class));
                    break;

                case EXPIRED:
                    // VIP was expired - user is now regular
                    Toast.makeText(context, "Your VIP has expired", Toast.LENGTH_SHORT).show();
                    break;

                case INACTIVE:
                    // User never had VIP
                    Toast.makeText(context, "This room requires VIP", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
