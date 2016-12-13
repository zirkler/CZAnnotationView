package com.zirkler.czannotationviewsample.AnnotationView;


import android.view.MotionEvent;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;

public interface CZItemShortClickListener {

    /**
     * The onItemShortClicked callback gets called when an item on the photo view got "short-clicked" by the user.
     * @param item The clicked item.
     * @param event The corresponding android motion event.
     */
    void onItemShortClicked(CZIDrawingAction item, MotionEvent event);
}
