package com.github.perfmatters.customview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by ilewis on 2/2/15.
 */
public class CardBitmaps {
    public static final int SUIT_COUNT = 4;
    public static final int SUIT_CLUBS = 0;
    public static final int SUIT_HEARTS = 1;
    public static final int SUIT_SPADES = 2;
    public static final int SUIT_DIAMONDS = 3;

    public static final int CARDVALUE_COUNT = 13;
    public static final int CARDVALUE_ACE = 0;
    public static final int CARDVALUE_TWO = 1;
    public static final int CARDVALUE_THREEE = 2;
    public static final int CARDVALUE_FOUR = 3;
    public static final int CARDVALUE_FIVE = 4;
    public static final int CARDVALUE_SIX = 5;
    public static final int CARDVALUE_SEVEN = 6;
    public static final int CARDVALUE_EIGHT = 7;
    public static final int CARDVALUE_NINE = 8;
    public static final int CARDVALUE_TEN = 9;
    public static final int CARDVALUE_JACK = 10;
    public static final int CARDVALUE_QUEEN = 11;
    public static final int CARDVALUE_KING = 12;

    public static final int CARD_COUNT = SUIT_COUNT * CARDVALUE_COUNT;

    public interface CardBitmapsListener {
        public void onLoadProgress(int progress);
        public void onLoadComplete();
        public void onError(Exception e);
    }
    private CardBitmapsListener mListener;

    // Using an AtomicReferenceArray allows us to safely set bitmap refs from a
    // worker thread without the use of blocking synchronization.
    private AtomicReferenceArray<Bitmap> mBitmaps = new AtomicReferenceArray<Bitmap>(CARD_COUNT);
    private AtomicInteger mLoaded = new AtomicInteger(0);
    private Rect mRect; // same for every card
    private final BitmapFactory.Options mOptions;


    private void loadCard(BitmapRegionDecoder decoder,
                          int i,
                          Rect initialBounds) {
        int suit = i % SUIT_COUNT;
        int value = i % CARDVALUE_COUNT;
        int top = suit * initialBounds.height();
        int left = value * initialBounds.width();
        initialBounds.offsetTo(left, top);
/*        Log.d("DECODE", String.format("%d %d: %d %d %d %d\n",
                suit,
                value,
                initialBounds.left,
                initialBounds.top,
                initialBounds.right,
                initialBounds.bottom));
*/
        Bitmap bmp = decoder.decodeRegion(initialBounds, mOptions);

        if (!mBitmaps.compareAndSet(i, null, bmp)) {
            throw new IllegalThreadStateException(
                    String.format(
                            "Bitmap loader worker threads collided on item %d", i)
            );
        }
        mLoaded.incrementAndGet();
    }

    private BitmapRegionDecoder getDecoderAndInitialRect(byte[] data, Rect r) throws IOException {
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);
        r.set(0, 0,
                decoder.getWidth() / CARDVALUE_COUNT,
                decoder.getHeight() / SUIT_COUNT);
        return decoder;
    }

    private class CardLoaderTask
            extends AsyncTask<Integer, BackgroundProgress<Integer>, Integer> {
        private byte[] mSourceData;
        private BackgroundProgress<Integer> mProgress = new BackgroundProgress<Integer>();

        public CardLoaderTask(byte[] source) {
            mSourceData = source;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            BitmapRegionDecoder decoder = null;
            Rect r = new Rect();
            try {
                int begin = params[0];
                int end = params[1];
                decoder = getDecoderAndInitialRect(mSourceData, r);

                for (int i = begin; i <= end; ++i) {
                    loadCard(decoder, i, r);
                    mProgress.setProgress(i);
                    publishProgress(mProgress);
                }
            } catch (Exception e) {
                mProgress.setErr(e);
                publishProgress(mProgress);
            } finally {
                if (decoder != null) {
                    decoder.recycle();
                }
            }
            return 0;
        }



        @Override
        protected void onProgressUpdate(BackgroundProgress<Integer>... values) {
            BackgroundProgress progress = values[0];
            if (progress.getErr() != null) {
                mListener.onError(progress.getErr());
            } else {
                mListener.onLoadProgress(mLoaded.get());
            }
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            // Check for mLoaded to equal CARD_COUNT, which indicates
            // that all workers have completed. Set back to -1 to
            // indicate that the load-completed event has been raised,
            // so we don't call it twice.
            if (mLoaded.compareAndSet(CARD_COUNT, -1)) {
                mListener.onLoadComplete();
            }
        }

        @Override
        protected void onCancelled() {
            try {
                throw new Exception("Loading cancelled");
            } catch (Exception e) {
                mProgress.setErr(e);
                publishProgress(mProgress);
            }
        }

    }

    public CardBitmaps(byte[] source,
                       CardBitmapsListener listener,
                       boolean loadInBackground) throws IOException {
        mRect = new Rect(0, 0, 0, 0);
        mListener = listener;
        mOptions = new BitmapFactory.Options();

        BitmapRegionDecoder decoder = getDecoderAndInitialRect(source, mRect);

        if (loadInBackground) {
            for (int i = 0; i < SUIT_COUNT; ++i) {
                int begin = i * CARDVALUE_COUNT;
                int end = begin + CARDVALUE_COUNT - 1;
                CardLoaderTask task = new CardLoaderTask(source);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, begin, end);
            }
        } else {
            Rect r = new Rect();
            for (int i = 0; i < CARD_COUNT; ++i) {
                loadCard(decoder, i, r);
            }
        }

    }

    public Bitmap getCardBitmap(int suit, int value) {
        return mBitmaps.get(suit * CARDVALUE_COUNT + value);
    }

    public Bitmap getCardBitmap(int cardId) {
        return mBitmaps.get(cardId);
    }

    public boolean isDone() {
        return mLoaded.get() == -1;
    }

    public Rect getCardRect() {
        return mRect;
    }
}
