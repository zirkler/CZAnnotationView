package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;


public class CZPhotoView extends PhotoView {

    Matrix mConcatMatrix = new Matrix();
    private CZIDrawingAction mCurrentDrawingAction;
    private List<CZIDrawingAction> mDrawnActions = new ArrayList<>();
    private List<CZIDrawingAction> mRedoActions = new ArrayList<>();

    public CZPhotoView(Context context) {
        super(context);
    }

    public CZPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public CZPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    /**
     * This method performs the actual drawing of the users drawn stuff.
     * @param canvas The canvas of the image / photoview.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Here happens all the scaling and translation MAGIC!
        canvas.concat(mConcatMatrix);

        // Draw the path the user is currently drawing.
        if (mCurrentDrawingAction != null) {
            mCurrentDrawingAction.draw(canvas);
        }

        // Then draw all the already drawn stuff to the canvas.
        for (int i = 0; i < mDrawnActions.size(); i++) {
            mDrawnActions.get(i).draw(canvas);
        }
    }

    /**
     * Undoes the last drawn thing on the canvas.
     */
    public boolean undo() {
        if (mDrawnActions.size() > 0) {
            CZIDrawingAction lastAction = mDrawnActions.get(mDrawnActions.size() - 1);
            mDrawnActions.remove(lastAction);
            mRedoActions.add(lastAction);
            invalidate();
            return true;
        } else {
            // Nothing to undo
            return false;
        }
    }

    /**
     * Redos the last undone action.
     */
    public boolean redo() {
        if (mRedoActions.size() > 0) {
            CZIDrawingAction lastUndoneAction = mRedoActions.get(mRedoActions.size() - 1);
            mRedoActions.remove(lastUndoneAction);
            mDrawnActions.add(lastUndoneAction);
            invalidate();
            return true;
        } else {
            // Nothing to redo
            return false;
        }
    }

    public CZIDrawingAction getmCurrentDrawingAction() {
        return mCurrentDrawingAction;
    }

    public void setmCurrentDrawingAction(CZIDrawingAction mCurrentDrawingAction) {
        this.mCurrentDrawingAction = mCurrentDrawingAction;
    }

    /**
     * Gets called from the CZAttacher.
     */
    public void userFinishedDrawing() {
        mDrawnActions.add(mCurrentDrawingAction);
        mCurrentDrawingAction = null;
        mRedoActions.clear();
        invalidate();
    }
}
