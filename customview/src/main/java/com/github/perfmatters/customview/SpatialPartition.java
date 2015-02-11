package com.github.perfmatters.customview;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by ilewis on 2/9/15.
 *
 * WORK IN PROGRESS DO NOT USE
 */
public class SpatialPartition<T> {
    int mCellsX;
    int mCellsY;
    int mCellWidth;
    int mCellHeight;

    ArrayList[] mCells;

    SpatialPartition(int w, int h, int nCellsX, int nCellsY) {
        mCellsX = nCellsX;
        mCellsY = nCellsY;
        mCellWidth = w / nCellsX;
        mCellHeight = h / nCellsY;


        int nCells = nCellsX * nCellsY;
        mCells = new ArrayList[nCells];
        for (int i = 0; i < nCells; ++i) {
            mCells[i] = new ArrayList();
        }
    }

    public void addItem(T item, Rect bounds){
        addItem(item, bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void addItem(T item, int left, int top, int right, int bottom) {
        int cellLeft = left % mCellWidth;
        int cellTop = top % mCellHeight;
        int cellRight = right % mCellWidth;
        int cellBottom = bottom % mCellHeight;

        for (int i = cellLeft; i <= cellRight; ++i) {
            for (int j = cellTop; j <= cellBottom; ++j) {
                mCells[i * mCellsX + j].add(item);
            }
        }
    }
}
