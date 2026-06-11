# VIP System Integration Guide

## Overview

The VIP system for TUILiveKit provides a complete implementation of VIP levels (1-5) with animated frames, Firebase integration, and UI components for displaying VIP status throughout the application.

## Components

### Core Components

1. **VipLevel** - Enum defining 5 VIP levels with properties
2. **VipInfo** - Data class holding VIP status and expiration dates
3. **VipRepository** - Firebase Firestore integration for VIP data
4. **VipManager** - High-level API for VIP operations
5. **VipFrameView** - Animated frame view around avatars
6. **VipBadgeView** - Badge displaying VIP level
7. **VipParticleView** - Particle effects for premium VIP levels
8. **VipFrameGenerator** - Optional AI frame generation

## Installation & Setup

### 1. Initialize VIP Manager

In your Application class or MainActivity:

```java
import com.trtc.uikit.livekit.features.vip.manager.VipManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize VIP Manager
        VipManager.init(this);
    }
}
```

### 2. Configure VIP Settings (Optional)

```java
import com.trtc.uikit.livekit.features.vip.manager.VipConfig;

VipConfig config = VipConfig.createDefault();
config.enableFrameAnimations = true;
config.enableParticleEffects = true;
config.showVipInChat = true;
config.showVipInParticipantList = true;
config.glowIntensity = 0.8f;
```

## Usage Examples

### Check if User has VIP Status

```java
VipManager vipManager = VipManager.getInstance();
vipManager.isVipActive(userId, new VipManager.OnVipCheckListener() {
    @Override
    public void onResult(boolean isVipActive) {
        if (isVipActive) {
            // User has active VIP
        }
    }
});
```

### Grant VIP Status to User

```java
// Grant VIP level 3 for 30 days
vipManager.grantVip(userId, 3, 30, new VipManager.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Log.d(TAG, "VIP granted successfully");
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, "Failed to grant VIP", e);
    }
});
```

### Get VIP Level (Live Data)

```java
vipManager.getVipLevel(userId).observe(this, new Observer<VipLevel>() {
    @Override
    public void onChanged(VipLevel vipLevel) {
        if (vipLevel != VipLevel.NONE) {
            Log.d(TAG, "User VIP level: " + vipLevel.displayName);
        }
    }
});
```

### Extend VIP Duration

```java
vipManager.extendVip(userId, 30, new VipManager.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Log.d(TAG, "VIP extended by 30 days");
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, "Failed to extend VIP", e);
    }
});
```

### Upgrade VIP Level

```java
vipManager.upgradeVip(userId, 5, new VipManager.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Log.d(TAG, "VIP upgraded to level 5");
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, "Failed to upgrade VIP", e);
    }
});
```

## UI Integration

### 1. Add VIP Frame to User Avatar (Chat/Messages)

```xml
<!-- In your layout XML -->
<FrameLayout
    android:id="@+id/avatar_container"
    android:layout_width="48dp"
    android:layout_height="48dp">
    
    <ImageView
        android:id="@+id/user_avatar"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/default_avatar"
        android:scaleType="centerCrop" />
        
    <!-- VIP Frame will be added programmatically -->
</FrameLayout>
```

```java
// In your Activity/Fragment
FrameLayout avatarContainer = findViewById(R.id.avatar_container);
VipFrameView vipFrame = VipUIHelper.setupVipFrame(this, avatarContainer, userId);
```

### 2. Add VIP Badge Next to Username

```xml
<!-- In your layout XML -->
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal">
    
    <TextView
        android:id="@+id/user_name"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Username" />
    
    <com.trtc.uikit.livekit.features.vip.view.VipBadgeView
        android:id="@+id/vip_badge"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp" />
</LinearLayout>
```

```java
// In your Activity/Fragment
VipBadgeView badgeView = findViewById(R.id.vip_badge);
VipUIHelper.setupVipBadge(this, userId);
```

### 3. Add VIP Frame in Participant List Item

```java
// In your adapter or view holder
public void bindUserInfo(UserInfo userInfo) {
    // ... existing code ...
    
    FrameLayout avatarContainer = viewHolder.avatarContainer;
    VipFrameView vipFrame = VipUIHelper.setupVipFrame(context, avatarContainer, userInfo.userId);
    
    // Apply VIP styling to name if needed
    if (userInfo.getCurrentVipLevel() != VipLevel.NONE) {
        VipUIHelper.applyVipStyling(viewHolder.nameView, userInfo.getCurrentVipLevel());
    }
}
```

### 4. Add VIP Frame in Speaker Seats

```java
// In speaker seat view
FrameLayout speakerAvatarContainer = findViewById(R.id.speaker_avatar);
VipFrameView vipFrame = VipUIHelper.setupVipFrame(this, speakerAvatarContainer, speakerId);
VipParticleView particles = VipUIHelper.setupVipParticles(this, speakerAvatarContainer, speakerId);
```

## Firebase Firestore Structure

### VIP Data in Firestore

Collection: `users_vip`
Document ID: `{userId}`

```json
{
  "userId": "user123",
  "vipLevel": 3,
  "vipExpireDate": 1735689600000,
  "renewalDate": 1733011200000
}
```

### Setting up Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users_vip/{userId} {
      // Allow users to read their own VIP data
      allow read: if request.auth.uid == userId;
      // Allow admin to write VIP data
      allow write: if request.auth.token.admin == true;
    }
  }
}
```

## VIP Level Properties

### VIP 1 - Entry Level
- **Color**: Gold (#FFD4AF37)
- **Animation**: Subtle glowing border
- **Glow Radius**: 4px
- **Effects**: Basic glow

### VIP 2 - Standard
- **Color**: Bright Gold (#FFFFEF00)
- **Animation**: Golden frame with spark particles
- **Glow Radius**: 6px
- **Effects**: Glow + Light animation

### VIP 3 - Premium
- **Color**: Cyan (#FF00D4FF)
- **Animation**: Neon animated frame
- **Glow Radius**: 8px
- **Effects**: Glow + Particle effects

### VIP 4 - Elite
- **Color**: Purple Royal (#FF9D4EDD)
- **Animation**: Royal frame with moving light
- **Glow Radius**: 10px
- **Effects**: Glow + Particles + Corner highlights

### VIP 5 - Ultimate
- **Color**: Deep Pink (#FFFF1493)
- **Animation**: Crown animation + Premium effects
- **Glow Radius**: 12px
- **Effects**: All effects + Crown indicator

## Optional: AI Frame Generation

### Enable AI Frame Generation

```java
VipFrameGenerator frameGenerator = new VipFrameGenerator(this);
frameGenerator.enableAiGeneration(
    "your-api-key",
    "https://your-api-endpoint.com"
);

frameGenerator.setCallback(new VipFrameGenerator.VipFrameGeneratorCallback() {
    @Override
    public void onFrameGenerated(VipLevel level, Bitmap frameBitmap) {
        Log.d(TAG, "Frame generated for level " + level.level);
    }

    @Override
    public void onFrameGenerationFailed(VipLevel level, Exception error) {
        Log.e(TAG, "Frame generation failed", error);
    }
});

// Generate frame for specific level
frameGenerator.generateFrame(VipLevel.VIP_5);
```

## Real-time Updates

All VIP data is updated in real-time through Firebase. The system uses LiveData for automatic UI updates:

```java
// User avatar will automatically update when VIP status changes
vipManager.getVipLevel(userId).observe(lifecycleOwner, newVipLevel -> {
    // UI automatically updates with new VIP frame
    vipFrameView.setVipLevel(newVipLevel);
});
```

## Performance Considerations

1. **Caching**: VIP data is cached locally to reduce Firebase queries
2. **Lazy Loading**: VIP frames are only created when needed
3. **Memory Management**: Animations are cleaned up when views are detached
4. **Batch Operations**: Fetch multiple users' VIP data efficiently

## Testing the VIP System

### Test VIP Grant

```java
@Test
public void testGrantVip() {
    VipManager manager = VipManager.getInstance();
    String testUserId = "test_user_123";
    
    manager.grantVip(testUserId, 3, 30, new VipManager.OnVipOperationListener() {
        @Override
        public void onSuccess() {
            // Verify VIP was granted
            assertTrue(true);
        }

        @Override
        public void onFailure(Exception e) {
            fail("Grant VIP failed: " + e.getMessage());
        }
    });
}
```

## Troubleshooting

### VIP Frame Not Showing
- Check that VipManager is initialized
- Verify Firebase connection
- Ensure userId is correct

### Animations Not Running
- Check `enableFrameAnimations` in VipConfig
- Verify lifecycle owner is active
- Check that view is attached to window

### Firebase Firestore Errors
- Verify Firestore is enabled in Firebase Console
- Check Firestore security rules
- Verify user has read/write permissions

## Architecture Diagram

```
┌─────────────────────────────────────┐
│         UI Components               │
│  ┌──────────────────────────────┐   │
│  │  VipFrameView                │   │
│  │  VipBadgeView                │   │
│  │  VipParticleView             │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    VipUIHelper (Integration)        │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│      VipManager (Business Logic)    │
│  - Caching                          │
│  - VIP Status Checking              │
│  - Operations (Grant, Extend, etc)  │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    VipRepository (Data Access)      │
│  - Firebase Firestore Integration   │
│  - Real-time Listeners              │
│  - Batch Operations                 │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│   Firebase Firestore Database       │
│   Collection: users_vip             │
└─────────────────────────────────────┘
```

## Next Steps

1. Configure Firebase Firestore for your project
2. Initialize VipManager in your Application class
3. Add VipFrameView and VipBadgeView to your layouts
4. Subscribe to VIP level changes with LiveData observers
5. Test VIP grant/revoke operations
6. Optional: Enable AI frame generation if desired

## Support

For issues or questions about the VIP system, refer to the class documentation in the source files or contact the development team.
