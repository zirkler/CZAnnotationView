package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;
import com.zirkler.czannotationviewsample.AnnotationView.CZUndoRedoAction;

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
    private CZRelCords mStartPoint;
    private List<CZRelCords> mCoords = new ArrayList<>();
    transient private Path mPath;
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
    public void touchStart(float x, float y, RectF displayRect) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mStartPoint = new CZRelCords(x, y);
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

        // We currently don't want freehand lines to be movable.
        /*
        if (mState == CZDrawingActionState.ITEM_SELECTED) {
            mStartPoint.setX(mStartPoint.getX() + dx);
            mStartPoint.setY(mStartPoint.getY() + dy);

            for (int i = 0; i < mCoords.size(); i++) {
                CZRelCords currCords = mCoords.get(i);
                currCords.setX(currCords.getX() + dx);
                currCords.setY(currCords.getY() + dy);
            }
        }*/
    }

    @Override
    public CZUndoRedoAction touchUp(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            if (mCoords.size() > 0) {
                mCoords.add(new CZRelCords(x, y));
            }
        }
        return null;
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
    public boolean checkIfClicked(CZRelCords clickCords, RectF displayRect, Context context) {
        for (int i = 0; i < mCoords.size() - 1; i++) {

            // If distance from point to line is smaller then tolerance, it's a click on this line
            double absoluteDistance = CZPhotoView.pointToSegmentDistance(
                    mCoords.get(i).toAbsCordsAsPoint(displayRect),
                    mCoords.get(i + 1).toAbsCordsAsPoint(displayRect),
                    clickCords.toAbsCordsAsPoint(displayRect));

            double deviceIndependentDistance = absoluteDistance / context.getResources().getDisplayMetrics().density;

            if (deviceIndependentDistance <= CLICK_AREA_TOLERANCE) {
                return true;
            }
        }

        return false;
    }

    @Override
    public float getClickDistance(CZRelCords cords, RectF displayRect, Context context) {
        float smallestDistance = Float.MAX_VALUE;
        for (int i = 0; i < mCoords.size() - 1; i++) {

            // If distance from point to line is smaller then tolerance, it's a click on this line
            float absoluteDistance = (float) CZPhotoView.pointToSegmentDistance(
                    mCoords.get(i).toAbsCordsAsPoint(displayRect),
                    mCoords.get(i + 1).toAbsCordsAsPoint(displayRect),
                    cords.toAbsCordsAsPoint(displayRect));

            float deviceIndependentDistance = absoluteDistance / context.getResources().getDisplayMetrics().density;

            if (deviceIndependentDistance < smallestDistance) {
                smallestDistance = deviceIndependentDistance;
            }
        }

        return smallestDistance;
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

        if (mCoords != null && mCoords.size() > 0) {

            // Move to start position
            mPath.moveTo(mStartPoint.toAbsCordsAsPoint(displayRect).x,
                         mStartPoint.toAbsCordsAsPoint(displayRect).y);

            // Quad through all coordinates
            for (int i = 1; i < mCoords.size(); i++) {
                float x1 = mCoords.get(i-1).toAbsCordsAsPoint(displayRect).x;
                float y1 = mCoords.get(i-1).toAbsCordsAsPoint(displayRect).y;
                float x2 = mCoords.get(i).toAbsCordsAsPoint(displayRect).x;
                float y2 = mCoords.get(i).toAbsCordsAsPoint(displayRect).y;

                mPath.quadTo(x1,
                             y1,
                             (x1 + x2) / 2,
                             (y1 + y2) / 2);
            }
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionFreehand(context, paint);
    }

    @Override
    public List<CZRelCords> getCords() {
        return mCoords;
    }
}
