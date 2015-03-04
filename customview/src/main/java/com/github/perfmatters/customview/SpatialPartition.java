package com.github.perfmatters.customview;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by ilewis on 2/9/15.
 *
 * WORK IN PROGRESS DO NOT USE
 */
public class SpatialPartition<T> {

    public interface ItemVisitor<T>{
        public void visit(T tag);
    }

    int mCellsX;
    int mCellsY;
    int mCellWidth;
    int mCellHeight;

    ArrayList<T>[] mCells;

    SpatialPartition(int w, int h, int nCellsX, int nCellsY) {
        mCellsX = nCellsX;
        mCellsY = nCellsY;
        mCellWidth = w / mCellsX;
        mCellHeight = h / mCellsY;

        int total = nCellsX * nCellsY;
        mCells = new ArrayList[total];
        for (int i = 0; i < total; ++i) {
            mCells[i] = new ArrayList<>();
        }
    }

    public void addItem(T item, Point position){
        addItem(item, position.x, position.y);
    }

    public void addItem(T item, int x, int y) {
        int cell = (y / mCellHeight) * mCellsX + (x / mCellWidth);
        mCells[cell].add(item);
    }

    public void visitItems(Rect bounds, ItemVisitor visitor) {
        visitItems(bounds.left, bounds.top, bounds.right,bounds.bottom, visitor);
    }

    public void visitItems(int left, int top, int right, int bottom, ItemVisitor visitor) {
        int cellLeft = left / mCellWidth;
        int cellTop = top / mCellHeight;
        int cellRight = right / mCellWidth;
        int cellBottom = bottom / mCellHeight;

        for (int i = cellLeft; i <= cellRight; ++i) {
            for (int j = cellTop; j <= cellBottom; ++j) {
                int cell = j * mCellsX + i;
                for (T item : mCells[cell]) {
                    visitor.visit(item);
                }
            }
        }
    }

    public void clear() {
        for (ArrayList<T> cell : mCells) {
            cell.clear();
        }
    }
}
