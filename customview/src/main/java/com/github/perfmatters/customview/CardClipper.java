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
        mRectPool = new UncheckedPool<>(rectPoolSize);
        for (int i = 0; i < rectPoolSize; ++i) {
            mRectPool.release(new Rect());
        }

        // One ClipRect per input rectangle, because the ClipRect object
        // holds any extras.
        mClipRectPool = new UncheckedPool<>(capacity);
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
            clipLayer(clipper, layer);
        }
        // ...and the clipper joins the clippees.
        mCliplist.add(clipper);
    }

    private void clipLayer(ClipRect clipperLayer, ClipRect clippeeLayer) {
        boolean isClipped = false;

        // For each clip rect in the current layer (where "layer" here means a
        // list of rects that came from the same original rectangle, and therefore never
        // need to be tested against one another), build a new list of rects (into mScratchRects)
        // by clipping against the current (left, top, right, bottom) rect. After all rects
        // in the layer have been examined, swap in the new list and delete the old list.
        for (Rect clippee : clippeeLayer.rects) {
            if (clipperLayer.originalRect.contains(clippee)) {
                // The clipper completely contains the clippee, so this clip
                // produces no rectangles.
                continue;
            }
            Rect intersection = mRectPool.acquire();
            intersection.set(clippee);
            if (intersection.intersect(clipperLayer.originalRect)) {

                if (intersection.top != clippee.top) {
                    Rect rTop = mRectPool.acquire();
                    rTop.set(clippee.left, clippee.top, clippee.right, intersection.top);
                    mScratchRects.add(rTop);
                    clippee.top = intersection.top;
                }

                // Create new rectangles above and to the right
                if (intersection.bottom != clippee.bottom) {
                    Rect rBottom = mRectPool.acquire();
                    rBottom.set(clippee.left, intersection.bottom, clippee.right, clippee.bottom);
                    mScratchRects.add(rBottom);
                    clippee.bottom = intersection.bottom;
                }

                if (intersection.left != clippee.left) {
                    Rect rLeft = mRectPool.acquire();
                    rLeft.set(clippee.left, clippee.top, intersection.left, clippee.bottom);
                    mScratchRects.add(rLeft);
                    clippee.left = intersection.left;
                }

                if (intersection.right != clippee.right) {
                    Rect rRight = mRectPool.acquire();
                    rRight.set(intersection.right, clippee.top, clippee.right, clippee.bottom);
                    mScratchRects.add(rRight);
                    clippee.right = intersection.right;
                }
            } else  {
                Rect scratchClippee = mRectPool.acquire();
                scratchClippee.set(clippee);
                mScratchRects.add(scratchClippee);
            }
            mRectPool.release(intersection);
        }
        for (Rect r : clippeeLayer.rects) {
            mRectPool.release(r);
        }
        clippeeLayer.rects.clear();
        clippeeLayer.rects.addAll(mScratchRects);
        mScratchRects.clear();
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
