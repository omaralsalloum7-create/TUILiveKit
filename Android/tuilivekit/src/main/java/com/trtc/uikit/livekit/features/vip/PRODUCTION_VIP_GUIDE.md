# Production-Level VIP System - Setup & Usage Guide

## 🎯 Overview

This is a **state-driven, fully dynamic, production-grade VIP system** that automatically handles:
- ✅ Real-time VIP state validation
- ✅ Automatic lifecycle management (activation → expiration)
- ✅ Permission-based feature gating
- ✅ Instant visual state updates across the app
- ✅ Continuous background validation

## 🚀 Quick Setup (5 Minutes)

### Step 1: Initialize VIPService in Application Class

```java
import com.trtc.uikit.livekit.features.vip.service.VIPService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize and start VIPService
        VIPService.init(this);
        VIPService.getInstance().start();
    }
    
    @Override
    public void onTerminate() {
        VIPService.getInstance().stop();
        super.onTerminate();
    }
}
```

### Step 2: Use State-Aware UI Components

```xml
<!-- In your layout -->
<FrameLayout
    android:id="@+id/avatar_container"
    android:layout_width="48dp"
    android:layout_height="48dp">
    
    <ImageView
        android:id="@+id/user_avatar"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
    <com.trtc.uikit.livekit.features.vip.view.StateAwareVipFrameView
        android:id="@+id/vip_frame"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>

<com.trtc.uikit.livekit.features.vip.view.StateAwareVipBadgeView
    android:id="@+id/vip_badge"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginStart="8dp" />
```

### Step 3: Setup Components in Activity/Fragment

```java
// In onCreate or onViewCreated
StateAwareVipFrameView vipFrame = findViewById(R.id.vip_frame);
StateAwareVipBadgeView vipBadge = findViewById(R.id.vip_badge);

vipFrame.setupWithUser(userId, this);
vipBadge.setupWithUser(userId, this);

// Optional: Listen to state changes
vipFrame.setStateChangeListener(new StateAwareVipFrameView.OnStateChangedListener() {
    @Override
    public void onVipActivated() {
        Log.d(TAG, "VIP activated!");
    }

    @Override
    public void onVipExpired() {
        Log.d(TAG, "VIP expired!");
    }

    @Override
    public void onVipStateChanged(VipState newState) {
        Log.d(TAG, "VIP state: " + newState.status);
    }
});
```

## 🎮 Permission-Based Feature Gating

### Check Permissions Before Actions

```java
VIPService vipService = VIPService.getInstance();

// Example 1: Check single permission
if (vipService.checkPermission(userId, VipPermission.ACCESS_PRIVATE_ROOMS)) {
    // Allow room access
} else {
    // Deny access
}

// Example 2: Check multiple permissions
if (vipService.checkAllPermissions(userId,
        VipPermission.SEND_LARGE_GIFTS,
        VipPermission.CUSTOM_BADGES)) {
    // Allow premium features
}

// Example 3: Get current VIP level
VipLevel level = vipService.getVipLevel(userId);
if (level.level >= 4) {
    // Allow VIP 4+ features
}

// Example 4: Check days remaining
long daysLeft = vipService.getDaysRemaining(userId);
if (daysLeft <= 7) {
    // Show expiration warning
}
```

## 🔄 Real-Time State Updates

The system automatically validates and updates state:

```java
// Subscribe to VIP state changes (real-time)
vipService.getVipState(userId).observe(this, new Observer<VipState>() {
    @Override
    public void onChanged(VipState state) {
        switch (state.status) {
            case ACTIVE:
                // VIP is active - show premium features
                showPremiumFeatures();
                break;
            
            case EXPIRED:
                // VIP expired - hide premium features
                hidePremiumFeatures();
                break;
            
            case INACTIVE:
                // No VIP - show regular features
                showRegularFeatures();
                break;
        }
    }
});
```

## 🎨 VIP UI Integration Points

### Participant List

```java
class ParticipantViewHolder extends RecyclerView.ViewHolder {
    void bind(UserInfo userInfo) {
        // Regular binding
        nameView.setText(userInfo.name);
        
        // Setup VIP frame (auto-updates from service)
        StateAwareVipFrameView vipFrame = new StateAwareVipFrameView(context);
        vipFrame.setupWithUser(userInfo.userId, lifecycleOwner);
        avatarContainer.addView(vipFrame);
    }
}
```

### Chat Messages

```java
void bindChatMessage(ChatMessage message) {
    // Message binding
    messageView.setText(message.text);
    senderNameView.setText(message.senderName);
    
    // Auto-updating VIP badge
    StateAwareVipBadgeView badge = new StateAwareVipBadgeView(context);
    badge.setupWithUser(message.senderId, lifecycleOwner);
    nameContainer.addView(badge);
}
```

### Speaker Seats

```java
void bindSpeakerSeat(SpeakerInfo speaker) {
    // Seat binding
    nameView.setText(speaker.name);
    
    // VIP frame for speaker
    StateAwareVipFrameView vipFrame = new StateAwareVipFrameView(context);
    vipFrame.setupWithUser(speaker.userId, lifecycleOwner);
    avatarContainer.addView(vipFrame);
}
```

## 📋 Available Permissions

VIP features can be gated with these permissions:

```
ACCESS_PRIVATE_ROOMS (VIP 1+)
ACCESS_EXCLUSIVE_FEATURES (VIP 1+)
CLAIM_PRIORITY_SEAT (VIP 2+)
CLAIM_VIP_SEAT (VIP 3+)
SPEAKING_WITHOUT_APPROVAL (VIP 2+)
SEND_SPECIAL_MESSAGES (VIP 1+)
USE_VIP_EMOJIS (VIP 2+)
SEND_LARGE_GIFTS (VIP 2+)
STREAM_WITH_CUSTOM_BITRATE (VIP 3+)
STREAM_4K (VIP 4+)
UNLIMITED_STREAM_DURATION (VIP 3+)
BYPASS_LEADERBOARD_COOLDOWN (VIP 2+)
FEATURED_ON_LEADERBOARD (VIP 3+)
CREATE_VIP_ROOM (VIP 4+)
CUSTOM_BADGES (VIP 3+)
PROFILE_CUSTOMIZATION (VIP 2+)
PRIORITY_SUPPORT (VIP 2+)
```

## 🔍 State Management Architecture

```
┌─────────────────────────────────────┐
│         UI Components               │
│ StateAwareVipFrameView              │
│ StateAwareVipBadgeView              │
└────────────────┬────────────────────┘
                 │ (observe)
                 ↓
┌─────────────────────────────────────┐
│      VIPService (Singleton)         │
│  - Centralized state management     │
│  - Real-time validation             │
│  - Permission checking              │
│  - Background scheduler             │
└────────────────┬────────────────────┘
                 │
        ┌────────┴────────┬───────────┬──────────┐
        ↓                 ↓           ↓          ↓
   VipState        VipStateValidator Permission  State
   Manager         Validator        Manager      Listener
        │                 │           │              │
        └────────┬────────┴───────────┴──────────┘
                 │
                 ↓
        ┌─────────────────────┐
        │  VipStateManager    │
        │  (Lifecycle)        │
        └─────────────────────┘
                 │
                 ↓
        ┌─────────────────────┐
        │  VipRepository      │
        │  (Firebase)         │
        └─────────────────────┘
```

## 🧪 Testing the VIP System

### Grant VIP to User

```java
VIPService vipService = VIPService.getInstance();
vipService.grantVip("user123", 3, 30, new VIPService.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Log.d(TAG, "VIP granted!");
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, "Error", e);
    }
});
```

### Verify VIP State

```java
vipService.getVipState("user123").observe(this, state -> {
    Log.d(TAG, "VIP State: " + state.status);
    Log.d(TAG, "VIP Level: " + state.vipLevel.displayName);
    Log.d(TAG, "Days Remaining: " + state.daysRemaining);
});
```

### Simulate Expiration

```java
// Grant VIP for 0 days (immediately expires)
vipService.grantVip("user123", 3, 0, listener);

// UI will automatically update to show expired state
```

## ⚙️ System Features

### 1. Automatic Lifecycle Management
- VIP automatically transitions from ACTIVE → EXPIRED when time passes
- System detects expiration and immediately removes permissions
- UI updates instantly when VIP expires

### 2. Real-Time Validation
- Validates VIP status every 60 seconds (configurable)
- Validates on app start
- Validates on user login
- Validates before any permission-gated action

### 3. State Synchronization
- All VIP changes sync across the entire app in real-time
- Firebase listeners keep data fresh
- Multiple components can safely access VIP state

### 4. Permission Control
- 17 permission types for different features
- Permissions are checked dynamically, not cached
- Easy to add new permissions

### 5. Background Validation
- Automatic validation scheduler runs continuously
- Handles expiration without user interaction
- Prevents stale state

## 🚨 Important Design Principles

### 1. No Hardcoded State
```java
// ✅ CORRECT - Always use service
if (vipService.isVipActive(userId)) { }

// ❌ WRONG - Don't cache or hardcode
if (user.vipLevel == 3) { }
```

### 2. Always Real-Time
```java
// ✅ CORRECT - Gets current state
vipService.checkPermission(userId, permission);

// ❌ WRONG - Uses potentially stale local state
if (cachedVipLevel >= 3) { }
```

### 3. Observe from Service
```java
// ✅ CORRECT - Observes from service
vipFrame.setupWithUser(userId, lifecycleOwner);

// ❌ WRONG - Local state that won't update
vipFrame.setVipLevel(cachedLevel);
```

## 🔧 Configuration

### Customize Validation Interval

In VIPService.java, change:
```java
private static final long VALIDATION_INTERVAL_SECONDS = 60;
```

### Add Custom Permissions

In VipPermission.java, add to enum:
```java
MY_CUSTOM_PERMISSION(2),  // Requires VIP 2+
```

## 📊 Debugging

### Enable Debug Logging

All components log to TAG:
- VIPService
- VipStateValidator
- VipStateManager
- VipPermissionManager

### View State Debug Info

```java
VipStateValidator validator = new VipStateValidator();
VipInfo vipInfo = getUserVipInfo();
Log.d(TAG, validator.getDebugInfo(vipInfo));
```

## 🎯 Best Practices

1. **Always initialize VIPService at app startup**
2. **Use StateAware* components for UI**
3. **Check permissions before critical actions**
4. **Never cache VIP state - always ask service**
5. **Subscribe to state changes for real-time updates**
6. **Validate VIP before granting access**
7. **Handle expiration gracefully**

## 🔗 File Locations

```
features/vip/
├── service/
│   ├── VIPService.java              (Main service)
│   ├── VipStateValidator.java       (Validation logic)
│   ├── VipStateManager.java         (Lifecycle management)
│   └── VipPermissionManager.java    (Permission checking)
├── view/
│   ├── StateAwareVipFrameView.java  (State-driven frame)
│   └── StateAwareVipBadgeView.java  (State-driven badge)
├── model/
│   ├── VipState.java               (State representation)
│   ├── VipPermission.java          (Permission enum)
│   └── ... (other models)
└── example/
    └── PermissionGatedFeaturesExample.java
```

## ✅ Verification Checklist

- [ ] VIPService initialized and started
- [ ] StateAware components used in UI
- [ ] Permission checks before gated actions
- [ ] VIP state observable throughout app
- [ ] Expiration handled automatically
- [ ] UI updates instantly on VIP changes
- [ ] No hardcoded VIP logic in UI
- [ ] Background validation running

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| VIP frame not appearing | Check if VIP is ACTIVE, not EXPIRED |
| State not updating | Ensure VIPService is started |
| Permissions always fail | Check VIP level and permission requirements |
| Stale state in UI | Use StateAware* components, don't cache |
| Performance issues | Adjust VALIDATION_INTERVAL_SECONDS |

---

**Status**: ✅ Production Ready
**Last Updated**: 2024
**Version**: 2.0 (State-Driven Architecture)
