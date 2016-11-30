package com.zirkler.czannotationviewsample.AnnotationView;


import android.view.MotionEvent;

public interface CZIItemLongClickListener {

    /**
     * The onItemLongClicked callback gets called when an item on the photo view got clicked by the user.
     * @param item The clicked item.
     * @param event The corresponding android motion event.
     */
    void onItemLongClicked(CZIDrawingAction item, MotionEvent event);
}
