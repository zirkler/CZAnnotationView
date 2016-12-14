package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;

import java.io.Serializable;
import java.util.List;

public interface CZIDrawingAction extends Serializable {


    void touchStart(float x, float y, RectF displayRect);

    void touchMove(float x, float y);

    void touchMoveRelative(float dx, float dy);

    void touchUp(float x, float y);

    void draw(Canvas canvas, RectF displayRect);

    CZIDrawingAction createInstance(Context context, CZPaint paint);

    List<CZRelCords> getCords();

    CZPaint getPaint();

    void setPaint(CZPaint paint);

    boolean isErasable();

    boolean checkIfClicked(CZRelCords cords, RectF displayRect, Context context);

    void setActionState(CZDrawingActionState state);

    /**
     * Checks if there is something which can be undone in this item.
     * @return Returns true if there is something to undo in this item, otherwise false.
     */
    boolean canUndo();

    /**
     * Check if there is something which can be redone in this item.
     * @return Return true if there is something to redo in this item, otherwise false.
     */
    boolean canRedo();

    /**
     * Undoes the last action in this item.
     */
    void undo();

    /**
     * Redoes the last action in this item.
     */
    void redo();

    enum CZDrawingActionState {
        ITEM_DRAWN,     // The item got drawn a long time ago
        ITEM_DRAWING,   // The user is currently drawing this item
        ITEM_SELECTED   // The user selected the item
    }
}
