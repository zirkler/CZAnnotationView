package com.zirkler.czannotationviewsample.AnnotationView;

import android.view.MotionEvent;
import android.view.View;

/**
 * A custom OnLongClickListener which also takes a MotionEvent as parameter.
 */
public interface CZOnLongClickListener {

    boolean onLongClick(View view, MotionEvent e);
}
