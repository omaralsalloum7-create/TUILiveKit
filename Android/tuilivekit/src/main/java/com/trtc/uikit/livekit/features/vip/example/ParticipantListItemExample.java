package com.trtc.uikit.livekit.features.vip.example;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.trtc.uikit.livekit.features.audiencecontainer.state.UserState;
import com.trtc.uikit.livekit.features.vip.manager.VipManager;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;
import com.trtc.uikit.livekit.features.vip.view.VipBadgeView;
import com.trtc.uikit.livekit.features.vip.view.VipFrameView;
import com.trtc.uikit.livekit.features.vip.view.VipParticleView;
import com.trtc.uikit.livekit.features.vip.view.VipUIHelper;

/**
 * Example: ParticipantListItemViewHolder with VIP Integration
 *
 * This example shows how to integrate the VIP system into a participant list item.
 * You would use this pattern in your RecyclerView adapter.
 */
public class ParticipantListItemViewHolder {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;

    // UI Views
    private final ImageView avatarImageView;
    private final TextView userNameTextView;
    private final FrameLayout avatarContainer;
    private final LinearLayout nameContainer;

    // VIP Views
    private VipFrameView vipFrameView;
    private VipBadgeView vipBadgeView;
    private VipParticleView vipParticleView;

    /**
     * Constructor
     */
    public ParticipantListItemViewHolder(
            Context context,
            LifecycleOwner lifecycleOwner,
            ImageView avatarImageView,
            TextView userNameTextView,
            FrameLayout avatarContainer,
            LinearLayout nameContainer) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.avatarImageView = avatarImageView;
        this.userNameTextView = userNameTextView;
        this.avatarContainer = avatarContainer;
        this.nameContainer = nameContainer;
    }

    /**
     * Bind user information including VIP status
     */
    public void bindUserInfo(UserState.UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }

        // Bind basic user info
        bindBasicUserInfo(userInfo);

        // Bind VIP system
        bindVipInfo(userInfo);
    }

    /**
     * Bind basic user information
     */
    private void bindBasicUserInfo(UserState.UserInfo userInfo) {
        // Set user name
        userNameTextView.setText(userInfo.name.getValue());

        // Set avatar (use Glide/Picasso in real implementation)
        // Glide.with(context)
        //     .load(userInfo.avatarUrl.getValue())
        //     .into(avatarImageView);
    }

    /**
     * Bind VIP information to views
     */
    private void bindVipInfo(UserState.UserInfo userInfo) {
        String userId = userInfo.userId;

        // Add VIP frame around avatar
        setupVipFrame(userId);

        // Add VIP particles for premium levels
        setupVipParticles(userId);

        // Add VIP badge next to name
        setupVipBadge(userId);

        // Listen to VIP level changes
        listenToVipLevelChanges(userInfo);
    }

    /**
     * Setup VIP frame view around avatar
     */
    private void setupVipFrame(String userId) {
        if (vipFrameView == null) {
            vipFrameView = new VipFrameView(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            avatarContainer.addView(vipFrameView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(userId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipFrameView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Setup VIP particle effects
     */
    private void setupVipParticles(String userId) {
        VipManager.getInstance().getVipLevel(userId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null && vipLevel.hasParticleEffects()) {
                if (vipParticleView == null) {
                    vipParticleView = new VipParticleView(context);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                    avatarContainer.addView(vipParticleView, params);
                }
                vipParticleView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Setup VIP badge next to username
     */
    private void setupVipBadge(String userId) {
        if (vipBadgeView == null) {
            vipBadgeView = new VipBadgeView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginStart(8);
            nameContainer.addView(vipBadgeView, params);
        }

        // Subscribe to VIP level updates
        VipManager.getInstance().getVipLevel(userId).observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null) {
                vipBadgeView.setVipLevel(vipLevel);
            }
        });
    }

    /**
     * Listen to VIP level changes and update styling
     */
    private void listenToVipLevelChanges(UserState.UserInfo userInfo) {
        userInfo.vipLevel.observe(lifecycleOwner, vipLevel -> {
            if (vipLevel != null && vipLevel != VipLevel.NONE) {
                // Apply VIP styling to username
                VipUIHelper.applyVipStyling(userNameTextView, vipLevel);

                // Add visual indicator
                userNameTextView.setTextColor(vipLevel.getFrameColorAccent());
            } else {
                // Reset to default styling
                userNameTextView.setTextColor(0xFF000000); // Black
            }
        });
    }

    /**
     * Clean up views when holder is recycled
     */
    public void onRecycled() {
        // Stop animations
        if (vipFrameView != null) {
            avatarContainer.removeView(vipFrameView);
            vipFrameView = null;
        }

        if (vipParticleView != null) {
            avatarContainer.removeView(vipParticleView);
            vipParticleView = null;
        }

        if (vipBadgeView != null) {
            nameContainer.removeView(vipBadgeView);
            vipBadgeView = null;
        }
    }
}

/**
 * Example: Usage in RecyclerView Adapter
 *
 * Here's how you would use the view holder in a participant list adapter:
 */
/*
public class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListViewHolder> {
    private Context context;
    private LifecycleOwner lifecycleOwner;
    private List<UserState.UserInfo> userList;

    @Override
    public ParticipantListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(
                R.layout.item_participant_list, parent, false);

        ImageView avatar = itemView.findViewById(R.id.avatar);
        TextView name = itemView.findViewById(R.id.user_name);
        FrameLayout avatarContainer = itemView.findViewById(R.id.avatar_container);
        LinearLayout nameContainer = itemView.findViewById(R.id.name_container);

        return new ParticipantListItemViewHolder(
                context,
                lifecycleOwner,
                avatar,
                name,
                avatarContainer,
                nameContainer
        );
    }

    @Override
    public void onBindViewHolder(ParticipantListViewHolder holder, int position) {
        UserState.UserInfo userInfo = userList.get(position);
        holder.bindUserInfo(userInfo);
    }

    @Override
    public void onViewRecycled(ParticipantListViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onRecycled();
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }
}
*/

/**
 * Example: Layout XML for Participant List Item
 *
 * Here's the corresponding layout file:
 */
/*
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="12dp">

    <!-- Avatar Container with VIP Frame -->
    <FrameLayout
        android:id="@+id/avatar_container"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_marginEnd="12dp">

        <ImageView
            android:id="@+id/avatar"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:src="@drawable/default_avatar"
            android:background="@drawable/circle_background" />

    </FrameLayout>

    <!-- User Name with VIP Badge -->
    <LinearLayout
        android:id="@+id/name_container"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <TextView
            android:id="@+id/user_name"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="#000000"
            android:textStyle="bold" />

        <!-- VIP Badge will be added programmatically -->

    </LinearLayout>

    <!-- Optional: Online/Offline indicator -->
    <View
        android:id="@+id/online_indicator"
        android:layout_width="12dp"
        android:layout_height="12dp"
        android:background="@drawable/circle_green"
        android:layout_gravity="center_vertical"
        android:layout_marginStart="8dp" />

</LinearLayout>
*/
