package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class CZDrawingActionEraser implements CZIDrawingAction {

    CZPaint mEraserPaint;
    float mX;
    float mY;
    private List<CZRelCords> mCoords;

    public CZDrawingActionEraser(Context context, CZPaint paint) {
        mCoords = new ArrayList<>();

        if (paint == null) {
            mEraserPaint = new CZPaint();
            mEraserPaint.setAlpha(0xff);
            mEraserPaint.setColor(Color.TRANSPARENT);
            mEraserPaint.setStrokeWidth(100);
            mEraserPaint.setStyle(Paint.Style.STROKE);
            mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
            mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
            mEraserPaint.setMaskFilter(null);
            mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraserPaint.setAntiAlias(true);
        } else {
            mEraserPaint = paint;
        }
    }

    @Override
    public void touchStart(float x, float y, RectF displayRect) {
        mX = x;
        mY = y;
        mCoords.add(new CZRelCords(x, y));
    }

    @Override
    public void touchMove(float x, float y) {
        mCoords.add(new CZRelCords(x, y));
        mX = x;
        mY = y;
    }

    @Override
    public void touchMoveRelative(float dx, float dy) {

    }

    @Override
    public void touchUp(float x, float y) {
        mCoords.add(new CZRelCords(x, y));
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {
        Path path = new Path();
        if (mCoords != null && mCoords.size() > 0) {
            // moveItem to start coordinates
            path.moveTo(mCoords.get(0).getX() * displayRect.width() + displayRect.left,
                    mCoords.get(0).getY() * displayRect.height() + displayRect.top);

            // following along the coordinates
            for (int i = 1; i < mCoords.size(); i++) {
                path.lineTo(mCoords.get(i).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(i).getY() * displayRect.height() + displayRect.top);
            }
            canvas.drawPath(path, mEraserPaint);
        }
    }

    @Override
    public CZPaint getPaint() {
        return mEraserPaint;
    }

    @Override
    public void setPaint(CZPaint paint) {
        mEraserPaint = paint;
    }

    @Override
    public boolean isErasable() {
        return true;
    }

    @Override
    public boolean checkIfClicked(CZRelCords cords, RectF displayRect, Context context) {
        return false;
    }

    @Override
    public void setActionState(CZDrawingActionState state) {

    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionEraser(context, paint);
    }

    @Override
    public List<CZRelCords> getCoords() {
        return mCoords;
    }
}
