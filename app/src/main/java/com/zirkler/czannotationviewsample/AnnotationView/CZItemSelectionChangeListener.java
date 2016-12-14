package com.zirkler.czannotationviewsample.AnnotationView;


import android.view.MotionEvent;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;

public interface CZItemSelectionChangeListener {

    /**
     * The onItemSelectionChanged event gets called when the user cancels the selection a selected item.
     */
    void onItemSelectionChanged(CZIDrawingAction newSelectedItem, CZIDrawingAction prevSelectedItem, MotionEvent event);
}
