package com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.zirkler.czannotationviewsample.AnnotationView.CZPaint;
import com.zirkler.czannotationviewsample.AnnotationView.CZRelCords;
import com.zirkler.czannotationviewsample.AnnotationView.CZUndoRedoAction;

import java.util.List;

public class CZDrawingActionText implements CZIDrawingAction {

    public static int PADDING = 20;
    protected String mText = "NO TEXT PROVIDED";
    private CZRelCords mTextPositionCords;
    private CZRelCords mCordsBevoreMove;
    private CZPaint mTextPaint;
    private CZPaint mNormalPaint;
    private CZPaint mSelectionPaint;
    private CZPaint mRectanglePaint;
    private CZDrawingActionState mState;
    transient private RectF mRect;

    public CZDrawingActionText(Context context, CZPaint textPaint, String text) {

        mText = text;

        // Place the text view per default in the middle of the screen
        mTextPositionCords = new CZRelCords(0.5f, 0.5f);
        mCordsBevoreMove = mTextPositionCords.clone();


        if (textPaint == null) {
            mTextPaint = new CZPaint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint.setTextSize(40);

            mNormalPaint = new CZPaint();
            mNormalPaint.setAntiAlias(true);
            mNormalPaint.setColor(Color.parseColor("#B0000000")); // 20% opacity
            mNormalPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mNormalPaint.setStrokeJoin(Paint.Join.ROUND);
            mNormalPaint.setStrokeCap(Paint.Cap.ROUND);
            mNormalPaint.setStrokeWidth(10);

            mSelectionPaint = new CZPaint();
            mSelectionPaint.setAntiAlias(true);
            mSelectionPaint.setColor(Color.parseColor("#B0FF0000")); // 20% opacity
            mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mSelectionPaint.setStrokeJoin(Paint.Join.ROUND);
            mSelectionPaint.setStrokeCap(Paint.Cap.ROUND);
            mSelectionPaint.setStrokeWidth(10);

            mRectanglePaint = mNormalPaint;
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
            mTextPositionCords = new CZRelCords(x, y);
        }
    }

    @Override
    public void touchMoveRelative(float dx, float dy) {
        if (mState == CZDrawingActionState.ITEM_SELECTED) {
            mTextPositionCords.setX(mTextPositionCords.getX() + dx);
            mTextPositionCords.setY(mTextPositionCords.getY() + dy);
        }
    }

    @Override
    public CZUndoRedoAction touchUp(float x, float y) {
        if (mState == CZDrawingActionState.ITEM_DRAWING) {

        } else if (mState == CZDrawingActionState.ITEM_SELECTED) {
            TextMoveAction moveAction = new TextMoveAction(
                this,
                new CZRelCords(mTextPositionCords.getX(), mTextPositionCords.getY()),
                new CZRelCords(mCordsBevoreMove.getX(), mCordsBevoreMove.getY()));
            mCordsBevoreMove = new CZRelCords(mTextPositionCords.getX(), mTextPositionCords.getY());
            return moveAction;
        }
        return null;
    }

    @Override
    public void draw(Canvas canvas, RectF displayRect) {

        if (mTextPositionCords == null) return;

        String[] lines = mText.split("\n");
        // find widest line
        float textWidth = 0;
        for (int i = 0; i < lines.length; i++) {
            float currTextWidth = mTextPaint.measureText(lines[i], 0, lines[i].length());
            if (currTextWidth > textWidth) {
                textWidth = currTextWidth;
            }
        }

        float singleLineTextHeight = mTextPaint.getTextSize();
        float totalTextHeight = singleLineTextHeight * lines.length;

        float textPosX = mTextPositionCords.toAbsCordsAsPoint(displayRect).x;
        float textPosY = mTextPositionCords.toAbsCordsAsPoint(displayRect).y;

        // draw a rectangle around the text
        mRect = new RectF(
                    textPosX - PADDING,
                    textPosY - singleLineTextHeight + mTextPaint.descent() - PADDING,
                    textPosX + textWidth + PADDING,
                    textPosY + totalTextHeight + mTextPaint.ascent() + PADDING);


        canvas.drawRoundRect(mRect, 5, 5, mRectanglePaint);

        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], textPosX , textPosY + singleLineTextHeight * i, mTextPaint);
        }
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
        if (mRect == null) return false;

        if (mRect.contains(cords.toAbsCordsAsPoint(displayRect).x,
                           cords.toAbsCordsAsPoint(displayRect).y)) {
            return true;
        }
        return false;
    }

    @Override
    public float getClickDistance(CZRelCords cords, RectF displayRect, Context context) {
        if (mRect == null) return Float.MAX_VALUE;

        if (mRect.contains(cords.toAbsCordsAsPoint(displayRect).x,
                cords.toAbsCordsAsPoint(displayRect).y)) {
            return 0;
        }

        return Float.MAX_VALUE;
    }

    @Override
    public void setActionState(CZDrawingActionState state) {
        if (state == CZDrawingActionState.ITEM_DRAWN) {
            mRectanglePaint = mNormalPaint;
        } else if (state == CZDrawingActionState.ITEM_SELECTED) {
            mRectanglePaint = mSelectionPaint;
        }
        mState = state;
    }

    public String getText() {
        return mText;
    }

    public CZUndoRedoAction setText(String text) {
        if (!text.equals(mText)) {
            TextChangeAction textChangeAction = new TextChangeAction(this, mText, text);
            this.mText = text;
            return textChangeAction;
        }
        return null;
    }

    class TextMoveAction implements CZUndoRedoAction {

        CZDrawingActionText mTextItem;
        CZRelCords mPrevPosition;
        CZRelCords mNewPosition;

        TextMoveAction(CZDrawingActionText textItem, CZRelCords newPosition, CZRelCords prevPosition) {
            mTextItem = textItem;
            mNewPosition = newPosition;
            mPrevPosition = prevPosition;
        }

        @Override
        public void undo() {
            if (mPrevPosition != null) {
                mTextItem.mTextPositionCords = mPrevPosition;
            }

        }

        @Override
        public void redo() {
            if (mNewPosition != null) {
                mTextItem.mTextPositionCords = mNewPosition;
            }
        }
    }

    class TextChangeAction implements CZUndoRedoAction {

        CZDrawingActionText mTextItem;
        String mPrevText = "";
        String mNewText = "";

        TextChangeAction(CZDrawingActionText textItem, String prevText, String newText) {
            mTextItem = textItem;
            mPrevText = prevText;
            mNewText = newText;
        }

        @Override
        public void undo() {
            if (mPrevText == null) {
                mTextItem.mText = "#N.A.";
            } else {
                mTextItem.mText = mPrevText;
            }
        }

        @Override
        public void redo() {
            mTextItem.mText = mNewText;
        }
    }


}
