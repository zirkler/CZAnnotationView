package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.io.Serializable;
import java.util.List;

public interface CZIDrawingAction extends Serializable {


    void touchStart(float x, float y);

    void touchMove(float x, float y);

    void touchMoveRelative(float dx, float dy);

    void touchUp(float x, float y);

    void draw(Canvas canvas, RectF displayRect);

    CZIDrawingAction createInstance(Context context, CZPaint paint);

    List<CZRelCords> getCoords();

    CZPaint getPaint();

    void setPaint(CZPaint paint);

    boolean isErasable();

    boolean checkBounds(float x, float y);

    boolean checkIfClicked(CZRelCords coords, RectF displayRect);

    void setActionState(CZDrawingActionState state);

    enum CZDrawingActionState {
        ITEM_DRAWN,     // The item got drawn a long time ago
        ITEM_DRAWING,   // The user is currently drawing this item
        ITEM_SELECTED,  // The user selected the item
        ITEM_MOVING     // THe user moved the item right now
    }
}
