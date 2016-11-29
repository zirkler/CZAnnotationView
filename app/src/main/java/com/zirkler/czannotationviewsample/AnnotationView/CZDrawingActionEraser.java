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

    CZPath mPath;
    CZPaint mEraserPaint;
    float mX;
    float mY;
    private List<ImageRelCoords> mCoords = new ArrayList<>();

    public CZDrawingActionEraser(Context context, CZPaint paint) {
        mPath = new CZPath();

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
    public void touchStart(float x, float y) {
        mX = x;
        mY = y;
        // mPath.moveTo(x, y);
        mCoords.add(new ImageRelCoords(x, y));
    }

    @Override
    public void touchMove(float x, float y) {
        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mPath.addCircle(x, y , 100, Path.Direction.CW);
        mCoords.add(new ImageRelCoords(x, y));
        mX = x;
        mY = y;
    }

    @Override
    public void touchUp(float x, float y) {
        mCoords.add(new ImageRelCoords(x, y));
    }


    @Override
    public void draw(Canvas canvas, RectF displayRect) {
        Path path = new Path();
        if (mCoords != null && mCoords.size() > 0) {
            // move to start coordinates
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
    public CZPath getPath() {
        return mPath;
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
    public boolean checkBounds(float x, float y) {
        return false;
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionEraser(context, paint);
    }
}
