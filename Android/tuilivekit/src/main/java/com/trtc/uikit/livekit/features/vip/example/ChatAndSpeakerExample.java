package com.trtc.uikit.livekit.features.vip.example;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.trtc.uikit.livekit.features.vip.manager.VipManager;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;
import com.trtc.uikit.livekit.features.vip.view.VipBadgeView;
import com.trtc.uikit.livekit.features.vip.view.VipFrameView;

/**
 * Example: Chat Message ViewHolder with VIP Integration
 *
 * This example shows how to integrate the VIP system into a chat message view.
 * VIP frames will appear around user avatars in chat messages.
 */
public class ChatMessageViewHolder {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;

    // UI Views
    private final ImageView senderAvatarImageView;
    private final TextView senderNameTextView;
    private final TextView messageTextView;
    private final FrameLayout avatarContainer;
    private final LinearLayout senderInfoContainer;

    // VIP Views
    private VipFrameView vipFrameView;
    private VipBadgeView vipBadgeView;

    /**
     * Constructor
     */
    public ChatMessageViewHolder(
            Context context,
            LifecycleOwner lifecycleOwner,
            ImageView senderAvatarImageView,
            TextView senderNameTextView,
            TextView messageTextView,
            FrameLayout avatarContainer,
            LinearLayout senderInfoContainer) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.senderAvatarImageView = senderAvatarImageView;
        this.senderNameTextView = senderNameTextView;
        this.messageTextView = messageTextView;
        this.avatarContainer = avatarContainer;
        this.senderInfoContainer = senderInfoContainer;
    }

    /**
     * Bind chat message with VIP information
     */
    public void bindMessage(String senderId, String senderName, String message) {
        // Bind basic message info
        senderNameTextView.setText(senderName);
        messageTextView.setText(message);

        // Setup VIP frame around sender avatar
        setupVipFrameForChat(senderId);

        // Setup VIP badge next to sender name
        setupVipBadgeForChat(senderId);
    }

    /**
     * Setup VIP frame for chat avatar
     */
    private void setupVipFrameForChat(String senderId) {
        if (vipFrameView == null) {
            vipFrameView = new VipFrameView(context);

            // Set smaller size for chat (typically 32-40dp)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            avatarContainer.addView(vipFrameView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(senderId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipFrameView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Setup VIP badge next to sender name in chat
     */
    private void setupVipBadgeForChat(String senderId) {
        if (vipBadgeView == null) {
            vipBadgeView = new VipBadgeView(context);

            // Smaller size for chat display
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginStart(6);
            senderInfoContainer.addView(vipBadgeView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(senderId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipBadgeView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Clean up when holder is recycled
     */
    public void onRecycled() {
        if (vipFrameView != null) {
            avatarContainer.removeView(vipFrameView);
            vipFrameView = null;
        }

        if (vipBadgeView != null) {
            senderInfoContainer.removeView(vipBadgeView);
            vipBadgeView = null;
        }
    }
}

/**
 * Example: Speaker Seat ViewHolder with VIP Integration
 *
 * For speaker seat UI in live rooms.
 */
public class SpeakerSeatViewHolder {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;

    // UI Views
    private final ImageView speakerAvatarImageView;
    private final TextView speakerNameTextView;
    private final FrameLayout avatarContainer;
    private final LinearLayout seatContainer;

    // VIP Views
    private VipFrameView vipFrameView;
    private VipBadgeView vipBadgeView;

    /**
     * Constructor
     */
    public SpeakerSeatViewHolder(
            Context context,
            LifecycleOwner lifecycleOwner,
            ImageView speakerAvatarImageView,
            TextView speakerNameTextView,
            FrameLayout avatarContainer,
            LinearLayout seatContainer) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.speakerAvatarImageView = speakerAvatarImageView;
        this.speakerNameTextView = speakerNameTextView;
        this.avatarContainer = avatarContainer;
        this.seatContainer = seatContainer;
    }

    /**
     * Bind speaker information with VIP status
     */
    public void bindSpeaker(String speakerId, String speakerName) {
        speakerNameTextView.setText(speakerName);

        // Setup prominent VIP frame for speaker seat
        setupVipFrameForSpeaker(speakerId);

        // Setup VIP badge
        setupVipBadgeForSpeaker(speakerId);
    }

    /**
     * Setup VIP frame for speaker seat (larger and more prominent)
     */
    private void setupVipFrameForSpeaker(String speakerId) {
        if (vipFrameView == null) {
            vipFrameView = new VipFrameView(context);

            // Larger size for speaker seat (typically 80-120dp)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            avatarContainer.addView(vipFrameView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(speakerId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipFrameView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Setup VIP badge for speaker seat
     */
    private void setupVipBadgeForSpeaker(String speakerId) {
        if (vipBadgeView == null) {
            vipBadgeView = new VipBadgeView(context);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginStart(8);
            seatContainer.addView(vipBadgeView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(speakerId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipBadgeView.setVipLevel(vipLevel);

                // Optional: Highlight seat container for VIP speakers
                if (vipLevel != VipLevel.NONE) {
                    seatContainer.setBackgroundColor(vipLevel.getFrameColorAccent() & 0x20FFFFFF);
                } else {
                    seatContainer.setBackgroundColor(0x00000000);
                }
            }
        });
    }

    /**
     * Clean up when holder is recycled
     */
    public void onRecycled() {
        if (vipFrameView != null) {
            avatarContainer.removeView(vipFrameView);
            vipFrameView = null;
        }

        if (vipBadgeView != null) {
            seatContainer.removeView(vipBadgeView);
            vipBadgeView = null;
        }
    }
}
