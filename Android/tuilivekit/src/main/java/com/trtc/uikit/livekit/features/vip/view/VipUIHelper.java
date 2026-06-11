package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.FrameLayout;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.trtc.uikit.livekit.features.vip.manager.VipManager;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;

/**
 * VipUIHelper - Utility class for integrating VIP system into UI
 */
public class VipUIHelper {
    private static final String TAG = "VipUIHelper";

    /**
     * Setup VIP frame around an ImageView (avatar)
     * Should be called in a container that has the ImageView
     */
    public static VipFrameView setupVipFrame(Context context, FrameLayout container, String userId) {
        // Create VIP frame view
        VipFrameView vipFrame = new VipFrameView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(vipFrame, params);

        // Subscribe to VIP level updates
        if (container.getContext() instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) container.getContext();
            VipManager.getInstance().getVipLevel(userId).observe(owner, new Observer<VipLevel>() {
                @Override
                public void onChanged(VipLevel vipLevel) {
                    vipFrame.setVipLevel(vipLevel);
                }
            });
        }

        return vipFrame;
    }

    /**
     * Setup VIP particle effects around an avatar container
     */
    public static VipParticleView setupVipParticles(Context context, FrameLayout container, String userId) {
        VipParticleView particleView = new VipParticleView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(particleView, params);

        // Subscribe to VIP level updates
        if (container.getContext() instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) container.getContext();
            VipManager.getInstance().getVipLevel(userId).observe(owner, new Observer<VipLevel>() {
                @Override
                public void onChanged(VipLevel vipLevel) {
                    particleView.setVipLevel(vipLevel);
                }
            });
        }

        return particleView;
    }

    /**
     * Setup VIP badge view
     */
    public static VipBadgeView setupVipBadge(Context context, String userId) {
        VipBadgeView badgeView = new VipBadgeView(context);

        // Subscribe to VIP level updates
        if (context instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) context;
            VipManager.getInstance().getVipLevel(userId).observe(owner, new Observer<VipLevel>() {
                @Override
                public void onChanged(VipLevel vipLevel) {
                    badgeView.setVipLevel(vipLevel);
                }
            });
        }

        return badgeView;
    }

    /**
     * Get VIP frame glow color for a user
     */
    public static int getVipFrameColor(VipLevel vipLevel) {
        return vipLevel.getFrameColorAccent();
    }

    /**
     * Check if a user has active VIP status
     */
    public static void checkUserVipStatus(String userId, OnVipStatusCheckListener listener) {
        VipManager.getInstance().isVipActive(userId, new VipManager.OnVipCheckListener() {
            @Override
            public void onResult(boolean isVipActive) {
                listener.onVipStatusChecked(isVipActive);
            }
        });
    }

    /**
     * Apply VIP styling to a username TextView
     * (This is for future enhancement with styling)
     */
    public static void applyVipStyling(android.widget.TextView nameView, VipLevel vipLevel) {
        if (vipLevel != VipLevel.NONE) {
            // Example: Add special styling, fonts, or colors for VIP names
            nameView.setTextColor(vipLevel.getFrameColorAccent());
            // You can add more styling here as needed
        }
    }

    /**
     * Listener for VIP status checks
     */
    public interface OnVipStatusCheckListener {
        void onVipStatusChecked(boolean isVipActive);
    }
}
