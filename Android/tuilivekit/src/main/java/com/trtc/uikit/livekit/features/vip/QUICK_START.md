# VIP System - Quick Implementation Roadmap

## ⚡ 5-Step Implementation Guide

### Step 1: Setup (10 minutes)

**Location**: Application class

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

**Also update AndroidManifest.xml**:
```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### Step 2: Add to Participant List (15 minutes)

**Location**: res/layout/item_participant.xml

```xml
<!-- Avatar Container -->
<FrameLayout
    android:id="@+id/avatar_container"
    android:layout_width="48dp"
    android:layout_height="48dp">
    <ImageView
        android:id="@+id/user_avatar"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />
</FrameLayout>

<!-- Name Container -->
<LinearLayout
    android:id="@+id/name_container"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal">
    <TextView
        android:id="@+id/user_name"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>
```

**Location**: ParticipantAdapter.java

```java
@Override
public void onBindViewHolder(ParticipantViewHolder holder, int position) {
    UserInfo userInfo = userList.get(position);
    
    // Bind basic info
    holder.nameView.setText(userInfo.name);
    
    // Setup VIP frame
    VipUIHelper.setupVipFrame(context, holder.avatarContainer, userInfo.userId);
    
    // Setup VIP badge
    VipUIHelper.setupVipBadge(context, userInfo.userId);
}
```

### Step 3: Add to Chat Messages (15 minutes)

**Location**: res/layout/item_chat_message.xml

```xml
<FrameLayout
    android:id="@+id/avatar_container"
    android:layout_width="40dp"
    android:layout_height="40dp">
    <ImageView
        android:id="@+id/sender_avatar"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>

<LinearLayout
    android:id="@+id/sender_info"
    android:orientation="horizontal">
    <TextView
        android:id="@+id/sender_name"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>
```

**Location**: ChatAdapter.java

```java
@Override
public void onBindViewHolder(ChatViewHolder holder, int position) {
    ChatMessage message = chatMessages.get(position);
    
    holder.senderNameView.setText(message.senderName);
    holder.messageView.setText(message.message);
    
    // Setup VIP frame
    VipUIHelper.setupVipFrame(context, holder.avatarContainer, message.senderId);
    
    // Setup VIP badge
    VipUIHelper.setupVipBadge(context, message.senderId);
}
```

### Step 4: Add to Speaker Seats (15 minutes)

**Location**: res/layout/speaker_seat.xml

```xml
<FrameLayout
    android:id="@+id/avatar_container"
    android:layout_width="80dp"
    android:layout_height="80dp">
    <ImageView
        android:id="@+id/speaker_avatar"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

**Location**: LiveRoomActivity.java or SpeakerSeatView.java

```java
FrameLayout avatarContainer = findViewById(R.id.avatar_container);
VipUIHelper.setupVipFrame(this, avatarContainer, speakerId);
VipUIHelper.setupVipParticles(this, avatarContainer, speakerId);
```

### Step 5: Test VIP Operations (10 minutes)

**Test in your Activity or Settings**:

```java
VipManager vipManager = VipManager.getInstance();
String testUserId = "user123";

// Grant VIP
vipManager.grantVip(testUserId, 3, 30, new VipManager.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Toast.makeText(this, "VIP 3 granted for 30 days!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
});

// Check VIP status
vipManager.isVipActive(testUserId, isActive -> {
    Log.d(TAG, "User has active VIP: " + isActive);
});

// Extend VIP
vipManager.extendVip(testUserId, 30, new VipManager.OnVipOperationListener() {
    @Override
    public void onSuccess() {
        Toast.makeText(this, "VIP extended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Exception e) {
        Log.e(TAG, "Error extending VIP", e);
    }
});
```

## 📊 Implementation Checklist

- [ ] **Step 1: Setup**
  - [ ] Add VipManager.init() to Application
  - [ ] Update AndroidManifest with app name

- [ ] **Step 2: Participant List**
  - [ ] Update layout with avatar container
  - [ ] Update adapter with setupVipFrame()
  - [ ] Test VIP frame appears

- [ ] **Step 3: Chat Messages**
  - [ ] Update layout with avatar container
  - [ ] Update chat adapter with setupVipFrame()
  - [ ] Test VIP badge shows

- [ ] **Step 4: Speaker Seats**
  - [ ] Update speaker seat layout
  - [ ] Setup VIP frame and particles
  - [ ] Test animations

- [ ] **Step 5: Testing**
  - [ ] Grant VIP to test user
  - [ ] Verify frame appears
  - [ ] Verify badge shows
  - [ ] Test real-time updates
  - [ ] Test on low-end device

## 🔐 Firebase Setup

1. **Create Firestore Database**
   - Go to Firebase Console
   - Create Cloud Firestore

2. **Create Collection**
   - Collection name: `users_vip`
   - Don't create document yet

3. **Set Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users_vip/{userId} {
         allow read: if request.auth.uid == userId;
         allow write: if request.auth.token.admin == true;
       }
     }
   }
   ```

4. **Test Data (Firebase Console)**
   - Collection: `users_vip`
   - Document ID: `test_user_123`
   - Fields:
     ```
     vipLevel: 3
     vipExpireDate: 1735689600000
     renewalDate: 1733011200000
     ```

## 💡 API Quick Reference

```java
VipManager vm = VipManager.getInstance();

// Check VIP
vm.isVipActive(userId, listener);

// Grant (level 1-5, days valid)
vm.grantVip(userId, 3, 30, listener);

// Revoke
vm.revokeVip(userId, listener);

// Extend (add days)
vm.extendVip(userId, 30, listener);

// Upgrade
vm.upgradeVip(userId, 4, listener);

// Get VIP level (LiveData)
vm.getVipLevel(userId).observe(owner, vipLevel -> {
    // Update UI
});

// Setup UI
VipUIHelper.setupVipFrame(context, container, userId);
VipUIHelper.setupVipBadge(context, userId);
```

## 🎨 VIP Level Reference

| Level | Color | Effect | Particles |
|-------|-------|--------|-----------|
| 1 | Gold | Glow | No |
| 2 | Bright Gold | Glow | No |
| 3 | Cyan | Neon | Yes |
| 4 | Purple | Royal | Yes |
| 5 | Deep Pink | Crown | Yes |

## 📍 File Locations

```
/workspaces/TUILiveKit/Android/tuilivekit/src/main/java/
  com/trtc/uikit/livekit/features/vip/
    ├── model/         (2 files)
    ├── manager/       (4 files)
    ├── view/          (4 files)
    ├── example/       (2 files)
    ├── README.md
    ├── VIP_INTEGRATION_GUIDE.md
    └── FILE_LISTING.md

/workspaces/TUILiveKit/Android/tuilivekit/src/main/res-vip/
    ├── values/colors.xml
    └── drawable/ic_vip_*.xml

/workspaces/TUILiveKit/Android/tuilivekit/src/main/assets/
    └── animations/LOTTIE_SETUP.md
```

## 🚨 Common Issues & Solutions

### Issue: VIP frame not showing
**Solution**: 
- Check userId is correct
- Verify Firebase connection
- Ensure VipManager.init() called

### Issue: Badge not displaying
**Solution**:
- Verify addView() is called
- Check view container has space
- Verify VIP level is not NONE

### Issue: Firebase errors
**Solution**:
- Check Firestore is enabled
- Verify security rules
- Check user authentication

### Issue: Animations stuttering
**Solution**:
- Reduce glowIntensity in VipConfig
- Disable particles on low-end devices
- Reduce animation duration

## 📚 Learn More

- **Complete Guide**: `VIP_INTEGRATION_GUIDE.md`
- **Examples**: `example/ParticipantListItemExample.java`
- **Animations**: `assets/animations/LOTTIE_SETUP.md`
- **File List**: `FILE_LISTING.md`

## 🎯 Success Criteria

✅ VIP frames appear around user avatars in:
- Participant list
- Chat messages
- Speaker seats

✅ VIP badges display next to usernames

✅ Real-time updates work when VIP status changes

✅ Firebase integration works correctly

✅ No crashes or performance issues

✅ Works on low-end devices (API 21+)

---

**Estimated Total Implementation Time**: 1-2 hours
**Complexity**: Medium
**Status**: Production Ready ✅
