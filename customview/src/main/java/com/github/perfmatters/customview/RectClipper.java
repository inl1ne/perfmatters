package com.github.perfmatters.customview;

import android.graphics.Rect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ilewis on 3/2/15.
 */
public class RectClipper<T> {

    public interface ClipRectVisitor<T> {
        public void visit(Rect clippedBounds, T item);
    }

    private class ClipItem<T> {
        final Rect bounds = new Rect();
        int z;
        int clipResults = 0;
        T tag;

        public ClipItem clone() {
            ClipItem result = mClipItemPool.acquire();
            result.bounds.set(bounds);
            result.z = z;
            result.clipResults = clipResults;
            result.tag = tag;
            return result;
        }
    }

    class TestVisitor implements SpatialPartition.ItemVisitor<ClipItem> {
        public Rect r;
        public int z;
        public boolean topLeft; // false implies bottom-right

        @Override
        public void visit(ClipItem item) {
            item.clipResults &= ~(1 << ALREADY_CLIPPED);
            if (topLeft) {
                item.clipResults |= Boolean.compare(item.bounds.left > r.right, false) << FAILED_LEFT;
                item.clipResults |= Boolean.compare(item.bounds.right < r.left, false) << FAILED_RIGHT;
                //item.clipResults |= Boolean.compare(item.z > z, false) << FAILED_Z;
            } else {
                item.clipResults |= Boolean.compare(item.bounds.top > r.bottom, false) << FAILED_TOP;
                item.clipResults |= Boolean.compare(item.bounds.bottom < r.top, false) << FAILED_BOTTOM;
            }
        }
    }
    private TestVisitor mClipTestItemVisitor = new TestVisitor();

    private class DoClipVisitor implements SpatialPartition.ItemVisitor<ClipItem<T>> {
        private Rect scratchRect = new Rect();
        private ArrayList<ClipItem> results = new ArrayList<>();
        private ArrayList<ClipItem> scratchList = new ArrayList<>();

        public void doClip(ClipItem<T> toClip) {
            ClipItem<T> unobscuredRect = toClip.clone();
            results.add(unobscuredRect);

            mLeftTopMap.visitItems(toClip.bounds, this);
            mRightBottomMap.visitItems(toClip.bounds, this);
            mClippedList.addAll(results);

            results.clear();
        }

        @Override
        public void visit(ClipItem<T> item) {
            if (item.clipResults == 0) {
                for (ClipItem<T> currentResult : results) {
                    Rect r = currentResult.bounds;
                    scratchRect.set(item.bounds);
                    if (scratchRect.intersect(r)) {
                        if (scratchRect.top != r.top) {
                            ClipItem<T> rTop = currentResult.clone();
                            rTop.bounds.set(r.left, r.top, r.right, scratchRect.top);
                            scratchList.add(rTop);
                            r.top = scratchRect.top;
                        }

                        // Create new rectangles above and to the right
                        if (scratchRect.bottom != r.bottom) {
                            ClipItem<T> rBottom = currentResult.clone();
                            rBottom.bounds.set(r.left, scratchRect.bottom, r.right, r.bottom);
                            scratchList.add(rBottom);
                            r.bottom = scratchRect.bottom;
                        }

                        if (scratchRect.left != r.left) {
                            ClipItem<T> rLeft = currentResult.clone();
                            rLeft.bounds.set(r.left, r.top, scratchRect.left, r.bottom);
                            scratchList.add(rLeft);
                            r.left = scratchRect.left;
                        }

                        if (scratchRect.right != r.right) {
                            ClipItem<T> rRight = currentResult.clone();
                            rRight.bounds.set(scratchRect.right, r.top, r.right, r.bottom);
                            scratchList.add(rRight);
                            r.right = scratchRect.right;
                        }
                        mClipItemPool.release(currentResult);
                    } else {
                        scratchList.add(currentResult);
                    }
                }
                results.clear();
                results.addAll(scratchList);
                scratchList.clear();
            }
            item.clipResults = 1 << ALREADY_CLIPPED;
        }
    }
    DoClipVisitor mDoClipVisitor = new DoClipVisitor();

    private final ArrayList<ClipItem> mClippedList = new ArrayList<>();
    private final ArrayList<ClipItem> mUnclippedList = new ArrayList<>();

    private static final int FAILED_LEFT = 1;
    private static final int FAILED_TOP = 2;
    private static final int FAILED_RIGHT = 3;
    private static final int FAILED_BOTTOM = 4;
    private static final int FAILED_Z = 5;
    private static final int ALREADY_CLIPPED = 6;

    private UncheckedPool<ClipItem<T>> mClipItemPool;
    private SpatialPartition<ClipItem> mLeftTopMap;
    private SpatialPartition<ClipItem> mRightBottomMap;

    private int currentDepth;

    public RectClipper(Rect viewport, int nPartitions, int size) {
        mClipItemPool = new UncheckedPool<>(size);
        for (int i = 0; i < size; ++i) {
            mClipItemPool.release(new ClipItem<T>());
        }

        mLeftTopMap = new SpatialPartition<>(
                viewport.width(),
                viewport.height(),
                nPartitions,
                nPartitions);
        mRightBottomMap = new SpatialPartition<>(
                viewport.width(),
                viewport.height(),
                nPartitions,
                nPartitions);

    }

    public void addRectToBottom(Rect bounds, T item) {
        ClipItem unclipped = mClipItemPool.acquire();
        unclipped.z = currentDepth;
        unclipped.tag = item;
        unclipped.bounds.set(bounds);
        unclipped.clipResults = 0;


        mClipTestItemVisitor.r = bounds;
        mClipTestItemVisitor.z = currentDepth;
        mClipTestItemVisitor.topLeft = true;
        mLeftTopMap.visitItems(bounds, mClipTestItemVisitor);

        mClipTestItemVisitor.topLeft = false;
        mRightBottomMap.visitItems(bounds, mClipTestItemVisitor);

        mDoClipVisitor.doClip(unclipped);

        mUnclippedList.add(unclipped);
        mLeftTopMap.addItem(unclipped, bounds.left, bounds.top);
        mRightBottomMap.addItem(unclipped, bounds.right, bounds.bottom);
        ++currentDepth;
    }

    public void visitCliprects(ClipRectVisitor<T> visitor) {
        for(ClipItem<T> item : mClippedList) {
            visitor.visit(item.bounds, item.tag);
        }
    }

    public void clear() {
        for(ClipItem<T> item : mClippedList) {
            mClipItemPool.release(item);
        }
        mClippedList.clear();

        for(ClipItem<T> item : mUnclippedList) {
            mClipItemPool.release(item);
        }
        mUnclippedList.clear();

        mLeftTopMap.clear();
        mRightBottomMap.clear();
        currentDepth = 0;
    }
}
