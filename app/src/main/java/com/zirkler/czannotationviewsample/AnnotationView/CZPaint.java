package com.zirkler.czannotationviewsample.AnnotationView;

import android.graphics.Paint;
import android.support.annotation.ColorInt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class CZPaint extends Paint implements Serializable {

    private boolean mAntiAlias;
    private int mColor;
    private Style mStyle;
    private Join mStrokeJoin;
    private Cap mStrokeCap;
    private float mStrokeWidth;
    private float mTextSize;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        super.setAntiAlias(mAntiAlias);
        super.setColor(mColor);
        super.setStyle(mStyle);
        super.setStrokeJoin(mStrokeJoin);
        super.setStrokeCap(mStrokeCap);
        super.setStrokeWidth(mStrokeWidth);
        super.setTextSize(mTextSize);
    }

    public float getmStrokeWidth() {
        return mStrokeWidth;
    }

    public void setmStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }

    public Cap getmStrokeCap() {
        return mStrokeCap;
    }

    public void setmStrokeCap(Cap mStrokeCap) {
        this.mStrokeCap = mStrokeCap;
    }

    public Join getmStrokeJoin() {
        return mStrokeJoin;
    }

    public void setmStrokeJoin(Join mStrokeJoin) {
        this.mStrokeJoin = mStrokeJoin;
    }

    public Style getmStyle() {
        return mStyle;
    }

    public void setmStyle(Style mStyle) {
        this.mStyle = mStyle;
    }

    public int getmColor() {
        return mColor;
    }

    public void setmColor(@ColorInt int mColor) {
        this.mColor = mColor;
    }

    public boolean ismAntiAlias() {
        return mAntiAlias;
    }

    public void setmAntiAlias(boolean mAntiAlias) {
        this.mAntiAlias = mAntiAlias;
    }

    public float getmTextSize() {
        return mTextSize;
    }

    public void setmTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }
}
