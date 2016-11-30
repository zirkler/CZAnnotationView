package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.io.Serializable;
import java.util.List;

public interface CZIDrawingAction extends Serializable {


    void touchStart(float x, float y);

    void touchMove(float x, float y);

    void touchUp(float x, float y);

    void moveStart();

    void moveItem(float relDX, float relDY);

    void moveFinished();

    void draw(Canvas canvas, RectF displayRect);

    CZIDrawingAction createInstance(Context context, CZPaint paint);

    List<CZRelCords> getCoords();

    CZPaint getPaint();

    void setPaint(CZPaint paint);

    boolean isErasable();

    boolean checkBounds(float x, float y);

    boolean checkIfClicked(CZRelCords coords, RectF displayRect);
}
