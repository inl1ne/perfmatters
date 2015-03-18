package com.github.perfmatters.hidden_cost_of_alpha;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ilewis on 11/17/14.
 */
public class ItemView extends FrameLayout {
    private boolean mOverlappingRendering = true;

    public ItemView(Context context) {
        super(context);
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public ItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
