package com.zirkler.czannotationviewsample.AnnotationView;

import android.view.MotionEvent;
import android.view.View;

/**
 * A custom OnLongClickListener which also takes a MotionEvent as parameter.
 */
public interface CZOnLongClickListener {

    /**
     * User long clicked on an item on the drawing view.
     * @param view The CZPhotoView
     * @param event The android motion event.
     * @return A boolean value.
     */
    boolean onLongClick(View view, MotionEvent event);
}
