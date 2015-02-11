package com.github.perfmatters.customview;

import android.support.v4.util.Pools;

import java.util.Arrays;

/**
 * Created by ilewis on 2/9/15.
 */
public class UncheckedPool<T> implements Pools.Pool<T> {
    private final Object[] mPool;
    private int mPoolSize;
    /**
     * Creates a new instance.
     *
     * @param maxPoolSize The max pool size.
     *
     * @throws IllegalArgumentException If the max pool size is less than zero.
     */
    public UncheckedPool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        mPool = new Object[maxPoolSize];
    }
    @Override
    @SuppressWarnings("unchecked")
    public T acquire() {
        if (mPoolSize > 0) {
            final int lastPooledIndex = mPoolSize - 1;
            T instance = (T) mPool[lastPooledIndex];
            mPool[lastPooledIndex] = null;
            mPoolSize--;
            return instance;
        }
        return null;
    }
    @Override
    public boolean release(T instance) {
        if (mPoolSize < mPool.length) {
            mPool[mPoolSize] = instance;
            mPoolSize++;
            return true;
        }
        return false;
    }


}
