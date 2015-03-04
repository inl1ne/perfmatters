package com.github.perfmatters.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * TODO: document your custom view class.
 */
public class CardView extends View {
    private CardBitmaps mCardBitmaps;
    private Paint mPaint = new Paint();
    private Paint mPaint2 = new Paint();
    private RectClipper mClipper = new RectClipper(new Rect(0,0,4000,4000),16, 10000);
    private int maxCardsToDraw = 400;
    private boolean mDrawBitmaps = true;
    private boolean mClip = true;
    private boolean mDrawRects = false;
    private Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
    private boolean mDirty = true;
    private ClipVisitor mVisitor;

    public CardView(Context context) {
        super(context);
        init(null, 0);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public boolean isDrawBitmaps() {
        return mDrawBitmaps;
    }

    public void setDrawBitmaps(boolean mDrawBitmaps) {
        this.mDrawBitmaps = mDrawBitmaps;
        invalidate();
    }

    public boolean isClip() {
        return mClip;
    }

    public void setClip(boolean mClip) {
        this.mClip = mClip;
        invalidate();
    }

    public boolean isDrawRects() {
        return mDrawRects;
    }

    public void setDrawRects(boolean mDrawRects) {
        this.mDrawRects = mDrawRects;
        invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (isInEditMode()) return;
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CardView, defStyle, 0);

        mVisitor = new ClipVisitor();

        int w = a.getDimensionPixelSize(R.styleable.CardView_cardWidth, 90);
        int h = a.getDimensionPixelSize(R.styleable.CardView_cardHeight, 160);
        int bitmapResource = a.getResourceId(R.styleable.CardView_bitmapSheet, R.raw.cards);
        a.recycle();

        try {
            InputStream src = this.getResources().openRawResource(bitmapResource);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(src.available());
            byte[] buf = new byte[4096];
            int nRead = 0;
            while((nRead = src.read(buf)) > 0) {
                bytes.write(buf, 0, nRead);
            }

            mCardBitmaps = new CardBitmaps(bytes.toByteArray(), new CardBitmaps.CardBitmapsListener() {
                @Override
                public void onLoadProgress(int progress) {

                }

                @Override
                public void onLoadComplete() {
                    invalidate();
                }

                @Override
                public void onError(Exception e) {
                    try {
                        throw e;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //setLayerType(LAYER_TYPE_HARDWARE, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(0xff0000ff);
        mPaint.setTextSize(24);

        mPaint2.set(mPaint);
        mPaint2.setColor(0xffff0000);

    }

    private class Card {
        Rect screenRect;
        int id;

        private Card(Rect screenRect, int id) {
            this.screenRect = screenRect;
            this.id = id;
        }

        public Rect getScreenRect() {
            return screenRect;
        }

        public void setScreenRect(Rect screenRect) {
            this.screenRect = screenRect;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
    ArrayList<Card> mCards = new ArrayList<>();

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) return;
        mVisitor.setCanvas(canvas);
        mClipper.visitCliprects(mVisitor);
       // mDirty = false;
       // invalidate(new Rect(100, 100, 400, 500));
    }

    public int getMaxCardsToDraw() {
        return maxCardsToDraw;
    }

    public void setMaxCardsToDraw(int maxCardsToDraw) {
        this.maxCardsToDraw = maxCardsToDraw;

        mDirty = true;
        mCards.clear();
        mClipper.clear();
        for (int j = 0; j < maxCardsToDraw; ++j) {
            int row = j / CardBitmaps.CARDVALUE_COUNT;
            int suit = row % CardBitmaps.SUIT_COUNT;
            int id = j % CardBitmaps.CARDVALUE_COUNT;
            int left = id * 80 + 5;
            int top =  row * 60 + id * 10 + 5;
            int right = left + mCardBitmaps.getCardRect().width();
            int bottom = top + mCardBitmaps.getCardRect().height();
            if (mDirty) {
                Card card = new Card(new Rect(left,top,right,bottom), id);
                mCards.add(card);
                mClipper.addRectToBottom(card.getScreenRect(), card);
//              mClipper.addRect(suit * CardBitmaps.CARDVALUE_COUNT + card,
//                        left, top, right, bottom);
            }
        }

        invalidate();
    }

    private class ClipVisitor implements CardClipper.CardClipperVisitor, RectClipper.ClipRectVisitor<Card> {
        private Canvas mCanvas;

        public Canvas getCanvas() {
            return mCanvas;
        }

        public void setCanvas(Canvas canvas) {
            mCanvas = canvas;
        }

        @Override
        public void visit(int originalCardId, Rect originalRect, Rect clippedRect) {
            Bitmap bmp = mCardBitmaps.getCardBitmap(originalCardId);
            if (bmp != null) {
                Paint rectPaint = clippedRect.width() > clippedRect.height() ? mPaint : mPaint2;
                if (mClip) {
                    mCanvas.clipRect(clippedRect, Region.Op.REPLACE);
                }
                if (mDrawBitmaps) {
                    mCanvas.drawBitmap(bmp, originalRect.left, originalRect.top, mPaint);
                }
                if (mDrawRects) {
                    mPaint.getFontMetrics(mFontMetrics);
                    mCanvas.drawRect(clippedRect, rectPaint);
                    mCanvas.drawText(
                            String.format("%d", originalCardId),
                            clippedRect.left,
                            clippedRect.top - mFontMetrics.ascent,
                            rectPaint);
                }
            }
        }

        @Override
        public void visit(Rect clippedBounds, Card item) {
            visit(item.id, item.screenRect, clippedBounds);
        }
    }
}
