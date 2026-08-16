package com.foodwheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

public class FoodWheelView extends View {
    public interface OnSpinFinish {
        void onSpinStart();
        void onSpinFinish(int sector);
    }

    private static final int SECTOR_COUNT = 16;
    private static final float SECTOR_ANGLE = 360f / SECTOR_COUNT;
    private static final int[] COLORS = {
        0xFFF7B733, 0xFFF36F4F, 0xFFFFD98E, 0xFFE8554B,
        0xFF59B2A6, 0xFFF4A259, 0xFF8AC6D1, 0xFFE2685A,
        0xFF7FB069, 0xFFF2C14E, 0xFF6A9C89, 0xFFEE7B59,
        0xFFFAD3A7, 0xFFE0634B, 0xFF9BC1BC, 0xFFF29E38
    };

    private final Paint sectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] dishIndexes;
    private float rotation = 0f;
    private OnSpinFinish listener;

    public FoodWheelView(Context context) {
        super(context);
        sectorPaint.setAntiAlias(true);
        ringPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        centerPaint.setAntiAlias(true);
        dotPaint.setAntiAlias(true);
    }

    public void setDishes(int[] indexes) {
        dishIndexes = indexes;
        invalidate();
    }

    public float getRotation() { return rotation; }
    public void setRotation(float r) { rotation = r; invalidate(); }
    public void setOnSpinFinish(OnSpinFinish l) { listener = l; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dishIndexes == null) return;
        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float R = Math.min(w, h) * 0.465f;

        canvas.save();
        canvas.rotate(rotation, cx, cy);
        RectF slice = new RectF(cx - R, cy - R, cx + R, cy + R);
        for (int i = 0; i < SECTOR_COUNT; i++) {
            float start = i * SECTOR_ANGLE - 90f;
            sectorPaint.setColor(COLORS[i % COLORS.length]);
            sectorPaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(slice, start, SECTOR_ANGLE + 0.4f, true, sectorPaint);
        }
        float textR = R * 0.65f;
        for (int i = 0; i < SECTOR_COUNT; i++) {
            if (dishIndexes[i] < 0 || dishIndexes[i] >= FoodData.NAMES.length) continue;
            float mid = i * SECTOR_ANGLE + SECTOR_ANGLE / 2f - 90f;
            RectF pathBounds = new RectF(cx - textR, cy - textR, cx + textR, cy + textR);
            int len = FoodData.NAMES[dishIndexes[i]].length();
            textPaint.setTextSize(Math.min(R * 0.20f, 48) * (len <= 2 ? 1.18f : len <= 3 ? 1.0f : 0.82f));
            textPaint.setColor(0xFF3A2417);
            Path p = new Path();
            p.addArc(pathBounds, mid, 0.02f);
            canvas.drawTextOnPath(FoodData.NAMES[dishIndexes[i]], p, 0, -10, textPaint);
        }
        for (int i = 0; i < 8; i++) {
            float a = i * 45f - 90f;
            float x = cx + (float)Math.cos(Math.toRadians(a)) * R * 0.86f;
            float y = cy + (float)Math.sin(Math.toRadians(a)) * R * 0.86f;
            dotPaint.setColor((i % 2 == 0) ? 0xFFFFE3B8 : 0xFFF7B733);
            canvas.drawCircle(x, y, R * 0.035f, dotPaint);
        }
        canvas.restore();

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(0xFF332018);
        ringPaint.setStrokeWidth(R * 0.055f);
        canvas.drawCircle(cx, cy, R, ringPaint);
        ringPaint.setColor(0xFFF2C14E);
        ringPaint.setStrokeWidth(R * 0.015f);
        canvas.drawCircle(cx, cy, R * 0.95f, ringPaint);

        centerPaint.setColor(0xFFFBF0DC);
        canvas.drawCircle(cx, cy, R * 0.14f, centerPaint);
        centerPaint.setColor(0xFFE0452F);
        canvas.drawCircle(cx, cy, R * 0.055f, centerPaint);
        centerPaint.setColor(0xFFFFD98E);
        canvas.drawCircle(cx, cy, R * 0.02f, centerPaint);
    }

    public void spinTo(int target, int turns, long duration) {
        float targetCenter = target * SECTOR_ANGLE + SECTOR_ANGLE / 2f - 90f;
        float start = rotation;
        float desiredTotal = turns * 360f + (360f - targetCenter - (start % 360f)) % 360f;
        final float endRot = start + desiredTotal;
        SpinAnimation anim = new SpinAnimation(this, start, endRot);
        anim.setDuration(duration);
        anim.setInterpolator(new DecelerateInterpolator(2.4f));
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) { if (listener != null) listener.onSpinStart(); }
            @Override public void onAnimationEnd(Animation a) { if (listener != null) listener.onSpinFinish(target); }
            @Override public void onAnimationRepeat(Animation a) {}
        });
        startAnimation(anim);
    }

    private static final class SpinAnimation extends Animation {
        private final FoodWheelView view;
        private final float start;
        private final float end;
        SpinAnimation(FoodWheelView v, float s, float e) { view = v; start = s; end = e; }
        @Override
        protected void applyTransformation(float time, android.view.animation.Transformation t) {
            view.rotation = start + (end - start) * time;
            view.invalidate();
        }
    }
}

