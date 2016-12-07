package com.zirkler.czannotationviewsample.AnnotationView;

import android.graphics.PointF;
import android.graphics.RectF;

import java.io.Serializable;

public class CZRelCords implements Serializable {
    private float x;
    private float y;

    public CZRelCords() { }

    public CZRelCords(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF toAbsCordsAsPoint(RectF displayRect) {
        PointF point = new PointF(
                this.getX() * displayRect.width() + displayRect.left,
                this.getY() * displayRect.height() + displayRect.top);
        return point;
    }

    public float[] toAbsCords(RectF displayRect) {
        float[] absoluteValues = new float[2];
        absoluteValues[0] = this.getX() * displayRect.width() + displayRect.left;
        absoluteValues[1] = this.getY() * displayRect.height() + displayRect.top;
        return absoluteValues;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
