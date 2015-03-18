package com.github.perfmatters.customview;

/**
 * Created by ilewis on 3/2/15.
 */
public class HandlePool {
    int[] free;
    int head;

    public HandlePool(int size) {
        free = new int[size];
        int i;
        for(i = 0; i < size - 1; ++i) {
            free[size] = i + 1;
        }
        free[i] = -1;
        head = 0;
    }

    public int allocHandle() {
        int handle = head;
        head = free[head];
        return handle;
    }

    public void freeHandle(int handle) {
        free[handle] = head;
        head = handle;
    }
}
