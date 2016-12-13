package com.zirkler.czannotationviewsample.AnnotationView;

import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overridden with custom behavior, if needed.
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

    private CZAttacher mCZAttacher;
    private CZPhotoView mCZPhotoView;

    /**
     * Default constructor
     *
     * @param photoViewAttacher PhotoViewAttacher to bind to
     */
    public DefaultOnDoubleTapListener(CZAttacher photoViewAttacher, CZPhotoView photoView) {
        setmCZAttacher(photoViewAttacher);
        mCZPhotoView = photoView;
    }

    /**
     * Allows to change PhotoViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacher PhotoViewAttacher to bind to
     */
    public void setmCZAttacher(CZAttacher newPhotoViewAttacher) {
        this.mCZAttacher = newPhotoViewAttacher;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (this.mCZAttacher == null)
            return false;

        ImageView imageView = mCZAttacher.getImageView();

        if (null != mCZAttacher.getOnPhotoTapListener()) {
            final RectF displayRect = mCZAttacher.getDisplayRect();

            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    mCZAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                }else{
                    mCZAttacher.getOnPhotoTapListener().onOutsidePhotoTap();
                }
            }
        }
        if (null != mCZAttacher.getOnViewTapListener()) {
            mCZAttacher.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {

        if (mCZAttacher == null)
            return false;

        Log.i("onDoubleTap before ", mCZAttacher.getmCurrentState().toString());
        mCZAttacher.setmCurrentState(CZAttacher.CZState.DOUBLE_TAP_ZOOMING);
        Log.i("asd", mCZAttacher.getmCurrentState().toString());
        if (mCZAttacher.getSelectedItem() != null) {
            mCZAttacher.getSelectedItem().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mCZAttacher.setSelectedItem(null);
        }

        try {
            float scale = mCZAttacher.getScale();
            float x = ev.getX();
            float y = ev.getY();

            if (scale < mCZAttacher.getMediumScale()) {
                mCZAttacher.setScale(mCZAttacher.getMediumScale(), x, y, true);
            } else if (scale >= mCZAttacher.getMediumScale() && scale < mCZAttacher.getMaximumScale()) {
                mCZAttacher.setScale(mCZAttacher.getMaximumScale(), x, y, true);
            } else {
                mCZAttacher.setScale(mCZAttacher.getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        Log.i("asd finsihed double tap", mCZAttacher.getmCurrentState().toString());
        mCZAttacher.setmCurrentState(CZAttacher.CZState.READY_TO_DRAW);
        Log.i("asd", mCZAttacher.getmCurrentState().toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

    public CZPhotoView getmCZPhotoView() {
        return mCZPhotoView;
    }

    public void setmCZPhotoView(CZPhotoView mCZPhotoView) {
        this.mCZPhotoView = mCZPhotoView;
    }

}
