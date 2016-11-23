package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

public class CZDrawingActionFreehand implements CZIDrawingAction, Serializable {

    float mX;
    float mY;
    private Context mContext;
    private CZPaint mPaint;
    private CZPath mPath;

    public CZDrawingActionFreehand(Context context, CZPaint paint) {
        mContext = context;
        mPath = new CZPath();

        // If there isn't a paint provided, create a default paint.
        if (paint == null) {
            mPaint = new CZPaint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(10);
        } else {
            mPaint = paint;
        }
    }

    @Override
    public void touchStart(float x, float y) {
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
    }

    @Override
    public void touchMove(float x, float y) {
        mPath.quadTo(mX,
                     mY,
                     (x + mX) / 2,
                     (y + mY) / 2);
        mX = x;
        mY = y;
    }

    @Override
    public void touchUp(float x, float y) {
        mPath.lineTo(x, y);
    }

    @Override
    public CZPath getPath() {
        return mPath;
    }

    @Override
    public CZPaint getPaint() {
        return null;
    }

    @Override
    public void setPaint(CZPaint paint) {
        mPaint = paint;
    }

    @Override
    public boolean isErasable() {
        return true;
    }

    @Override
    public boolean checkBounds(float x, float y) {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionFreehand(context, paint);
    }
}
