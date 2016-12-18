package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZDrawingActionLine;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;


public class CZAttacher extends PhotoViewAttacher implements CZOnLongClickListener {

    public float[] lastScale;
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

            // Handle magnifier visibility for current drawing action
            if (mPhotoView.getCurrentDrawingAction() != null &&
                    mPhotoView.getCurrentDrawingAction() instanceof CZDrawingActionLine &&
                    mPhotoView.getMagnifierView() != null) {
                mPhotoView.getMagnifierView().setVisibility(View.VISIBLE);
            }

            // Handle magnifier visibility for selected item
            if (getSelectedItem() != null &&
                    getSelectedItem() instanceof CZDrawingActionLine &&
                    mPhotoView.getMagnifierView() != null) {
                mPhotoView.getMagnifierView().setVisibility(View.VISIBLE);
            }

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
                if (mSelectedItem != null && mSelectedItem.checkIfClicked(relCoords, mPhotoView.getInitialDisplayRect(), mContext)) {
                    // keep the item selected ...
                    mSelectedItem.touchStart(relCoords.getX(), relCoords.getY(), mPhotoView.getInitialDisplayRect());
                } else {
                    // User touched somewhere else, not on the selected item, now deselect the item
                    itemSelectionChanged(null, mSelectedItem, event);
                }
            }
        }

        // User moved a finger on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            // Check if the movement is bigger then the move threshold
            // TODO: move this into seperate method!
            PointF touchDownPoint = touchDownCords.toAbsCordsAsPoint(mPhotoView.getInitialDisplayRect());
            PointF touchNow = relCoords.toAbsCordsAsPoint(mPhotoView.getInitialDisplayRect());
            float distX = touchDownPoint.x - touchNow.x;
            float distY = touchDownPoint.y - touchNow.y;
            float totalDist = (float) Math.sqrt(distX*distX + distY*distY);
            if (totalDist > 3) {
                // User moved his finger
                if (isOneFinger && mCurrentState == CZState.CURRENTLY_DRAWING) {
                    mPhotoView.getCurrentDrawingAction().touchMove(relCoords.getX(), relCoords.getY());
                } else if (isOneFinger && mCurrentState == CZState.ITEM_SELECTED) {
                    mSelectedItem.touchMove(relCoords.getX(), relCoords.getY());
                }

                // User moved finger while there is more then one finger on the screen, we cancel everything then.
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
        }

        // User lifted finger up
        if (event.getAction() == MotionEvent.ACTION_UP) {

            // Hide magnifier
            if (mPhotoView.getMagnifierView() != null) {
                mPhotoView.getMagnifierView().setVisibility(View.INVISIBLE);
            }

            // Check if user "short-clicked" a drawn item
            if (touchDownCords.getX() == relCoords.getX() && touchDownCords.getY() == relCoords.getY()) {

                // Search for eventually clicked item
                CZIDrawingAction clickedItem = searchForSelectedItem(touchDownCords);
                if (clickedItem == null) {
                    // User did not perform a "short-click" event.
                    // If there was a selection before, invoke the itemSelectionChanged event
                    if (mSelectedItem != null) {
                        itemSelectionChanged(null, mSelectedItem, event);
                    }
                } else {
                    // The User performed "short-click" event, invoke the itemSelectionChanged event
                    itemSelectionChanged(clickedItem, mSelectedItem, event);

                    if (mPhotoView.getItemShortClickListener() != null) {
                        mPhotoView.getItemShortClickListener().onItemShortClicked(clickedItem, event);
                    }
                }
            }

            // User had an item selected, now lifted finger
            if (isOneFinger && mCurrentState == CZState.ITEM_SELECTED) {
                CZUndoRedoAction action = mSelectedItem.touchUp(relCoords.getX(), relCoords.getY());
                if (action != null) {
                    mPhotoView.addRedoableAction(action);
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
            // user is dragging an selected item
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
        // Move this away from here.
        // Deselect a maybe selected item
        /*mCurrentState = CZState.READY_TO_DRAW;
        if (mSelectedItem != null) {
            mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mSelectedItem = null;
        }*/


        lastScale = new float[]{scaleFactor, focusX, focusY};

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
            // User long-clicked on an item.
            itemSelectionChanged(selectedItem, mSelectedItem, event);
            mPhotoView.getItemLongClickListener().onItemLongClicked(selectedItem, event);
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
        for (int i = 0; i < mPhotoView.getDrawnActions().size(); i++) {
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

    public void setCurrentState(CZState mCurrentState) {
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

    public void itemSelectionChanged(CZIDrawingAction newSelectedItem, CZIDrawingAction prevSelectedItem, MotionEvent event) {

        // If there was a item previously select, deselect it properly
        if (prevSelectedItem != null) {
            prevSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
        }

        // Cancel an eventually started drawing action
        mPhotoView.cancelCurrentDrawingAction();

        if (newSelectedItem == null) {
            // User selected nothing
            mCurrentState = CZState.READY_TO_DRAW;
            mSelectedItem = null;

            // If something was previously select, no unselect it.
            if (prevSelectedItem != null) {
                prevSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            }

        } else {
            // If the user selected something new, change state
            mCurrentState = CZState.ITEM_SELECTED;
            mSelectedItem = newSelectedItem;
            mSelectedItem.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_SELECTED);

            // Move the selected element on top of the drawing stack.
            mPhotoView.getDrawnActions().remove(mSelectedItem);
            mPhotoView.getDrawnActions().add(mSelectedItem);
        }

        // Forward the event to the user
        if (mPhotoView.getItemSelectionChangeListener() != null) {
            mPhotoView.getItemSelectionChangeListener().onItemSelectionChanged(
                    newSelectedItem,
                    prevSelectedItem,
                    event);
        }
    }

    public enum CZState {
        READY_TO_DRAW,
        CURRENTLY_DRAWING,
        ITEM_SELECTED,
        DOUBLE_TAP_ZOOMING
    }
}
