package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZLine;
import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;

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
    public void touchStart(float x, float y, RectF displayRect) {
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
    public boolean checkIfClicked(CZRelCords clickCords, RectF displayRect, Context context) {
        clickAreaLines = new ArrayList<>();
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
    public List<CZRelCords> getCords() {
        return mCoords;
    }
}
