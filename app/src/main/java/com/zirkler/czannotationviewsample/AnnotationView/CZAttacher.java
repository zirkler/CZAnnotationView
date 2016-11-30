package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class CZAttacher extends PhotoViewAttacher implements CZOnLongClickListener {

    private boolean mEditMode = true;
    private Context mContext;
    private CZPhotoView mPhotoView;
    private CZState mCurrentState = CZState.READY_TO_DRAW;
    private CZIDrawingAction mSelectedItem;

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
            if (isOneFinger && mCurrentState == CZState.READY_TO_DRAW) {
                mCurrentState = CZState.CURRENTLY_DRAWING;
                mPhotoView.setCurrentDrawingAction(mPhotoView.getmCurrentDrawingAction().createInstance(mContext, null));
                mPhotoView.getmCurrentDrawingAction().touchStart(relCoords.getX(), relCoords.getY());
            }
        }

        // User moved a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            // User moved his drawing finger
            if (isOneFinger && mCurrentState == CZState.CURRENTLY_DRAWING) {
                mPhotoView.getmCurrentDrawingAction().touchMove(relCoords.getX(), relCoords.getY());
            } else if (isOneFinger && mCurrentState == CZState.MOVE) {

            }

            // User moved finger while there is more then one finger on the screen
            if (!isOneFinger) {

                if (mCurrentState == CZState.CURRENTLY_DRAWING) {
                    mPhotoView.cancelCurrentDrawingAction();
                    mCurrentState = CZState.READY_TO_DRAW;
                } else if (mCurrentState == CZState.MOVE) {
                    mSelectedItem.moveFinished();
                    mSelectedItem = null;
                    mCurrentState = CZState.READY_TO_DRAW;
                }

            }
        }


        // User lifted finger up
        if (event.getAction() == MotionEvent.ACTION_UP) {

            // User was drawing, now finger got lifted
            if (isOneFinger && mCurrentState == CZState.CURRENTLY_DRAWING) {
                mPhotoView.getmCurrentDrawingAction().touchUp(relCoords.getX(), relCoords.getY());
                mPhotoView.userFinishedDrawing();
                super.cancelFling();
                mCurrentState = CZState.READY_TO_DRAW;
            }

            // User was moving, now finger got lifted
            if (isOneFinger && mCurrentState == CZState.MOVE) {
                mSelectedItem.moveFinished();
                mSelectedItem = null;
                mCurrentState = CZState.READY_TO_DRAW;
            }
        }

        mPhotoView.invalidate();
        super.onTouch(v, event);
        return true;
    }

    // useful to work with relative distances
    @Override
    public void onDrag(float dx, float dy) {

        if (mCurrentState == CZState.MOVE) {
            // user is dragging an item
            float relativeDX = dx / getDisplayRect().width();
            float relativeDY = dy / getDisplayRect().height();
            mSelectedItem.moveItem(relativeDX, relativeDY);

        } else if (mCurrentState == CZState.READY_TO_DRAW) {
            // When user drags and is not drawing, we forward the translation information to
            // the matrix in the CZPhotoView to adjust the draw canvas.
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
    public boolean onLongClick(View view, MotionEvent event) {
        CZRelCords cords = pixelCoordToImageRelativeCoord(event, getDisplayRect());

        // Search the item stack beggining form the for a clicked item
        for (int i = mPhotoView.getDrawnActions().size() - 1; i >= 0; i--) {
            CZIDrawingAction currAction = mPhotoView.getDrawnActions().get(i);
            if (currAction.checkIfClicked(cords, mPhotoView.getmInitialDisplayRect())) {
                mPhotoView.cancelCurrentDrawingAction();
                mCurrentState = CZState.MOVE;
                mSelectedItem = currAction;
                mPhotoView.getItemClickListener().onItemLongClicked(currAction, event);
                return true;
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

    public enum CZState {
        READY_TO_DRAW,
        CURRENTLY_DRAWING,
        MOVE,
        EDIT,
        VIEW
    }
}
