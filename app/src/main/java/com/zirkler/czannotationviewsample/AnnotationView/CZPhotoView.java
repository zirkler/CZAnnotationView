package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;


public class CZPhotoView extends PhotoView {

    Matrix mConcatMatrix = new Matrix();
    private CZIDrawingAction mCurrentDrawingAction;
    private List<CZIDrawingAction> mDrawnActions = new ArrayList<>();
    private List<CZIDrawingAction> mRedoActions = new ArrayList<>();
    private Canvas cacheCanvas;
    private Bitmap foreground;
    private Paint mBitmapPaint;
    private RectF initialDisplayRect;

    public CZPhotoView(Context context) {
        super(context);
        setup();
    }

    public CZPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
        setup();
    }

    public CZPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        setup();
    }

    private void setup() {

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        // We wait until the layouting has finished, and then receive width and height of our view
        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                foreground = Bitmap.createBitmap(
                        getWidth(),
                        getHeight(),
                        Bitmap.Config.ARGB_8888);

                cacheCanvas = new Canvas();
                cacheCanvas.setBitmap(foreground);
                initialDisplayRect = getDisplayRect();
            }
        });
    }


    /**
     * This method performs the actual drawing of the users drawn stuff.
     * @param canvas The canvas of the imageview / photoview.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear the cache canvas
        cacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Here happens all the scaling and translation MAGIC!
        canvas.concat(mConcatMatrix);

        // Draw all the already drawn stuff to the canvas.
        for (int i = 0; i < mDrawnActions.size(); i++) {
            mDrawnActions.get(i).draw(cacheCanvas, initialDisplayRect);
        }

        // Draw the path the user is currently drawing.
        if (mCurrentDrawingAction != null) {
            mCurrentDrawingAction.draw(cacheCanvas, initialDisplayRect);
        }

        Paint drawBitmapPaint = new Paint();
        drawBitmapPaint.setAntiAlias(true);
        drawBitmapPaint.setFilterBitmap(true);
        canvas.drawBitmap(foreground, 0, 0, drawBitmapPaint);
    }

    /**
     * Undoes the last drawn thing on the canvas.
     */
    public void undo() {
        if (mDrawnActions.size() > 0) {
            CZIDrawingAction lastAction = mDrawnActions.get(mDrawnActions.size() - 1);
            mDrawnActions.remove(lastAction);
            mRedoActions.add(lastAction);
            invalidate();
        }
    }

    /**
     * Redos the last undone action.
     */
    public void redo() {
        if (mRedoActions.size() > 0) {
            CZIDrawingAction lastUndoneAction = mRedoActions.get(mRedoActions.size() - 1);
            mRedoActions.remove(lastUndoneAction);
            mDrawnActions.add(lastUndoneAction);
            invalidate();
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
        mCurrentDrawingAction = mCurrentDrawingAction.createInstance(getContext(), null);
        mRedoActions.clear();
        invalidate();
    }

    /**
     * Gets called from the CZAttacher, i.e. when user lays down second finger while drawing.
     */
    public void userCanceldDrawing() {
        mCurrentDrawingAction = mCurrentDrawingAction.createInstance(getContext(), null);
        invalidate();
    }

    public List<CZIDrawingAction> getDrawnActions() {
        return mDrawnActions;
    }

    public void setmDrawnActions(List<CZIDrawingAction> mDrawnActions) {
        this.mDrawnActions = mDrawnActions;
    }
}
