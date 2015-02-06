package com.github.perfmatters.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * TODO: document your custom view class.
 */
public class CardView extends View {
    private CardBitmaps mCardBitmaps;
    private Paint mPaint = new Paint();

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

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CardView, defStyle, 0);

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

            mCardBitmaps = new CardBitmaps(bytes.toByteArray(), w, h, new CardBitmaps.CardBitmapsListener() {
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
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (mCardBitmaps.isDone())
            canvas.clipRect(100,100,400,500);

        for (int i = 0; i < CardBitmaps.SUIT_COUNT *2; ++i) {
            for (int j = 0; j < CardBitmaps.CARDVALUE_COUNT; ++j) {
                Bitmap bmp = mCardBitmaps.getCardBitmap(i % CardBitmaps.SUIT_COUNT, j);
                if (bmp != null) {
                    canvas.drawBitmap(bmp, j * 40, i * 100, mPaint);
                }
            }
        }
        invalidate(new Rect(100,100,400,500));
    }
}
