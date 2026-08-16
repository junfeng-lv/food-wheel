package com.foodwheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class NeedleView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public NeedleView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float r = Math.min(w, h) * 0.44f;

        paint.setColor(0xFF332018);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, r * 0.20f, paint);
        paint.setColor(0xFFF2C14E);
        canvas.drawCircle(cx, cy, r * 0.13f, paint);

        Path p = new Path();
        p.moveTo(cx - r * 0.78f, cy);
        p.lineTo(cx + r * 0.28f, cy - r * 0.30f);
        p.lineTo(cx + r * 0.28f, cy + r * 0.30f);
        p.close();
        paint.setColor(0xFFE0452F);
        canvas.drawPath(p, paint);

        Paint tip = new Paint(Paint.ANTI_ALIAS_FLAG);
        tip.setColor(0xFFFFF3D0);
        tip.setStyle(Paint.Style.STROKE);
        tip.setStrokeWidth(r * 0.05f);
        Path outline = new Path();
        outline.moveTo(cx - r * 0.78f, cy);
        outline.lineTo(cx + r * 0.28f, cy - r * 0.30f);
        outline.lineTo(cx + r * 0.28f, cy + r * 0.30f);
        outline.close();
        canvas.drawPath(outline, tip);
    }
}
