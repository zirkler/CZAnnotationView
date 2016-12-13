package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;

import java.util.List;

public class CZDrawingActionText implements CZIDrawingAction {

    private CZRelCords mCords;
    private CZPaint mTextPaint;
    private CZPaint mPaint;
    private CZDrawingActionState mState;
    private RectF mRect;
    private String mText = "NO TEXT PROVIDED";

    public CZDrawingActionText(Context context, CZPaint textPaint, String text) {

        mText = text;
        mCords = new CZRelCords(0.5f, 0.5f);

        if (textPaint == null) {
            mTextPaint = new CZPaint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setTextSize(40);

            mPaint = new CZPaint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.DKGRAY);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(10);

        } else {
            mTextPaint = textPaint;
        }
    }

    @Override
    public void touchStart(float x, float y, RectF displayRect) {
    }

    @Override
    public void touchMove(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {
            mCords = new CZRelCords(x, y);
        }
    }

    @Override
    public void touchMoveRelative(float dx, float dy) {
        if (mState == CZDrawingActionState.ITEM_SELECTED) {
            mCords.setX(mCords.getX() + dx);
            mCords.setY(mCords.getY() + dy);
        }
    }

    @Override
    public void touchUp(float x, float y) {
        mCords = new CZRelCords(x, y);
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {

        if (mCords == null) return;

        float textWidth = mTextPaint.measureText(mText, 0, mText.length());
        float textHeight = mTextPaint.getTextSize();

        float textPosX = mCords.toAbsCordsAsPoint(displayRect).x;
        float textPosY = mCords.toAbsCordsAsPoint(displayRect).y;

        // draw a rectangle around the mText
        mRect = new RectF(
                    textPosX,
                    (textPosY - textHeight),
                    textPosX + textWidth,
                    textPosY);
        canvas.drawRect(mRect, mPaint);
        canvas.drawText(mText, textPosX, textPosY, mTextPaint);
    }

    @Override
    public CZIDrawingAction createInstance(Context context, CZPaint paint) {
        return new CZDrawingActionFreehand(context, null);
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
    public boolean checkIfClicked(CZRelCords cords, RectF displayRect, Context context) {
        if (mRect.contains(cords.toAbsCordsAsPoint(displayRect).x,
                           cords.toAbsCordsAsPoint(displayRect).y)) {
            return true;
        }
        return false;
    }

    @Override
    public void setActionState(CZDrawingActionState state) {
        if (state == CZDrawingActionState.ITEM_DRAWN) {
            mPaint.setColor(Color.DKGRAY);
        } else if (state == CZDrawingActionState.ITEM_SELECTED) {
            mPaint.setColor(Color.GREEN);
        }
        mState = state;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }
}
