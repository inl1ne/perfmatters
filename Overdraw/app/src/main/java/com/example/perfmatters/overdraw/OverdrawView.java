package com.example.perfmatters.overdraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ilewis on 1/27/15.
 */
public class OverdrawView extends TextView {

    private int overdrawAmount = 1;
    private int overdrawMax = 100;

    private Paint paint = new Paint();

    public OverdrawView(Context context) {
        super(context);
    }

    public OverdrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverdrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw big rectangles
        Rect r = canvas.getClipBounds();
        for (int i = 0; i < overdrawAmount; ++i) {
            paint.setARGB(0x80, i%2 * 0xff, (1-i%2) * 0xff, i%3 * 0x80);
            canvas.drawRect(r, paint);
        }
        long pixels = r.width() * r.height() * overdrawAmount;

        // Draw small rectangles. This ensures that we issue the same number of draw calls
        // regardless of how much overdraw we've asked for.
        r.set(0,0,1,1);
        for (int i = overdrawAmount; i < overdrawMax; ++i) {
            paint.setARGB(0xFF, i%2 * 0xff, (1-i%2) * 0xff, i%3 * 0x80);
            canvas.drawRect(r, paint);
            pixels++;
        }
        if (!isInEditMode()) {
            invalidate();
        }
        setText(String.format("%,d pixels drawn this frame", pixels));
        super.onDraw(canvas);
    }

    public int getOverdrawAmount() {
        return overdrawAmount;
    }

    public void setOverdrawAmount(int OverdrawAmount) {
        this.overdrawAmount = OverdrawAmount;
    }

    public int getOverdrawMax() {
        return overdrawMax;
    }

    public void setOverdrawMax(int overdrawMax) {
        this.overdrawMax = overdrawMax;
    }



}
