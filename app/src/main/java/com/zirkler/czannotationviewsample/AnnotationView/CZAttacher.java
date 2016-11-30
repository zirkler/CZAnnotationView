package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class CZAttacher extends PhotoViewAttacher implements CZOnLongClickListener {

    private boolean mEditMode = true;
    private boolean isDrawingNow = false;
    private Context mContext;
    private CZPhotoView mPhotoView;

    public CZAttacher(ImageView imageView) {
        super(imageView);
        mContext = imageView.getContext();
        mPhotoView = (CZPhotoView) imageView;
        this.setOnLongClickListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean isOneFinger = event.getPointerCount() == 1;

        // Here map the pixel values of coordinate to a image relative representation.
        // pX/Y contains values between 0 and 1, if the user touches inside the image.
        // pX = 1 if the touches on the most right pixel of the image,
        // pX = 0.0...01 if the user touches the most left pixel in the image.
        // Same for pY, pY = 1 is bottom of the image, pY = 0 is top of the image.
        CZRelCords relCoords = pixelCoordToImageRelativeCoord(event, getDisplayRect());

        // User lays a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // User starts drawing
            if (isOneFinger) {
                isDrawingNow = true;
                mPhotoView.setCurrentDrawingAction(mPhotoView.getmCurrentDrawingAction().createInstance(mContext, null));
                mPhotoView.getmCurrentDrawingAction().touchStart(relCoords.getX(), relCoords.getY());
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
                mPhotoView.getmCurrentDrawingAction().touchMove(relCoords.getX(), relCoords.getY());
            }

            // User moved finger while there is more then one finger on the screen
            if (!isOneFinger) {
                isDrawingNow = false;
                mPhotoView.userCanceldDrawing();
            }
        }

        // User finished drawing
        if (event.getAction() == MotionEvent.ACTION_UP && isOneFinger && isDrawingNow) {
            mPhotoView.getmCurrentDrawingAction().touchUp(relCoords.getX(), relCoords.getY());
            mPhotoView.userFinishedDrawing();

            super.cancelFling();
            isDrawingNow = false;
        }

        mPhotoView.invalidate();
        super.onTouch(v, event);
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
        // Only allow flinging when we are not in edit mode
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


    @Override
    public boolean onLongClick(View view, MotionEvent e) {

        Log.i("asd", "LONG CLICK");
        CZRelCords cords = pixelCoordToImageRelativeCoord(e, getDisplayRect());
        for (int i = 0; i < mPhotoView.getDrawnActions().size(); i++) {
            if (mPhotoView.getDrawnActions().get(i).checkIfClicked(cords, mPhotoView.getInitialDisplayRect())) {
                Log.i("asd", "CLICKED DRAWN ITEM YO");
            }
        }
        return false;
    }

    /**
     * TODO: Document this bro.
     * @param e
     * @param displayRect
     * @return
     */
    private CZRelCords pixelCoordToImageRelativeCoord(MotionEvent e, RectF displayRect) {
        CZRelCords coords = new CZRelCords();
        coords.setX((e.getX() - getDisplayRect().left) / displayRect.width());
        coords.setY((e.getY() - getDisplayRect().top) / displayRect.height());
        return coords;
    }
}
