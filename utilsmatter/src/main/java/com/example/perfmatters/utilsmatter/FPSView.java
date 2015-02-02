package com.example.perfmatters.utilsmatter;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ilewis on 1/27/15.
 */
public class FPSView extends TextView {

    private long lastDraw = SystemClock.uptimeMillis();

    private long[] window = new long[10];
    private int windowLen = 10;
    private int writePtr = 0;
    private long sum = 0;


    private void addValue(long value) {
        long old = window[writePtr];
        window[writePtr] = value;
        sum -= old;
        sum += value;

        writePtr++;
        if (writePtr >= windowLen) {
            writePtr = 0;
        }
    }

    public FPSView(Context context) {
        super(context);
    }

    public FPSView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FPSView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long current = SystemClock.uptimeMillis();
        addValue(current - lastDraw);
        lastDraw = current;

        float avg = (float)sum / (float)windowLen;

        // TODO: see if we can eliminate allocs here
        setText(String.format("%3.1ffps", 1000.f / avg));

        super.onDraw(canvas);

        if (!isInEditMode()) {
            invalidate();
        }
    }
}
