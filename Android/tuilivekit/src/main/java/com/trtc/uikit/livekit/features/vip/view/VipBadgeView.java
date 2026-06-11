package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.trtc.uikit.livekit.R;
import com.trtc.uikit.livekit.features.vip.model.VipLevel;

/**
 * VipBadgeView - Displays VIP badge next to username
 */
public class VipBadgeView extends View {
    private static final String TAG = "VipBadgeView";

    private VipLevel vipLevel = VipLevel.NONE;
    private Paint badgePaint;
    private Paint textPaint;
    private float cornerRadius = 4f;
    private float paddingH = 8f;
    private float paddingV = 4f;

    public VipBadgeView(Context context) {
        super(context);
        init();
    }

    public VipBadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VipBadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setupPaints();
    }

    private void setupPaints() {
        badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(12f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Set VIP level to display
     */
    public void setVipLevel(VipLevel vipLevel) {
        if (this.vipLevel == vipLevel) {
            return;
        }

        this.vipLevel = vipLevel;

        if (vipLevel != VipLevel.NONE) {
            updateBadgeAppearance();
            setVisibility(VISIBLE);
            invalidate();
        } else {
            setVisibility(GONE);
        }
    }

    /**
     * Update badge appearance based on VIP level
     */
    private void updateBadgeAppearance() {
        badgePaint.setColor(vipLevel.getFrameColorAccent());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (vipLevel == VipLevel.NONE) {
            setMeasuredDimension(0, 0);
            return;
        }

        String text = getBadgeText();
        float textWidth = textPaint.measureText(text);
        int width = (int) (textWidth + paddingH * 2 + 2);
        int height = (int) (16 + paddingV * 2 + 2);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (vipLevel == VipLevel.NONE) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // Draw badge background with gradient effect
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, badgePaint);

        // Draw badge text
        String text = getBadgeText();
        float textY = height / 2f + (textPaint.getTextSize() / 4);
        canvas.drawText(text, width / 2f, textY, textPaint);

        // Draw highlight line on top for premium levels
        if (vipLevel.level >= 3) {
            Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlightPaint.setColor(0xFFFFFFFF);
            highlightPaint.setAlpha(150);
            canvas.drawLine(2, 1, width - 2, 1, highlightPaint);
        }
    }

    /**
     * Get badge text for current VIP level
     */
    private String getBadgeText() {
        switch (vipLevel) {
            case VIP_1:
                return "VIP";
            case VIP_2:
                return "VIP 2";
            case VIP_3:
                return "VIP 3";
            case VIP_4:
                return "VIP 4";
            case VIP_5:
                return "👑 VIP 5";
            default:
                return "";
        }
    }

    /**
     * Get current VIP level
     */
    public VipLevel getVipLevel() {
        return vipLevel;
    }
}
