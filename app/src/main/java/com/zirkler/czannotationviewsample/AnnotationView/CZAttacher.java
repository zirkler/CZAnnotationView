package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;


public class CZAttacher extends PhotoViewAttacher implements CZOnLongClickListener {

    private boolean mEditMode = true;
    private Context mContext;
    private CZPhotoView mPhotoView;
    private CZState mCurrentState = CZState.READY_TO_DRAW;
    private CZIDrawingAction mSelectedItem;
    private CZRelCords touchDownCords;

    public CZAttacher(ImageView imageView) {
        super(imageView);
        mContext = imageView.getContext();
        mPhotoView = (CZPhotoView) imageView;
        this.setOnLongClickListener(this);
        mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this, mPhotoView));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean isOneFinger = event.getPointerCount() == 1;
        CZRelCords relCoords = pixelCoordToImageRelativeCoord(event, getDisplayRect());

        // TODO: This breaks zooming, fix it!
        // handleMagnifierPosition(event);



        // Send absolute touch coordinates to the magnifier view
        mPhotoView.getMagnifierView().setFocusX(event.getX());
        mPhotoView.getMagnifierView().setFocusY(event.getY());

        // User lays a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // Store touch down cords, on touch up we check if user performed any movements.
            touchDownCords = relCoords;

            // User starts drawing
            if (isOneFinger && mCurrentState == CZState.READY_TO_DRAW) {
                mCurrentState = CZState.CURRENTLY_DRAWING;
                mPhotoView.setCurrentDrawingAction(mPhotoView.getCurrentDrawingAction().createInstance(mContext, null));
                mPhotoView.getCurrentDrawingAction().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWING);
                mPhotoView.getCurrentDrawingAction().touchStart(relCoords.getX(), relCoords.getY(), mPhotoView.getInitialDisplayRect());
            }

            // User has an item selected and now lays a finger down
            if (isOneFinger && mCurrentState == CZState.ITEM_SELECTED) {
                // Check if user touched on the selected item (ie. to perform moving or editing)
                if (mSelectedItem.checkIfClicked(relCoords, mPhotoView.getInitialDisplayRect(), mContext)) {
                    // keep the item selected ...
                    mSelectedItem.touchStart(relCoords.getX(), relCoords.getY(), mPhotoView.getInitialDisplayRect());
                } else {
                    // User touched somewhere else, not on the selected item, now unselect the item
                    mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                    mSelectedItem = null;
                    mCurrentState = CZState.READY_TO_DRAW;
                }
            }
        }

        // User moved a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            // User moved his finger
            if (isOneFinger && mCurrentState == CZState.CURRENTLY_DRAWING) {
                mPhotoView.getCurrentDrawingAction().touchMove(relCoords.getX(), relCoords.getY());
            } else if (isOneFinger && mCurrentState == CZState.ITEM_SELECTED) {
                mPhotoView.getCurrentDrawingAction().touchMove(relCoords.getX(), relCoords.getY());
            }

            // User moved finger while there is more then one finger on the screen
            if (!isOneFinger) {
                if (mCurrentState == CZState.CURRENTLY_DRAWING) {
                    mPhotoView.cancelCurrentDrawingAction();
                    mCurrentState = CZState.READY_TO_DRAW;
                } else if (mCurrentState == CZState.ITEM_SELECTED) {
                    mPhotoView.cancelCurrentDrawingAction();
                    mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                    mSelectedItem = null;
                    mCurrentState = CZState.READY_TO_DRAW;
                }
            }
        }

        // User lifted finger up
        if (event.getAction() == MotionEvent.ACTION_UP) {

            // Check if user "short-clicked" a drawn item
            if (touchDownCords.getX() == relCoords.getX() && touchDownCords.getY() == relCoords.getY()) {

                // Search for eventually clicked item
                CZIDrawingAction selectedItem = searchForSelectedItem(touchDownCords);
                if (selectedItem != null) {
                    // reset eventually already selected item
                    if (mSelectedItem != null) {
                        mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                    }

                    mPhotoView.cancelCurrentDrawingAction();
                    mCurrentState = CZState.ITEM_SELECTED;
                    mSelectedItem = selectedItem;

                    if (mPhotoView.getItemShortClickListener() != null) {
                        mPhotoView.getItemShortClickListener().onItemShortClicked(selectedItem, event);
                    }

                    mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_SELECTED);

                    // Move the selected element on top of the drawing stack.
                    mPhotoView.getDrawnActions().remove(mSelectedItem);
                    mPhotoView.getDrawnActions().add(mSelectedItem);
                }
            }

            // User was drawing, now finger got lifted
            if (isOneFinger && mCurrentState == CZState.CURRENTLY_DRAWING) {
                mPhotoView.getCurrentDrawingAction().touchUp(relCoords.getX(), relCoords.getY());
                mPhotoView.getCurrentDrawingAction().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                mPhotoView.userFinishedDrawing();
                super.cancelFling();
                mCurrentState = CZState.READY_TO_DRAW;
            }
        }

        mPhotoView.invalidate();
        super.onTouch(v, event);
        return true;
    }

    public void handleMagnifierPosition(MotionEvent event) {
        // check if we intercept with magnifier view
        int fullWidth = mPhotoView.getWidth();
        if (0 < event.getX() && event.getX() < (0.333f)*fullWidth) {
            // in first third, move magnifier right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPhotoView.getMagnifierView().getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mPhotoView.getMagnifierView().setLeft(500);
            }
        }

        if ((0.666f)*fullWidth < event.getX() && event.getX() < fullWidth) {
            // in third third, move magnifier left
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPhotoView.getMagnifierView().getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                /*params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mPhotoView.getMagnifierView().setLayoutParams(params);*/

            }
        }
    }

    // useful to work with relative distances
    @Override
    public void onDrag(float dx, float dy) {

        if (mCurrentState == CZState.ITEM_SELECTED) {
            // user is dragging an item
            float relativeDX = dx / getDisplayRect().width();
            float relativeDY = dy / getDisplayRect().height();
            mSelectedItem.touchMoveRelative(relativeDX, relativeDY);
        } else if (mCurrentState == CZState.READY_TO_DRAW) {
            // When user drags and is not drawing, we forward the translation information to
            // the matrix in the CZPhotoView to adjust the draw canvas.
            super.onDrag(dx, dy);
            super.getSuppMatrix(mPhotoView.mConcatMatrix);
        }
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        // Deselect a maybe selected item
        mCurrentState = CZState.READY_TO_DRAW;
        if (mSelectedItem != null) {
            mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mSelectedItem = null;
        }

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

    @Override
    public boolean onLongClick(View view, MotionEvent event) {
        CZRelCords cords = pixelCoordToImageRelativeCoord(event, getDisplayRect());

        // If user is currently zooming we do not allow any selections.
        if (mCurrentState == CZState.DOUBLE_TAP_ZOOMING) {
            return true;
        }

        CZIDrawingAction selectedItem = searchForSelectedItem(cords);
        if (selectedItem != null) {
            mPhotoView.cancelCurrentDrawingAction();
            mCurrentState = CZState.ITEM_SELECTED;
            mSelectedItem = selectedItem;
            mPhotoView.getItemLongClickListener().onItemLongClicked(selectedItem, event);
            mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_SELECTED);

            // Move the selected element on top of the drawing stack.
            mPhotoView.getDrawnActions().remove(mSelectedItem);
            mPhotoView.getDrawnActions().add(mSelectedItem);


            return true;
        }
        return false;
    }

    /**
     * Here we map the pixel values of coordinates to a image relative representation.
     * pX/Y contains values between 0 and 1, if the user touches inside the image.
     * pX = 1 if the touches on the most right pixel of the image,
     * pX = 0.0...01 if the user touches the most left pixel in the image.
     * Same for pY, pY = 1 is bottom of the image, pY = 0 is top of the image.
     * @param event Motion Event
     * @param displayRect
     * @return The relative coordinate.
     */
    private CZRelCords pixelCoordToImageRelativeCoord(MotionEvent event, RectF displayRect) {
        CZRelCords coords = new CZRelCords();
        coords.setX((event.getX() - getDisplayRect().left) / displayRect.width());
        coords.setY((event.getY() - getDisplayRect().top) / displayRect.height());
        return coords;
    }

    /**
     * Search through the already drawn item if one of them got clicked by the clickCords.
     * @param clickCords The coordinates which should be checked if they belong to the clickarea of any of the drawn items.
     * @return Returns the clicked item. If nothing got clicked, this method return null.
     */
    private CZIDrawingAction searchForSelectedItem(CZRelCords clickCords) {
        CZIDrawingAction selectedItem = null;
        for (int i = mPhotoView.getDrawnActions().size() - 1; i >= 0; i--) {
            CZIDrawingAction currAction = mPhotoView.getDrawnActions().get(i);
            if (currAction.checkIfClicked(clickCords, mPhotoView.getInitialDisplayRect(), mContext)) {
                selectedItem = currAction;
            }
        }
        return selectedItem;
    }

    public CZPhotoView getPhotoView() {
        return mPhotoView;
    }

    public void setPhotoView(CZPhotoView mPhotoView) {
        this.mPhotoView = mPhotoView;
    }

    public CZState getmCurrentState() {
        return mCurrentState;
    }

    public void setmCurrentState(CZState mCurrentState) {
        this.mCurrentState = mCurrentState;
    }

    public CZIDrawingAction getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(CZIDrawingAction mSelectedItem) {
        this.mSelectedItem = mSelectedItem;
    }

    public CZIDrawingAction getmSelectedItem() {
        return mSelectedItem;
    }

    public void setmSelectedItem(CZIDrawingAction mSelectedItem) {
        this.mSelectedItem = mSelectedItem;
    }

    public enum CZState {
        READY_TO_DRAW,
        CURRENTLY_DRAWING,
        ITEM_SELECTED,
        DOUBLE_TAP_ZOOMING
    }
}
