package com.github.perfmatters.customview;

/**
 * Created by ilewis on 2/2/15.
 */
public class BackgroundProgress<T> {
    private T mProgress;
    private Exception mErr;

    public BackgroundProgress(T progress, Exception err) {
        this.mProgress = progress;
        this.mErr = err;
    }

    public BackgroundProgress(T progress) {
        this.mProgress = progress;
        this.mErr = null;
    }

    public BackgroundProgress(Exception mErr) {
        this.mErr = mErr;
    }

    public BackgroundProgress() {
        this.mErr = null;
    }

    public T getProgress() {
        return mProgress;
    }

    public void setProgress(T progress) {
        this.mProgress = progress;
    }

    public Exception getErr() {
        return mErr;
    }

    public void setErr(Exception err) {
        this.mErr = err;
    }
}
