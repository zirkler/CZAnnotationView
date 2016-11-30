package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CZDrawingActionFreehand implements CZIDrawingAction, Serializable {

    public static final int CLICK_AREA_TOLERANCE = 10;
    float mX;
    float mY;
    private CZPaint mPaint;
    private CZPaint mClickAreaPaint;
    private List<CZRelCords> mCoords = new ArrayList<>();
    transient private Region mRegion;
    transient private Path mPath;
    transient private List<CZLine> clickAreaLines = new ArrayList<>();


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



            mClickAreaPaint = new CZPaint();
            mClickAreaPaint.setAntiAlias(true);
            mClickAreaPaint.setColor(Color.BLACK);
            mClickAreaPaint.setStyle(Paint.Style.STROKE);
            mClickAreaPaint.setStrokeJoin(Paint.Join.ROUND);
            mClickAreaPaint.setStrokeCap(Paint.Cap.ROUND);
            mClickAreaPaint.setStrokeWidth(5);
        } else {
            mPaint = paint;
        }
    }

    @Override
    public void touchStart(float x, float y) {
        mX = x;
        mY = y;
        mCoords.add(new CZRelCords(x, y));
    }

    @Override
    public void touchMove(float x, float y) {
        /*mPath.quadTo(mX,
                     mY,
                     (x + mX) / 2,
                     (y + mY) / 2); */
        Log.i("asd", String.valueOf(x) + " " + String.valueOf(y));
        mCoords.add(new CZRelCords(x, y));
        mX = x;
        mY = y;
    }

    @Override
    public void touchUp(float x, float y) {
        mCoords.add(new CZRelCords(x, y));
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
    public boolean checkIfClicked(CZRelCords cords, RectF displayRect) {
        float cordX = (cords.getX() * displayRect.width() + displayRect.left);
        float cordY = (cords.getY() * displayRect.height() + displayRect.top);

        clickAreaLines.clear();
        for (int i = 0; i < mCoords.size() - 1; i++) {
            float topXCoordinate;
            float topYCoordinate;
            float bottomXCoordinate;
            float bottomYCoordinate;

            // find top and bottom point
            CZRelCords topPoint;
            CZRelCords bottomPoint;

            if (mCoords.get(i).getY() < mCoords.get(i+1).getY()) {
                topPoint = mCoords.get(i);
                bottomPoint = mCoords.get(i+1);
            } else {
                topPoint = mCoords.get(i+1);
                bottomPoint = mCoords.get(i);
            }

            // Tramslate image relative points back to actual pixel coordinates
            topXCoordinate = topPoint.getX()       * displayRect.width()  + displayRect.left;
            topYCoordinate = topPoint.getY()       * displayRect.height() + displayRect.top;
            bottomXCoordinate = bottomPoint.getX() * displayRect.width()  + displayRect.left;
            bottomYCoordinate = bottomPoint.getY() * displayRect.height() + displayRect.top;

            // build polygon around this two points
            CZPolygon polygon = CZPolygon.Builder()
                    .addVertex(new CZPoint(topXCoordinate    - CLICK_AREA_TOLERANCE, topYCoordinate    - CLICK_AREA_TOLERANCE))
                    .addVertex(new CZPoint(topXCoordinate    + CLICK_AREA_TOLERANCE, topYCoordinate    - CLICK_AREA_TOLERANCE))
                    .addVertex(new CZPoint(bottomXCoordinate + CLICK_AREA_TOLERANCE, bottomYCoordinate + CLICK_AREA_TOLERANCE))
                    .addVertex(new CZPoint(bottomXCoordinate - CLICK_AREA_TOLERANCE, bottomYCoordinate + CLICK_AREA_TOLERANCE))
                    .close()
                    .build();

            // Uncomment below line to draw the click area polygons, awesome when debugging.
            // clickAreaLines.addAll(polygon.getSides());

            // Perform the actual check if users clicked inside the clickarea
            if (polygon.contains(new CZPoint(cordX, cordY))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {
        mPath = new Path();
        if (mCoords != null && mCoords.size() > 0) {
            mPath.moveTo(mCoords.get(0).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(0).getY() * displayRect.height() + displayRect.top);

            for (int i = 1; i < mCoords.size(); i++) {
                mPath.lineTo(mCoords.get(i).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(i).getY() * displayRect.height() + displayRect.top);
            }
            canvas.drawPath(mPath, mPaint);

            for (int i = 0; i < clickAreaLines.size(); i++) {
                canvas.drawLine(
                        clickAreaLines.get(i).getStart().x,
                        clickAreaLines.get(i).getStart().y,
                        clickAreaLines.get(i).getEnd().x,
                        clickAreaLines.get(i).getEnd().y,
                        mClickAreaPaint
                );
            }
        }
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionFreehand(context, paint);
    }

    @Override
    public List<CZRelCords> getCoords() {
        return mCoords;
    }
}
