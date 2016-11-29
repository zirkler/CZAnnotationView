package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;



public class CZAttacher extends PhotoViewAttacher {

    private boolean mEditMode = true;
    private boolean isDrawingNow = false;
    private Context mContext;
    private CZPhotoView mPhotoView;

    public CZAttacher(ImageView imageView) {
        super(imageView);
        mContext = imageView.getContext();
        mPhotoView = (CZPhotoView) imageView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean isOneFinger = event.getPointerCount() == 1;

        // Since we performed scaling and dragging, we cannot simply use the screens touch-coordinates for our events,
        // instead we reconstruct the coordinates in respect to the performed matrix operations (translation and scaling).
        float[] mv = new float[9];
        Matrix suppMatrix = new Matrix();
        super.getSuppMatrix(suppMatrix);
        suppMatrix.getValues(mv);
        float transX = mv[Matrix.MTRANS_X] * -1;
        float transY = mv[Matrix.MTRANS_Y] * -1;
        float scaleX = mv[Matrix.MSCALE_X];
        float scaleY = mv[Matrix.MSCALE_Y];
        float touchX = Math.abs((int) ((event.getX() + transX) / scaleX));
        float touchY = Math.abs((int) ((event.getY() + transY) / scaleY));

        RectF displayRect = getDisplayRect();
        float pX = (event.getX() - getDisplayRect().left) / displayRect.width();
        float pY = (event.getY() - getDisplayRect().top) / displayRect.height();

        // User lays a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // User starts drawing
            if (isOneFinger) {
                isDrawingNow = true;
                mPhotoView.setCurrentDrawingAction(mPhotoView.getmCurrentDrawingAction().createInstance(mContext, null));
                mPhotoView.getmCurrentDrawingAction().touchStart(pX, pY);
            }

            // User added another finger
            if (!isOneFinger) {
                isDrawingNow = false;
                mPhotoView.userCanceldDrawing();
            }
        }

        // User moved a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            // User moved his drawing finger
            if (isOneFinger && isDrawingNow) {
                mPhotoView.getmCurrentDrawingAction().touchMove(pX, pY);
            }

            // User moved finger while there is more then one finger on the screen
            if (!isOneFinger) {
                isDrawingNow = false;
                mPhotoView.userCanceldDrawing();
            }
        }

        // User finished drawing
        if (event.getAction() == MotionEvent.ACTION_UP && isOneFinger && isDrawingNow) {
            mPhotoView.getmCurrentDrawingAction().touchUp(pX, pY);
            mPhotoView.userFinishedDrawing();

            super.cancelFling();
            isDrawingNow = false;
        }

        super.onTouch(v, event);

        mPhotoView.invalidate(); // TODO: check if we actually have to invalidate at this time...
        return true;
    }

    @Override
    public void onDrag(float dx, float dy) {
        // When user drags and is not drawing, we forward the translation information to the matrix in the photoview to adjust the draw canvas.
        if (!isDrawingNow) {
            super.onDrag(dx, dy);
            super.getSuppMatrix(mPhotoView.mConcatMatrix);
        }
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        // When user scales we forward the scaling information to the matrix in the photoview to adjust the draw canvas.
        super.onScale(scaleFactor, focusX, focusY);
        super.getSuppMatrix(mPhotoView.mConcatMatrix);
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        // only allow flinging when we are not in edit mode
        if (!mEditMode) {
            super.onFling(startX, startY, velocityX, velocityY);
            super.getSuppMatrix(mPhotoView.mConcatMatrix);
        }
    }

    public CZPhotoView getPhotoView() {
        return mPhotoView;
    }

    public void setPhotoView(CZPhotoView mPhotoView) {
        this.mPhotoView = mPhotoView;
    }
}
