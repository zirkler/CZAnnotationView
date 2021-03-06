package com.zirkler.czannotationviewsample.AnnotationView;


import android.view.MotionEvent;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;

public interface CZItemLongClickListener {

    /**
     * The onItemLongClicked callback gets called when an item on the photo view got "long-clicked" by the user.
     * @param item The clicked item.
     * @param event The corresponding android motion event.
     */
    void onItemLongClicked(CZIDrawingAction item, MotionEvent event);
}
