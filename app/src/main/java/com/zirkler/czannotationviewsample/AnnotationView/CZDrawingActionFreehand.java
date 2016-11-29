package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CZDrawingActionFreehand implements CZIDrawingAction, Serializable {

    float mX;
    float mY;
    private CZPaint mPaint;
    private List<CZImageRelCoords> mCoords = new ArrayList<>();

    public CZDrawingActionFreehand(Context context, CZPaint paint) {
        mCoords = new ArrayList<>();

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
        mCoords.add(new CZImageRelCoords(x, y));
    }

    @Override
    public void touchMove(float x, float y) {
        /*mPath.quadTo(mX,
                     mY,
                     (x + mX) / 2,
                     (y + mY) / 2); */
        Log.i("asd", String.valueOf(x) + " " + String.valueOf(y));
        mCoords.add(new CZImageRelCoords(x, y));
        mX = x;
        mY = y;
    }

    @Override
    public void touchUp(float x, float y) {
        mCoords.add(new CZImageRelCoords(x, y));
        //mPath.lineTo(x, y);
    }

    @Override
    public CZPaint getPaint() {
        return mPaint;
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
    public void draw(Canvas canvas, RectF displayRect) {
        Path path = new Path();
        if (mCoords != null && mCoords.size() > 0) {
            path.moveTo(mCoords.get(0).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(0).getY() * displayRect.height() + displayRect.top);

            for (int i = 1; i < mCoords.size(); i++) {
                path.lineTo(mCoords.get(i).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(i).getY() * displayRect.height() + displayRect.top);
            }
            canvas.drawPath(path, mPaint);
        }
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionFreehand(context, paint);
    }

    @Override
    public List<CZImageRelCoords> getCoords() {
        return mCoords;
    }
}
