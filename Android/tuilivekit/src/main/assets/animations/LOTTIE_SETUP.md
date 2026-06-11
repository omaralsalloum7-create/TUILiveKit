# Lottie Animation Setup for VIP System

## Overview

This directory contains instructions for adding Lottie animations for each VIP level.

## Adding Lottie Animations

### Step 1: Add Lottie Dependency

Update `tuilivekit/build.gradle`:

```gradle
dependencies {
    implementation 'com.airbnb.android:lottie:5.2.0'
}
```

### Step 2: Place Animation Files

Save your Lottie animation JSON files in the `assets/animations/` directory:

- `lottie_vip_1_glowing_border.json` - Subtle glowing border animation
- `lottie_vip_2_golden_sparks.json` - Golden frame with spark effects
- `lottie_vip_3_neon_frame.json` - Neon animated frame
- `lottie_vip_4_royal_light.json` - Royal frame with light effects
- `lottie_vip_5_crown_premium.json` - Crown animation with premium effects

### Step 3: Load in VipFrameView

```java
// In VipFrameView or a custom LottieAnimationView
public void loadLottieAnimation(VipLevel vipLevel) {
    String assetName = vipLevel.getLottieAssetName();
    if (assetName != null) {
        Lottie AnimationView animView = new LottieAnimationView(getContext());
        animView.setAnimation(assetName);
        animView.loop(true);
        animView.playAnimation();
        addView(animView);
    }
}
```

## Recommended Animation Designs

### VIP Level 1 - Glowing Border
- Soft gold color
- Smooth pulsing glow effect
- Duration: 2-3 seconds per cycle
- Loop: Infinite
- File: `lottie_vip_1_glowing_border.json`

### VIP Level 2 - Golden Sparks
- Golden frame outline
- Spark particles flowing around the frame
- Duration: 2-2.5 seconds per cycle
- Speed: Medium
- File: `lottie_vip_2_golden_sparks.json`

### VIP Level 3 - Neon Frame
- Cyan and gold neon colors
- Animated glow/pulse effect
- Corner highlights
- Duration: 2 seconds per cycle
- File: `lottie_vip_3_neon_frame.json`

### VIP Level 4 - Royal Light
- Purple/gold color scheme
- Moving light effects (like light sweeping across)
- Corner decorations
- Duration: 2.5 seconds per cycle
- File: `lottie_vip_4_royal_light.json`

### VIP Level 5 - Crown Premium
- Deep pink/gold colors
- Crown animation at top
- Particle effects
- Shimmer/shine loop
- Multiple animated elements
- Duration: 3 seconds per cycle
- File: `lottie_vip_5_crown_premium.json`

## Creating Lottie Animations

You can create Lottie animations using:

1. **Lottie Figma Plugin** - Design in Figma and export to Lottie
2. **After Effects** - Export from After Effects using BodyMovin plugin
3. **LottieFiles Web Editor** - Create animations online at lottiefiles.com
4. **Custom JSON** - Write Lottie JSON manually

## Example: Using Lottie in Custom View

```java
import com.airbnb.lottie.LottieAnimationView;

public class VipLottieFrameView extends FrameLayout {
    private LottieAnimationView lottieView;
    
    public void setVipLevel(VipLevel vipLevel) {
        if (vipLevel == VipLevel.NONE) {
            if (lottieView != null) {
                removeView(lottieView);
            }
            return;
        }
        
        if (lottieView == null) {
            lottieView = new LottieAnimationView(getContext());
            lottieView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            addView(lottieView);
        }
        
        String animationName = vipLevel.getLottieAssetName();
        if (animationName != null) {
            lottieView.setAnimation(animationName);
            lottieView.loop(true);
            lottieView.playAnimation();
        }
    }
}
```

## Alternative: Drawable-Based Animations

If you don't want to use Lottie, you can create animations using:

1. **XML AnimationDrawable**
2. **Canvas Drawing with ObjectAnimator**
3. **Custom Paint Effects**

These are already implemented in `VipFrameView` and `VipParticleView`.

## Performance Tips

- Keep Lottie animations relatively simple to avoid performance issues
- Use the animation duration wisely (2-3 seconds is ideal)
- Test on low-end devices
- Monitor memory usage with multiple VIP users on screen
- Cache animation files in assets for faster loading

## Troubleshooting

### Animation Not Playing
- Verify JSON file is valid Lottie format
- Check file path is correct
- Ensure view is attached to window

### Animation Performance Issues
- Reduce animation complexity
- Use lower frame rate if possible
- Disable animations on low-end devices

### File Not Found
- Verify file is in `assets/animations/` directory
- Check filename matches exactly
- Rebuild project to ensure assets are included

## Resources

- Lottie Documentation: https://airbnb.design/lottie/
- LottieFiles: https://lottiefiles.com/
- After Effects BodyMovin Plugin: https://github.com/bodymovin/bodymovin
- Figma Lottie Plugin: https://www.figma.com/community/plugin/809860635037469556/Lottie
