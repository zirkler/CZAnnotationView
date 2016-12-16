package com.zirkler.czannotationviewsample.AnnotationView;

import java.io.Serializable;

public interface CZUndoRedoAction extends Serializable {
    void undo();
    void redo();
}