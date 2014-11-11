package com.example.android.perf.perfmatters;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by ilewis on 11/11/14.
 */
public class AlphaListView extends ListView {
    private boolean mOverlappingRendering = true;

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
    public boolean hasOverlappingRendering() {
        return mOverlappingRendering;
    }

    public void setOverlappingRendering(boolean mOverlappingRendering) {
        this.mOverlappingRendering = mOverlappingRendering;
        invalidate();
    }
}
