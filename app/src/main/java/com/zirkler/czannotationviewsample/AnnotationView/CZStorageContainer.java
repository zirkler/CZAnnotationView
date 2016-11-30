package com.zirkler.czannotationviewsample.AnnotationView;

import java.io.Serializable;
import java.util.List;

/**
 * Used for easy saving and loading from a single file.
 */
public class CZStorageContainer implements Serializable {
    List<CZIDrawingAction> drawnActions;
    byte[] bitmapBytes;
}
