package com.zirkler.czannotationviewsample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zirkler.czannotationviewsample.AnnotationView.CZAttacher;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZDrawingActionEraser;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZDrawingActionFreehand;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZDrawingActionLine;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZDrawingActionText;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActions.CZIDrawingAction;
import com.zirkler.czannotationviewsample.AnnotationView.CZItemLongClickListener;
import com.zirkler.czannotationviewsample.AnnotationView.CZItemSelectionChangeListener;
import com.zirkler.czannotationviewsample.AnnotationView.CZItemShortClickListener;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;
import com.zirkler.czannotationviewsample.AnnotationView.MagnifierView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

public class AnnotationActivity extends AppCompatActivity implements CZItemShortClickListener, CZItemLongClickListener, CZItemSelectionChangeListener {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    public static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 101;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.bttDeleteItem) Button mBttDelete;
    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;
    private String mFileName;
    private MagnifierView mMagnifierView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.annotation_activity);
        ButterKnife.bind(this);
        setupToolbar();

        mPhotoView = (CZPhotoView) findViewById(R.id.iv_photo);
        mAttacher = new CZAttacher(mPhotoView);
        mPhotoView.attacher = mAttacher;

        // Set default drawing tool
        mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(AnnotationActivity.this, null));

        // Set event listeners
        mPhotoView.setItemLongClickListener(this);
        mPhotoView.setItemShortClickListener(this);
        mPhotoView.setItemSelectionChangeListener(this);

        // Receive the drawing db object and check if a saved file of this drawing already exists
        Drawing drawing = (Drawing) getIntent().getSerializableExtra(MainActivity.DRAWING_KEY);
        mFileName = drawing.getDrawingTitle();
        File file = new File(getFilesDir() + "/" + mFileName);

        try {
            // If there is a file for this drawing, load it
            if (file.exists()) {
                mPhotoView.loadFromFile(this, mAttacher, mFileName);
            } else {
                // Otherwise set the default background
                Bitmap defaultBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                mPhotoView.setBackgroundPicture(defaultBackground, mAttacher, this, mFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup magnifier view, size is always 1/4 of the views width
        mMagnifierView = (MagnifierView) findViewById(R.id.magnifierView);
        mMagnifierView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMagnifierView.getLayoutParams().width  = getWindow().getDecorView().getWidth() / 4;
                mMagnifierView.getLayoutParams().height = getWindow().getDecorView().getWidth() / 4;
                mMagnifierView.mCZPhotoView = mPhotoView;
                mPhotoView.setMagnifierView(mMagnifierView);
            }
        });

        // Ask for permission for writing to external storage (needed for exporting to gallery)
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    this,
                    "Please issue permissions for saving to gallery!",
                    EXTERNAL_STORAGE_WRITE_PERMISSION,
                    perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.annotation_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_freehand_drawing) {
            changeTool(new CZDrawingActionFreehand(this, null));
        } else if (item.getItemId() == R.id.action_eraser) {
            changeTool(new CZDrawingActionEraser(this, null));
        } else if (item.getItemId() == R.id.action_pick_background) {
            EasyImage.openChooserWithDocuments(this, "Choose Background Image", 0);
        } else if (item.getItemId() == R.id.action_line) {
            changeTool(new CZDrawingActionLine(this, null));
        } else if (item.getItemId() == R.id.action_text) {
            // Ask user for text
            new MaterialDialog.Builder(this)
                    .title("Enter text")
                    .content("Text Please.")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                    .input("Text", "My Annotation Text", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            changeTool(new CZDrawingActionText(AnnotationActivity.this, null, input.toString()));
                            mPhotoView.getCurrentDrawingAction().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                            mPhotoView.userFinishedDrawing();
                            mAttacher.setCurrentState(CZAttacher.CZState.READY_TO_DRAW);
                        }
                    }).show();



        } else if (item.getItemId() == R.id.action_export) {
            // Export image and open in gallery, then open gallery (or do something different, put in an email or stuff)
            String imagePath = mPhotoView.exportAsJpg(this);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(imagePath), "image/*");
            startActivity(intent);
        }
        return true;
    }

    public void changeTool(CZIDrawingAction newAction) {
        if (mAttacher.getSelectedItem() != null) {
            mAttacher.getSelectedItem().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mAttacher.setSelectedItem(null);
        }
        mPhotoView.setCurrentDrawingAction(newAction);
        mAttacher.setCurrentState(CZAttacher.CZState.READY_TO_DRAW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                try {
                    mPhotoView.setBackgroundPicture(bitmap, mAttacher, AnnotationActivity.this, mFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.bttUndo)
    public void bttUndoClicked() {
        mPhotoView.undo();
    }

    @OnClick(R.id.bttRedo)
    public void bttRedoClicked() {
        mPhotoView.redo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAttacher.cleanup();
    }

    @Override
    protected void onStop() {
        if (mAttacher.getSelectedItem() != null) {
            mAttacher.getSelectedItem().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mAttacher.setSelectedItem(null);
        }
        try {
            mPhotoView.saveToFile(this, mFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    private void setupToolbar() {
        setTitle("Annotation View");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    public void onItemLongClicked(CZIDrawingAction item, MotionEvent event) {
    }

    @Override
    public void onItemShortClicked(CZIDrawingAction item, MotionEvent event) {
        // If user clicks a text view, show him dialog to edit the text.
        if (item instanceof CZDrawingActionText) {
            final CZDrawingActionText textItem = (CZDrawingActionText) item;
            new MaterialDialog.Builder(AnnotationActivity.this)
                    .title("Change text")
                    .content("Text Please.")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                    .input("Text", textItem.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            textItem.setText(input.toString());
                        }
                    }).show();
        }
    }

    private void showDeleteIcon() {

    }

    @OnClick(R.id.bttDeleteItem)
    public void bttDeleteClicked() {
        // Delete the item
        CZIDrawingAction selectedItem = mAttacher.getSelectedItem();
        mPhotoView.deleteItem(selectedItem);

        // Hide delete button
        mBttDelete.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemSelectionChanged(CZIDrawingAction newSelectedItem, CZIDrawingAction prevSelectedItem, MotionEvent event) {
        if (newSelectedItem == null) {
            // Hide the delete button if there is nothing selected to delete.
            mBttDelete.setVisibility(View.INVISIBLE);
        } else {
            mBttDelete.setVisibility(View.VISIBLE);
            // Show the delete button if user has something selected
        }
    }
}
