package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CZDrawingActionFreehand implements CZIDrawingAction, Serializable {

    public static final int CLICK_AREA_TOLERANCE = 15;
    float mX;
    float mY;
    private CZPaint mPaint;
    private CZPaint mNormalPaint;
    private CZPaint mMovementPaint;
    private CZPaint mClickAreaPaint;
    private CZPaint mSelectionPaint;
    private List<CZRelCords> mCoords = new ArrayList<>();
    transient private Path mPath;
    transient private List<CZLine> clickAreaLines = new ArrayList<>();
    private CZDrawingActionState mState;

    public CZDrawingActionFreehand(Context context, CZPaint paint) {
        mCoords = new ArrayList<>();

        // If there isn't a paint provided, create a default paint.
        if (paint == null) {
            mNormalPaint = new CZPaint();
            mNormalPaint.setAntiAlias(true);
            mNormalPaint.setColor(Color.BLACK);
            mNormalPaint.setStyle(Paint.Style.STROKE);
            mNormalPaint.setStrokeJoin(Paint.Join.ROUND);
            mNormalPaint.setStrokeCap(Paint.Cap.ROUND);
            mNormalPaint.setStrokeWidth(10);

            mSelectionPaint = new CZPaint();
            mSelectionPaint.setAntiAlias(true);
            mSelectionPaint.setColor(Color.YELLOW);
            mSelectionPaint.setStyle(Paint.Style.STROKE);
            mSelectionPaint.setStrokeJoin(Paint.Join.ROUND);
            mSelectionPaint.setStrokeCap(Paint.Cap.ROUND);
            mSelectionPaint.setStrokeWidth(10);

            mMovementPaint = new CZPaint();
            mMovementPaint.setAntiAlias(true);
            mMovementPaint.setColor(Color.RED);
            mMovementPaint.setStyle(Paint.Style.STROKE);
            mMovementPaint.setStrokeJoin(Paint.Join.ROUND);
            mMovementPaint.setStrokeCap(Paint.Cap.ROUND);
            mMovementPaint.setStrokeWidth(10);

            mClickAreaPaint = new CZPaint();
            mClickAreaPaint.setAntiAlias(true);
            mClickAreaPaint.setColor(Color.BLACK);
            mClickAreaPaint.setStyle(Paint.Style.STROKE);
            mClickAreaPaint.setStrokeJoin(Paint.Join.ROUND);
            mClickAreaPaint.setStrokeCap(Paint.Cap.ROUND);
            mClickAreaPaint.setStrokeWidth(5);
        } else {
            mNormalPaint = paint;
        }

        mPaint = mNormalPaint;
    }

    @Override
    public void touchStart(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mX = x;
            mY = y;
            mCoords.add(new CZRelCords(x, y));
        }
    }

    @Override
    public void touchMove(float x, float y) {
        // user is currently drawing this item
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mCoords.add(new CZRelCords(x, y));
            mX = x;
            mY = y;
        }
    }

    @Override
    public void touchMoveRelative(float dx, float dy) {
        if (mState == CZDrawingActionState.ITEM_SELECTED) {
            for (int i = 0; i < mCoords.size(); i++) {
                CZRelCords currCords = mCoords.get(i);
                currCords.setX(currCords.getX() + dx);
                currCords.setY(currCords.getY() + dy);
            }
        }
    }

    @Override
    public void touchUp(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            /*mPath.quadTo(mX,
                     mY,
                     (x + mX) / 2,
                     (y + mY) / 2); */
            mCoords.add(new CZRelCords(x, y));
            //mPath.lineTo(x, y);
        }

    }

    @Override
    public CZPaint getPaint() {
        return mNormalPaint;
    }

    @Override
    public void setPaint(CZPaint paint) {
        mNormalPaint = paint;
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

        clickAreaLines = new ArrayList<>();
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

            // Translate image relative points back to actual pixel coordinates
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
    public void setActionState(CZDrawingActionState state) {
        mState = state;
        if (state == CZDrawingActionState.ITEM_SELECTED) {
            mPaint = mSelectionPaint;
        } else if (state == CZDrawingActionState.ITEM_DRAWN){
            mPaint = mNormalPaint;
        }
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {
        mPath = new Path();
        CZPaint currPaint;

        if (mCoords != null && mCoords.size() > 0) {
            mPath.moveTo(mCoords.get(0).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(0).getY() * displayRect.height() + displayRect.top);

            for (int i = 1; i < mCoords.size(); i++) {
                mPath.lineTo(mCoords.get(i).getX() * displayRect.width() + displayRect.left,
                        mCoords.get(i).getY() * displayRect.height() + displayRect.top);
            }
            canvas.drawPath(mPath, mPaint);

            // draw click area polygons
            if (clickAreaLines != null) {
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
