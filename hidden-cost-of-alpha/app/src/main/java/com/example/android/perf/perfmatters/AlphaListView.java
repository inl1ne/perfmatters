package com.example.android.perf.perfmatters;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by ilewis on 11/11/14.
 */
public class AlphaListView extends ListView {
    private boolean mOverlappingRendering = true;
    private float mAlpha;

    public AlphaListView(Context context) {
        super(context);
    }

    public AlphaListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlphaListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setAlpha(float alpha) {
        mAlpha = alpha;
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).setAlpha(alpha);
        }
    }

    @Override
    public float getAlpha() {
        return mAlpha;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return mOverlappingRendering;
    }

    public void setOverlappingRendering(boolean mOverlappingRendering) {
        this.mOverlappingRendering = mOverlappingRendering;
        for (int i = 0; i < getChildCount(); ++i) {
            ((ItemView)getChildAt(i)).setOverlappingRendering(mOverlappingRendering);
        }
        invalidate();
    }
}
