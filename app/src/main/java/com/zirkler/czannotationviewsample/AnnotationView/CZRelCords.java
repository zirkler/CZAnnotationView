package com.zirkler.czannotationviewsample.AnnotationView;

import java.io.Serializable;

public class CZRelCords implements Serializable {
    private float x;
    private float y;

    public CZRelCords() { }

    public CZRelCords(float x, float y) {
        this.x = x;
        this.y = y;
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
