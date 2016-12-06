package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class CZPhotoView extends PhotoView {

    private static final String INSTANCE_STATE = "cz_annotationview_instance_state";
    private static final String DRAWN_ACTIONS = "drawn_actions";
    private static final String SUPER_STATE = "super_state";
    private static final String BACKGROUND_TEMP_FILE_PATH = "czannotation_bg_tmp";
    private static final String BACKGROUND_TEMP_FILE_PATH_KEY = "bg_temp_";
    transient public CZAttacher attacher;
    transient public MagnifierView mMagnifierView;
    Matrix mConcatMatrix = new Matrix();
    private CZIDrawingAction mCurrentDrawingAction;
    private List<CZIDrawingAction> mDrawnActions = new ArrayList<>();
    private List<CZIDrawingAction> mRedoActions = new ArrayList<>();
    private Canvas mCacheCanvas;
    private Bitmap mForeground;
    private Paint mBitmapPaint;
    private RectF mInitialDisplayRect;
    private String mTempBackgroundImagePath;
    transient private Context mContext;
    private CZIItemLongClickListener mItemClickListener;

    public CZPhotoView(Context context) {
        super(context);
        setup(context);
    }

    public CZPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
        setup(context);
    }

    public CZPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        setup(context);
    }

    public String getmTempBackgroundImagePath() {
        return mTempBackgroundImagePath;
    }

    private void setup(Context context) {
        mContext = context;
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        // We wait until the layouting has finished, and then receive width and height of our view
        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mForeground = Bitmap.createBitmap(
                        getWidth(),
                        getHeight(),
                        Bitmap.Config.ARGB_8888);

                mCacheCanvas = new Canvas();
                mCacheCanvas.setBitmap(mForeground);
                mInitialDisplayRect = getDisplayRect();
            }
        });
    }

    /**
     * This method performs the actual drawing of the users drawn stuff.
     * @param canvas The canvas of the imageview / photoview.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear the cache canvas
        mCacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Here happens all the scaling and translation MAGIC!
        canvas.concat(mConcatMatrix);

        // Draw all the already drawn stuff to the canvas.
        for (int i = 0; i < mDrawnActions.size(); i++) {
            mDrawnActions.get(i).draw(mCacheCanvas, mInitialDisplayRect);
        }

        // Draw the path the user is currently drawing.
        if (mCurrentDrawingAction != null) {
            mCurrentDrawingAction.draw(mCacheCanvas, mInitialDisplayRect);
        }

        Paint drawBitmapPaint = new Paint();
        drawBitmapPaint.setAntiAlias(true);
        drawBitmapPaint.setFilterBitmap(true);
        canvas.drawBitmap(mForeground, 0, 0, drawBitmapPaint);

        mMagnifierView.setDisplayRect(mInitialDisplayRect);
        mMagnifierView.invalidate();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
                attacher.update();
                attacher.onScale(0, 0, 0); // just called to force rescaling of drawings
            }
        }, 1);
    }

    /**
     * Undoes the last drawn thing on the canvas.
     */
    public void undo() {
        if (mDrawnActions.size() > 0) {
            CZIDrawingAction lastAction = mDrawnActions.get(mDrawnActions.size() - 1);
            mDrawnActions.remove(lastAction);
            mRedoActions.add(lastAction);
            invalidate();
        }
    }

    /**
     * Redos the last undone action.
     */
    public void redo() {
        if (mRedoActions.size() > 0) {
            CZIDrawingAction lastUndoneAction = mRedoActions.get(mRedoActions.size() - 1);
            mRedoActions.remove(lastUndoneAction);
            mDrawnActions.add(lastUndoneAction);
            invalidate();
        }
    }

    public CZIDrawingAction getmCurrentDrawingAction() {
        return mCurrentDrawingAction;
    }

    public void setCurrentDrawingAction(CZIDrawingAction mCurrentDrawingAction) {
        this.mCurrentDrawingAction = mCurrentDrawingAction;
    }

    /**
     * Gets called from the CZAttacher. Adds the currently drawn item and clears the redo stack.
     */
    public void userFinishedDrawing() {
        mDrawnActions.add(mCurrentDrawingAction);
        mCurrentDrawingAction = mCurrentDrawingAction.createInstance(getContext(), null);
        mRedoActions.clear();
        invalidate();
    }

    /**
     * Gets called from the CZAttacher, i.e. when user lays down second finger while drawing.
     */
    public void cancelCurrentDrawingAction() {
        mCurrentDrawingAction = mCurrentDrawingAction.createInstance(getContext(), null);
        invalidate();
    }

    /**
     * @return Returns all the already drawn actions.
     */
    public List<CZIDrawingAction> getDrawnActions() {
        return mDrawnActions;
    }

    public void setmDrawnActions(List<CZIDrawingAction> mDrawnActions) {
        this.mDrawnActions = mDrawnActions;
    }

    /**
     * @return Returns the item click listener.
     */
    public CZIItemLongClickListener getItemClickListener() {
        return mItemClickListener;
    }

    /**
     * Set the item click listener
     * @param itemClickListener The to be set item click listener.
     */
    public void setOnItemLongClickListener(CZIItemLongClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    /**
     * Returns the initial display rect.
     * @return Initial display rect.
     */
    public RectF getmInitialDisplayRect() {
        return mInitialDisplayRect;
    }


    public void saveToFile(Context context, String filename) {
        try {
            CZStorageContainer container = new CZStorageContainer();
            container.drawnActions = getDrawnActions();
            container.bitmapBytes = bitmapToBytes(((BitmapDrawable) getDrawable()).getBitmap());

            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(container);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(Context context, CZAttacher attacher, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);

            CZStorageContainer container = (CZStorageContainer) is.readObject();
            mDrawnActions = container.drawnActions;
            Bitmap backgroundBitmap = bytesToBitmap(container.bitmapBytes);

            setImageBitmap(backgroundBitmap);
            attacher.update();
            invalidate();
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        attacher.onScale(0, 0, 0); // just called to force rescaling of drawings
    }

    public void setBackgroundPicture(Bitmap backgroundBitmap, CZAttacher attacher, Context context) {
        // Set image bitmap of this image view.
        setImageBitmap(backgroundBitmap);
        invalidate();
        attacher.update();
    }

    /**
     * Takes a bitmap and returns its byte without any loss. Method performs very bad for big bitmaps.
     * The bytes array can later be converted back to an bitmap by using the {@link #bytesToBitmap(byte[])} method.
     * @param bitmap The bitmap from which you want the bytes.
     * @return Returns a bytes array which represents the given bitmap 1:1.
     */
    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Converts an array of bytes into an bitmap. The array of bytes should be created by the
     * {@link #bitmapToBytes(Bitmap)} method.
     * @param bytes The byte array representation of the bitmap
     * @return Returns the Bitmap of the corresponding byte array.
     */
    private Bitmap bytesToBitmap(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.length);
        return bitmap;
    }
}
