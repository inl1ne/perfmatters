package com.github.perfmatters.customview;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Debug;
import android.support.v4.util.Pools;

import java.util.ArrayList;

/**
 * Created by ilewis on 2/6/15.
 */
public class CardClipper {

    public interface CardClipperVisitor {
        public void visit(int originalCardId, Rect originalRect, Rect clippedRect);
    }
    private class ClipRect {
        public int id;
        public Rect originalRect;
        public final ArrayList<Rect> rects = new ArrayList<>();
    }

    private Pools.Pool<Rect> mRectPool;
    private Pools.Pool<ClipRect> mClipRectPool;
    private ArrayList<ClipRect> mCliplist;

    private ArrayList<Rect> mScratchRects = new ArrayList<>();


    public CardClipper(int capacity) {
        // Quick and dirty heuristic: assume each unclipped rectangle produces
        // two clipped rectangles on average.
        int rectPoolSize = capacity * 8;
        mRectPool = new Pools.SimplePool<>(rectPoolSize);
        for (int i = 0; i < rectPoolSize; ++i) {
            mRectPool.release(new Rect());
        }

        // One ClipRect per input rectangle, because the ClipRect object
        // holds any extras.
        mClipRectPool = new Pools.SimplePool<>(capacity);
        for (int i = 0; i < capacity; ++i) {
            mClipRectPool.release(new ClipRect());
        }

        mCliplist = new ArrayList<>(capacity);
    }

    public void processClippedCards(CardClipperVisitor visitor) {
        for (ClipRect clipped : mCliplist) {
            for (Rect r : clipped.rects)
            visitor.visit(clipped.id, clipped.originalRect, r);
        }
    }

    public void addRect(int contentId, Rect rect) {
        addRect(contentId, rect.top, rect.left, rect.right, rect.bottom);
    }

    public void addRect(int contentId, int left, int top, int right, int bottom) {
        ClipRect clipper = mClipRectPool.acquire();
        assert (clipper.rects.isEmpty()); // make sure we got a clean rect
        clipper.id = contentId;

        clipper.originalRect = mRectPool.acquire();
        clipper.originalRect.set(left, top, right, bottom);
        Rect firstRect = mRectPool.acquire();
        firstRect.set(clipper.originalRect);
        clipper.rects.add(firstRect);
        for (ClipRect layer : mCliplist) {
            boolean isClipped = false;
            for (Rect clippee : layer.rects) {
                if (clippee.contains(left, top)) {
                    isClipped = true;

                    // Create new rectangles above and to the left
                    if (top != clippee.top) {
                        Rect rTop = mRectPool.acquire();
                        rTop.set(clippee.left, clippee.top, clippee.right, top);
                        mScratchRects.add(rTop);
                    }

                    if (left != clippee.left) {
                        Rect rLeft = mRectPool.acquire();
                        rLeft.set(clippee.left, top, left, clippee.bottom);
                        mScratchRects.add(rLeft);
                    }

                    // Shrink the clippee to eliminate the new rectangles from
                    // subsequent tests
                    clippee.left = left;
                    clippee.top = top;
                }
                if (clippee.contains(right, top)) {
                    isClipped = true;

                    // Create new rectangles above and to the right
                    if (top != clippee.top) {
                        Rect rTop = mRectPool.acquire();
                        rTop.set(clippee.left, clippee.top, clippee.right, top);
                        mScratchRects.add(rTop);
                    }

                    if (right != clippee.right) {
                        Rect rRight = mRectPool.acquire();
                        rRight.set(right, top, clippee.right, clippee.bottom);
                        mScratchRects.add(rRight);
                    }

                    clippee.right = right;
                    clippee.top = top;
                }
                if (clippee.contains(left, bottom)) {
                    isClipped = true;

                    // Create new rectangles below and to the left
                    if (bottom != clippee.bottom) {
                        Rect rBottom = mRectPool.acquire();
                        rBottom.set(clippee.left, bottom, clippee.right, clippee.bottom);
                        mScratchRects.add(rBottom);
                    }

                    if (left != clippee.left) {
                        Rect rLeft = mRectPool.acquire();
                        rLeft.set(clippee.left, clippee.top, left, bottom);
                        mScratchRects.add(rLeft);
                    }

                    clippee.left = left;
                    clippee.bottom = bottom;
                }
                if (clippee.contains(right, bottom)) {
                    isClipped = true;

                    // Create new rectangles below and to the right
                    if (bottom != clippee.bottom) {
                        Rect rBottom = mRectPool.acquire();
                        rBottom.set(clippee.left, bottom, clippee.right, clippee.bottom);
                        mScratchRects.add(rBottom);
                    }

                    if (right != clippee.right) {
                        Rect rRight = mRectPool.acquire();
                        rRight.set(right, clippee.top, clippee.right, bottom);
                        mScratchRects.add(rRight);
                    }

                    clippee.right = right;
                    clippee.bottom = bottom;
                }
                if (!isClipped) {
                    Rect scratchClippee = mRectPool.acquire();
                    scratchClippee.set(clippee);
                    mScratchRects.add(scratchClippee);
                }
            }
            for (Rect r : layer.rects) {
                mRectPool.release(r);
            }
            layer.rects.clear();
            layer.rects.addAll(mScratchRects);
            mScratchRects.clear();
        }
        // ...and the clipper joins the clippees.
        mCliplist.add(clipper);
    }

    public void clear() {
        for (ClipRect clipper : mCliplist) {
            for (Rect rect : clipper.rects) {
                mRectPool.release(rect);
            }
            clipper.rects.clear();
            mRectPool.release(clipper.originalRect);
            mClipRectPool.release(clipper);
        }
        mCliplist.clear();
    }
}
