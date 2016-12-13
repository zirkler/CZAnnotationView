package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;

import java.util.List;

public class CZDrawingActionLine implements CZIDrawingAction {

    public static final int CLICK_AREA_TOLERANCE = 15;
    private CZPaint mPaint;
    private CZPaint mNormalPaint;
    private CZPaint mSelectionPaint;
    private CZPaint mHandlePaint;
    private CZRelCords mStartCord;
    private CZRelCords mEndCord;
    private CZDrawingActionState mState;
    private CZRelCords mCurrentlyEditingCords;
    private int handleRadius = 60;

    public CZDrawingActionLine(Context context, CZPaint paint) {

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
            mSelectionPaint.setColor(Color.GREEN);
            mSelectionPaint.setStyle(Paint.Style.STROKE);
            mSelectionPaint.setStrokeJoin(Paint.Join.ROUND);
            mSelectionPaint.setStrokeCap(Paint.Cap.ROUND);
            mSelectionPaint.setStrokeWidth(10);

            mHandlePaint = new CZPaint();
            mHandlePaint.setAntiAlias(true);
            mHandlePaint.setColor(Color.parseColor("#33ff0000")); // 20% opacity
            mHandlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mHandlePaint.setStrokeJoin(Paint.Join.ROUND);
            mHandlePaint.setStrokeCap(Paint.Cap.ROUND);
            mHandlePaint.setStrokeWidth(1);
        } else {
            mNormalPaint = paint;
        }

        mPaint = mNormalPaint;
    }

    @Override
    public void touchStart(float x, float y, RectF displayRect) {
        CZRelCords touchStartCord = new CZRelCords(x, y);

        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mStartCord = new CZRelCords(x, y);
        } if (mState == CZDrawingActionState.ITEM_SELECTED) {

            // Check if user touched one of the handles
            RectF startHandleClickArea = new RectF(
                    mStartCord.toAbsCords(displayRect)[0] - handleRadius / 2,
                    mStartCord.toAbsCords(displayRect)[1] - handleRadius / 2,
                    mStartCord.toAbsCords(displayRect)[0] + handleRadius / 2,
                    mStartCord.toAbsCords(displayRect)[1] + handleRadius / 2);

            RectF endHandleClickArea = new RectF(
                    mEndCord.toAbsCords(displayRect)[0] - handleRadius / 2,
                    mEndCord.toAbsCords(displayRect)[1] - handleRadius / 2,
                    mEndCord.toAbsCords(displayRect)[0] + handleRadius / 2,
                    mEndCord.toAbsCords(displayRect)[1] + handleRadius / 2);

            if (startHandleClickArea.contains(touchStartCord.toAbsCords(displayRect)[0],
                                              touchStartCord.toAbsCords(displayRect)[1])) {
                mCurrentlyEditingCords = mStartCord;
            } else if (endHandleClickArea.contains(touchStartCord.toAbsCords(displayRect)[0],
                                                   touchStartCord.toAbsCords(displayRect)[1])) {
                mCurrentlyEditingCords = mEndCord;
            } else {
                mCurrentlyEditingCords = null;
            }
        }
    }

    @Override
    public void touchMove(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mEndCord = new CZRelCords(x, y);
        }
    }

    @Override
    public void touchMoveRelative(float dx, float dy) {
        // implement this for easily move the item around
        if (mState == CZDrawingActionState.ITEM_SELECTED && mCurrentlyEditingCords == null) {
            mStartCord.setX(mStartCord.getX() + dx);
            mStartCord.setY(mStartCord.getY() + dy);
            mEndCord.setX(mEndCord.getX() + dx);
            mEndCord.setY(mEndCord.getY() + dy);
        }

        if (mState == CZDrawingActionState.ITEM_SELECTED && mCurrentlyEditingCords != null) {
            mCurrentlyEditingCords.setX(mCurrentlyEditingCords.getX() + dx);
            mCurrentlyEditingCords.setY(mCurrentlyEditingCords.getY() + dy);
        }
    }

    @Override
    public void touchUp(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mEndCord = new CZRelCords(x, y);
        }
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {
        if (mStartCord == null || mEndCord == null) return;

        float absStartX = mStartCord.getX() * displayRect.width() + displayRect.left;
        float absStartY = mStartCord.getY() * displayRect.height() + displayRect.top;

        float absEndX = mEndCord.getX() * displayRect.width() + displayRect.left;
        float absEndY = mEndCord.getY() * displayRect.height() + displayRect.top;

        canvas.drawLine(absStartX, absStartY, absEndX, absEndY, mPaint);

        if (mState == CZDrawingActionState.ITEM_SELECTED) {
            drawSelectionState(canvas, displayRect);
        }
    }

    private void drawSelectionState(Canvas canvas, RectF displayRect) {
        float absStartX = mStartCord.getX() * displayRect.width() + displayRect.left;
        float absStartY = mStartCord.getY() * displayRect.height() + displayRect.top;
        float absEndX = mEndCord.getX() * displayRect.width() + displayRect.left;
        float absEndY = mEndCord.getY() * displayRect.height() + displayRect.top;

        canvas.drawCircle(absStartX, absStartY, handleRadius, mHandlePaint);
        canvas.drawCircle(absEndX, absEndY, handleRadius, mHandlePaint);
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionLine(context, paint);
    }

    @Override
    public List<CZRelCords> getCords() {
        return null;
    }

    @Override
    public CZPaint getPaint() {
        return null;
    }

    @Override
    public void setPaint(CZPaint paint) {

    }

    @Override
    public boolean isErasable() {
        return false;
    }

    @Override
    public boolean checkIfClicked(CZRelCords clickCords, RectF displayRect, Context context) {

        // Allow to long press on end of line and directly extend / shrink it
        RectF startHandleClickArea = new RectF(
                mStartCord.toAbsCords(displayRect)[0] - handleRadius / 2,
                mStartCord.toAbsCords(displayRect)[1] - handleRadius / 2,
                mStartCord.toAbsCords(displayRect)[0] + handleRadius / 2,
                mStartCord.toAbsCords(displayRect)[1] + handleRadius / 2);

        RectF endHandleClickArea = new RectF(
                mEndCord.toAbsCords(displayRect)[0] - handleRadius / 2,
                mEndCord.toAbsCords(displayRect)[1] - handleRadius / 2,
                mEndCord.toAbsCords(displayRect)[0] + handleRadius / 2,
                mEndCord.toAbsCords(displayRect)[1] + handleRadius / 2);

        if (startHandleClickArea.contains(clickCords.toAbsCords(displayRect)[0],
                                          clickCords.toAbsCords(displayRect)[1])) {
            mCurrentlyEditingCords = mStartCord;
            return true;
        } else if (endHandleClickArea.contains(clickCords.toAbsCords(displayRect)[0],
                                               clickCords.toAbsCords(displayRect)[1])) {
            mCurrentlyEditingCords = mEndCord;
            return true;
        } else {
            mCurrentlyEditingCords = null;
        }


        // If distance from point to line is smaller then tolerance, it's a click on this line
        double absoluteDistance = CZPhotoView.pointToSegmentDistance(
                mStartCord.toAbsCordsAsPoint(displayRect),
                mEndCord.toAbsCordsAsPoint(displayRect),
                clickCords.toAbsCordsAsPoint(displayRect));

        double deviceIndependentDistance = absoluteDistance / context.getResources().getDisplayMetrics().density;

        if (deviceIndependentDistance <= CLICK_AREA_TOLERANCE) {
            return true;
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
}
