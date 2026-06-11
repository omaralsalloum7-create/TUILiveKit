package com.trtc.uikit.livekit.features.vip.service;

import android.util.Log;

import com.trtc.uikit.livekit.features.vip.model.VipLevel;
import com.trtc.uikit.livekit.features.vip.model.VipPermission;

/**
 * VipPermissionManager - Dynamically checks VIP permissions
 *
 * This manager determines if a VIP level has a specific permission.
 * Permissions are always checked against CURRENT VIP state, not cached.
 */
public class VipPermissionManager {
    private static final String TAG = "VipPermissionManager";

    /**
     * Check if a VIP level has a specific permission
     * IMPORTANT: This is called every time, not cached
     */
    public boolean hasPermission(VipLevel level, VipPermission permission) {
        if (level == null || permission == null) {
            return false;
        }

        boolean hasPermission = permission.isSatisfiedBy(level);
        Log.d(TAG, "Permission check - Level: " + level.displayName + 
                ", Permission: " + permission.name() + 
                ", Result: " + hasPermission);

        return hasPermission;
    }

    /**
     * Check if a VIP level has ALL specified permissions
     */
    public boolean hasAllPermissions(VipLevel level, VipPermission... permissions) {
        for (VipPermission permission : permissions) {
            if (!hasPermission(level, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a VIP level has ANY of the specified permissions
     */
    public boolean hasAnyPermission(VipLevel level, VipPermission... permissions) {
        for (VipPermission permission : permissions) {
            if (hasPermission(level, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get required VIP level for a permission
     */
    public int getRequiredVipLevel(VipPermission permission) {
        return permission.getMinimumVipLevel();
    }

    /**
     * Get all permissions for a VIP level
     */
    public VipPermission[] getAllPermissionsForLevel(VipLevel level) {
        VipPermission[] allPermissions = VipPermission.values();
        java.util.List<VipPermission> grantedPermissions = new java.util.ArrayList<>();

        for (VipPermission permission : allPermissions) {
            if (hasPermission(level, permission)) {
                grantedPermissions.add(permission);
            }
        }

        return grantedPermissions.toArray(new VipPermission[0]);
    }

    /**
     * Get debug info about permissions
     */
    public String getDebugInfo(VipLevel level) {
        VipPermission[] permissions = getAllPermissionsForLevel(level);
        StringBuilder sb = new StringBuilder();
        sb.append("Permissions for ").append(level.displayName).append(":\n");

        for (VipPermission permission : permissions) {
            sb.append("  ✓ ").append(permission.name()).append("\n");
        }

        return sb.toString();
    }
}
