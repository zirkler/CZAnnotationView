package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CZPhotoView extends PhotoView {

    private static final String INSTANCE_STATE = "cz_annotationview_instance_state";
    private static final String DRAWN_ACTIONS = "drawn_actions";
    private static final String SUPER_STATE = "super_state";
    private static final String BACKGROUND_TEMP_FILE_PATH = "czannotation_bg_tmp";
    private static final String BACKGROUND_TEMP_FILE_PATH_KEY = "bg_temp_";
    transient public CZAttacher attacher;

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
     * Saves the instance state, so the library user does not have to implement this himself.
     * @return Parceable state.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle outState = new Bundle();

        outState.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        outState.putSerializable(DRAWN_ACTIONS, (Serializable) mDrawnActions);
        outState.putString(BACKGROUND_TEMP_FILE_PATH_KEY, mTempBackgroundImagePath);

        // Save the background image to temp directory.
        /*
        FileOutputStream outputStream = null;
        try {
            File outputDir = mContext.getCacheDir();
            File outputFile = File.createTempFile(BACKGROUND_TEMP_FILE_PATH, "png", outputDir);
            outputStream = new FileOutputStream(outputFile);
            Bitmap backgroundBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            backgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            mTempBackgroundImagePath = outputFile.getPath();
            outState.putString(BACKGROUND_TEMP_FILE_PATH_KEY, mTempBackgroundImagePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/

        return outState;
    }


    /**
     * Restores the instance state, so the library user does not have to implement this himself.
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Parcelable) {
            Bundle inState = (Bundle) state;

            // Restore drawn items
            if (inState != null && inState.containsKey(DRAWN_ACTIONS)) {
                List<CZIDrawingAction> drawnActions = (List<CZIDrawingAction>)inState.getSerializable(DRAWN_ACTIONS);
                this.mDrawnActions = drawnActions;
                invalidate();

                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                String bitmapPath = inState.getString(BACKGROUND_TEMP_FILE_PATH_KEY);
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
                setImageBitmap(bitmap);
                attacher.update();*/
            }

            state = inState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(state);
        }
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
    }

    public void setBackgroundPicture(Bitmap backgroundBitmap, CZAttacher attacher, Context context) {
        // Set image bitmap of this image view.
        setImageBitmap(backgroundBitmap);
        invalidate();
        attacher.update();
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap bytesToBitmap(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.length);
        return bitmap;
    }
}
