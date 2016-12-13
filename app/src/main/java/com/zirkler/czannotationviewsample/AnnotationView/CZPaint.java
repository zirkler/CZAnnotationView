package com.zirkler.czannotationviewsample.AnnotationView;

import android.graphics.Paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * CZPaint is just a serializable extension of the Paint class.
 */
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

        if (mStyle != null) {
            super.setStyle(mStyle);
        }

        if (mStrokeJoin != null) {
            super.setStrokeJoin(mStrokeJoin);
        }

        if (mStrokeCap != null) {
            super.setStrokeCap(mStrokeCap);
        }

        super.setTextSize(mTextSize);
    }

    @Override
    public void setAntiAlias(boolean aa) {
        super.setAntiAlias(aa);
        mAntiAlias = true;
    }

    @Override
    public int getColor() {
        return super.getColor();
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        mColor = color;
    }

    @Override
    public Style getStyle() {
        return super.getStyle();
    }

    @Override
    public void setStyle(Style style) {
        mStyle = style;
        super.setStyle(style);
    }

    @Override
    public Join getStrokeJoin() {
        return super.getStrokeJoin();
    }

    @Override
    public void setStrokeJoin(Join join) {
        super.setStrokeJoin(join);
        mStrokeJoin = join;
    }

    @Override
    public Cap getStrokeCap() {
        return super.getStrokeCap();
    }

    @Override
    public void setStrokeCap(Cap cap) {
        super.setStrokeCap(cap);
        mStrokeCap = cap;
    }

    @Override
    public float getStrokeWidth() {
        return super.getStrokeWidth();
    }

    @Override
    public void setStrokeWidth(float width) {
        super.setStrokeWidth(width);
        mStrokeWidth = width;
    }

    @Override
    public float getTextSize() {
        return super.getTextSize();
    }

    @Override
    public void setTextSize(float textSize) {
        super.setTextSize(textSize);
        mTextSize = textSize;
    }
}
