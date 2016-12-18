package com.zirkler.czannotationviewsample.AnnotationView;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class CZPhotoView extends PhotoView {

    transient public CZAttacher attacher;
    public List<CZUndoRedoAction> mActionHistory = new ArrayList<>();
    public List<CZUndoRedoAction> mActionRedoHistory = new ArrayList<>();
    protected List<CZIDrawingAction> mDrawnActions = new ArrayList<>();
    Matrix mConcatMatrix = new Matrix();
    transient private MagnifierView mMagnifierView;
    private CZIDrawingAction mCurrentDrawingAction;
    private List<CZIDrawingAction> mRedoActions = new ArrayList<>();
    private Canvas mCacheCanvas;
    private Bitmap mForeground;
    private RectF mInitialDisplayRect;
    private Paint mDrawBitmapPaint;
    private Context mContext;

    // Listeners
    private CZItemLongClickListener mItemLongClickListener;
    private CZItemShortClickListener mItemShortClickListener;
    private CZItemSelectionChangeListener mItemSelectionChangeListener;

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

    public static double pointToSegmentDistance(PointF segA, PointF segB, PointF p) {
        PointF p2 = new PointF(segB.x - segA.x, segB.y - segA.y);
        float something = p2.x * p2.x + p2.y * p2.y;
        float u = ((p.x - segA.x) * p2.x + (p.y - segA.y) * p2.y) / something;
        if (u > 1)      u = 1;
        else if (u < 0) u = 0;
        float x = segA.x + u * p2.x;
        float y = segA.y + u * p2.y;
        float dx = x - p.x;
        float dy = y - p.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist;
    }

    private void setup(Context context) {
        mContext = context;


        mDrawBitmapPaint = new Paint();
        mDrawBitmapPaint.setAntiAlias(true);
        mDrawBitmapPaint.setFilterBitmap(true);

        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mForeground == null) {
                    mForeground = Bitmap.createBitmap(
                            getWidth(),
                            getHeight(),
                            Bitmap.Config.ARGB_8888);

                    mCacheCanvas = new Canvas();
                    mCacheCanvas.setBitmap(mForeground);
                    mInitialDisplayRect = getDisplayRect();
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i("asd", String.format("onSizeChanged(int w %d, int h %d, int oldw %d, int oldh %d)", w, h, oldw, oldh));
        invalidate();
    }


    /**
     * This method performs the actual drawing of the users drawn stuff.
     * @param canvas The canvas of the imageview / photoview.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // We can't do anything until the displayRect is available
        if (mInitialDisplayRect == null) return;

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

        canvas.drawBitmap(mForeground, 0, 0, mDrawBitmapPaint);

        mMagnifierView.invalidate();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                attacher.update();
                attacher.onScale(0, 0, 0);
            }
        }, 1);
    }

    /**
     * Undoes the last drawn thing on the canvas.
     */
    public void undo() {
        if (mActionHistory.size() >  0) {
            CZUndoRedoAction action = mActionHistory.get(mActionHistory.size() - 1);
            action.undo();
            mActionHistory.remove(action);
            mActionRedoHistory.add(action);
        }
        invalidate();
    }

    /**
     * Redos the last undone action.
     */
    public void redo() {
        if (mActionRedoHistory.size() > 0) {
            CZUndoRedoAction action = mActionRedoHistory.get(mActionRedoHistory.size() - 1);
            action.redo();
            mActionRedoHistory.remove(action);
            mActionHistory.add(action);
        }
        invalidate();
    }

    public CZIDrawingAction getCurrentDrawingAction() {
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

        // add the create action
        ActionAddDrawingItem actionAddDrawingItem = new ActionAddDrawingItem(mDrawnActions.get(mDrawnActions.size() - 1));
        addRedoableAction(actionAddDrawingItem);
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
     * Deletes an item from the drawing.
     * @param item The to be deleted item.
     */
    public void deleteItem(CZIDrawingAction item) {
        getDrawnActions().remove(item);
        item.setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
        attacher.setSelectedItem(null);
        attacher.setCurrentState(CZAttacher.CZState.READY_TO_DRAW);

        // Add the remove action
        ActionRemoveDrawingItem removeAction = new ActionRemoveDrawingItem(item);
        addRedoableAction(removeAction);
        invalidate();
    }

    /**
     * Returns the initial display rect.
     * @return Initial display rect.
     */
    public RectF getInitialDisplayRect() {
        return mInitialDisplayRect;
    }

    /**
     * Saves the drawn items to disk.
     * This does not save the background picture,
     * background picture gets saved by {@link #setBackgroundPicture(Bitmap, CZAttacher, Context, String)} automatically when setting it.
     * @param context Context.
     * @param filename Filename.
     * @throws IOException
     */
    public void saveToFile(Context context, String filename) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(mDrawnActions);
        os.close();
        fos.close();
    }

    /**
     * Loads drawn items and the background picture into the CZDrawingView.
     * @param context Context.
     * @param attacher CZAttacher.
     * @param filename Filename.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadFromFile(Context context, CZAttacher attacher, String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(filename);
        ObjectInputStream is = new ObjectInputStream(fis);

        // Load the drawn actions.
        mDrawnActions = (List<CZIDrawingAction>) is.readObject();

        // load the background image
        fis = context.openFileInput(filename + "_image");
        is = new ObjectInputStream(fis);
        byte[] bitmapBytes = (byte[]) is.readObject();
        Bitmap backgroundBitmap = bytesToBitmap(bitmapBytes);
        setImageBitmap(backgroundBitmap);

        attacher.update();
        invalidate();
        is.close();
        fis.close();

        attacher.onScale(0, 0, 0); // just called to force rescaling of drawings
    }

    /**
     * Sets the background image of the component.
     * Also updates PhotoView attacher and saves the image to the device disk.
     * @param backgroundBitmap
     * @param attacher
     * @param context
     */
    public void setBackgroundPicture(Bitmap backgroundBitmap, CZAttacher attacher, Context context, String filename) throws IOException {
        // Deselect an eventually selected item
        if (attacher.getSelectedItem() != null) {
            attacher.getSelectedItem().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            attacher.setSelectedItem(null);
        }

        // Save the background picture on the device
        FileOutputStream fos = context.openFileOutput(filename + "_image", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(bitmapToBytes(backgroundBitmap));
        os.close();
        fos.close();

        // set it to the CZPhotoView
        setImageBitmap(backgroundBitmap);
        invalidate();
        attacher.update();
        attacher.onScale(0, 0, 0); // just called to force rescaling of drawings
    }

    /**
     * Takes a bitmap and returns its byte without any loss. Method performs very bad for big bitmaps.
     * The bytes array can later be converted back to an bitmap by using the {@link #bytesToBitmap(byte[])} method.
     * @param bitmap The bitmap from which you want the bytes.
     * @return Returns a bytes array which represents the given bitmap 1:1.
     */
    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
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

    /**
     * Exports the entire image in the image's resolution to the android gallery.
     * @param context The CZPhotoView hosting activity.
     * @return Returns the path of the image file or null if this method failed.
     */
    public String exportAsJpg(Context context) {
        String imagePath = null;
        Bitmap backgroundFullSize = ((BitmapDrawable) getDrawable()).getBitmap();
        Bitmap exportBitmap = backgroundFullSize.copy(Bitmap.Config.ARGB_8888, true);
        Canvas exportCanvas = new Canvas(exportBitmap);

        // Full resolution display rect
        RectF displayRect = new RectF(0, 0,
                                    (float)exportBitmap.getWidth(),
                                    (float)exportBitmap.getHeight());

        // Foreground bitmap, we gonna draw the users stuff on it
        Bitmap foreground = Bitmap.createBitmap(
                                    exportBitmap.getWidth(),
                                    exportBitmap.getHeight(),
                                    Bitmap.Config.ARGB_8888);
        Canvas foregroundCanvas = new Canvas();
        foregroundCanvas.setBitmap(foreground);

        Log.i("export", String.format("Width: %d, Height: %d", exportBitmap.getWidth(), exportBitmap.getHeight()));


        // Draw all the already drawn stuff to the canvas.
        for (int i = 0; i < mDrawnActions.size(); i++) {
            mDrawnActions.get(i).draw(foregroundCanvas, displayRect);
        }

        // Just define a bitmap drawing paint
        Paint drawBitmapPaint = new Paint();
        drawBitmapPaint.setAntiAlias(true);
        drawBitmapPaint.setFilterBitmap(true);

        // Draw the foreground to the background
        exportCanvas.drawBitmap(foreground, 0, 0, drawBitmapPaint);

        // Write export bitmap jpg png to gallery
        imagePath = SKPhotoUtils.insertImage(context.getContentResolver(), exportBitmap, "", "");

        return imagePath;
    }

    public MagnifierView getMagnifierView() {
        return mMagnifierView;
    }

    public void setMagnifierView(MagnifierView mMagnifierView) {
        this.mMagnifierView = mMagnifierView;
    }

    /**
     * @return Returns the item long click listener.
     */
    public CZItemLongClickListener getItemLongClickListener() {
        return mItemLongClickListener;
    }

    /**
     * Set the item long click listener
     * @param itemLongClickListener The to be set listener.
     */
    public void setItemLongClickListener(CZItemLongClickListener itemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener;
    }

    /**
     * @return Returns the item short click listener.
     */
    public CZItemShortClickListener getItemShortClickListener() {
        return mItemShortClickListener;
    }

    /**
     * Sets the item short click listener.
     * @param mItemShortClickListener The to be set listener.
     */
    public void setItemShortClickListener(CZItemShortClickListener mItemShortClickListener) {
        this.mItemShortClickListener = mItemShortClickListener;
    }

    public CZItemSelectionChangeListener getItemSelectionChangeListener() {
        return mItemSelectionChangeListener;
    }

    public void setItemSelectionChangeListener(CZItemSelectionChangeListener mItemSelectionChangeListener) {
        this.mItemSelectionChangeListener = mItemSelectionChangeListener;
    }

    /**
     * Adds redoable action, keeps track of action history index.
     * @param action The to be added action.
     */
    public void addRedoableAction(CZUndoRedoAction action) {
        mActionRedoHistory.clear();
        mActionHistory.add(action);
        invalidate();
    }

    public class ActionAddDrawingItem implements CZUndoRedoAction {

        CZIDrawingAction mDrawingItem;

        ActionAddDrawingItem(CZIDrawingAction drawingItem) {
            mDrawingItem = drawingItem;
        }

        @Override
        public void undo() {
            // Remove the item.
            mDrawnActions.remove(mDrawingItem);
            invalidate();
        }

        @Override
        public void redo() {
            // Add the item back again.
            mDrawnActions.add(mDrawingItem);
            invalidate();
        }
    }

    public class ActionRemoveDrawingItem implements CZUndoRedoAction {

        CZIDrawingAction mDrawingItem;

        ActionRemoveDrawingItem(CZIDrawingAction drawingItem) {
            mDrawingItem = drawingItem;
        }

        @Override
        public void undo() {
            // Remove the item.
            mDrawnActions.add(mDrawingItem);
            invalidate();
        }

        @Override
        public void redo() {
            // Add the item back again.
            mDrawnActions.remove(mDrawingItem);

            invalidate();
        }
    }

}
