package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

public class MagnifierView extends View {

    public CZPhotoView mCZPhotoView;
    private Bitmap mBitmap;
    private float mFocusX;
    private float mFocusY;

    public MagnifierView(Context context) {
        super(context);
        setup(context);
    }

    public MagnifierView(Context context, AttributeSet attr) {
        super(context, attr);
        setup(context);
    }

    public MagnifierView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        setup(context);
    }

    private void setup(Context context) {
        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBitmap = Bitmap.createBitmap(
                        getWidth(),
                        getHeight(),
                        Bitmap.Config.RGB_565);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCZPhotoView != null && mBitmap != null) {
            Paint drawBitmapPaint = new Paint();
            drawBitmapPaint.setAntiAlias(true);
            drawBitmapPaint.setFilterBitmap(true);

            canvas.drawColor(Color.WHITE);
            mBitmap.eraseColor(Color.WHITE);
            Canvas c = new Canvas(mBitmap);
            float scaleFactor = 2;
            c.translate(-mFocusX * scaleFactor + (getWidth() / 2),
                        -mFocusY * scaleFactor + (getHeight() / 2));
            c.scale(scaleFactor, scaleFactor);
            mCZPhotoView.draw(c);

            canvas.drawBitmap(mBitmap, 0, 0, drawBitmapPaint);
        }
    }

    public float getFocusX() {
        return mFocusX;
    }

    public void setFocusX(float mFocusX) {
        this.mFocusX = mFocusX;
    }

    public float getFocusY() {
        return mFocusY;
    }

    public void setFocusY(float mFocusY) {
        this.mFocusY = mFocusY;
    }
}
