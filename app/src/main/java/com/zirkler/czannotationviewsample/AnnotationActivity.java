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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zirkler.czannotationviewsample.AnnotationView.CZAttacher;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionEraser;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionFreehand;
import com.zirkler.czannotationviewsample.AnnotationView.CZIDrawingAction;
import com.zirkler.czannotationviewsample.AnnotationView.CZIItemLongClickListener;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;
import com.zirkler.czannotationviewsample.AnnotationView.MagnifierView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;

public class AnnotationActivity extends AppCompatActivity {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    public static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 101;

    @BindView(R.id.toolbar) Toolbar mToolbar;
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

        // Set onItemLongClickListener
        mPhotoView.setOnItemLongClickListener(new CZIItemLongClickListener() {
            @Override
            public void onItemLongClicked(CZIDrawingAction item, MotionEvent e) {
                Toast.makeText(AnnotationActivity.this, "Item got clicked", Toast.LENGTH_SHORT).show();
                item.moveStart();
            }
        });

        // Receive the drawing db object and check if a saved file of this drawing already exists
        Drawing drawing = (Drawing) getIntent().getSerializableExtra(MainActivity.DRAWING_KEY);
        mFileName = drawing.getDrawingTitle();
        File file = new File(getFilesDir() + "/" + mFileName);

        if (file.exists()) {
            // load the saved file
            mPhotoView.loadFromFile(this, mAttacher, mFileName);
        } else {
            // load the default background into the view
            Picasso.with(this).load(R.drawable.background).into(mPhotoView, new Callback() {
                @Override
                public void onSuccess() {
                    mAttacher.update();
                }

                @Override
                public void onError() {
                    Log.e(AnnotationActivity.class.getSimpleName(), "Picasso Error occurred.");
                }
            });
        }

        // Setup magnifier view
        mMagnifierView = (MagnifierView) findViewById(R.id.magnifierView);
        mMagnifierView.mCZPhotoView = mPhotoView;
        mPhotoView.mMagnifierView = mMagnifierView;

        // Ask for permission for writing to external storage (needed for exporting to gallery)
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.annotation_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_freehand_drawing) {
            mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(this, null));
        } else if (item.getItemId() == R.id.action_eraser) {
            mPhotoView.setCurrentDrawingAction(new CZDrawingActionEraser(this, null));
        } else if (item.getItemId() == R.id.action_pick_background) {
            EasyImage.openChooserWithDocuments(this, "Choose Background Image", 0);
        } else if (item.getItemId() == R.id.action_measurement_line) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                mPhotoView.setBackgroundPicture(bitmap, mAttacher, AnnotationActivity.this);
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

    @OnClick(R.id.bttSaveToFile)
    public void bttSaveToFileClicked() {
        mPhotoView.saveToFile(this, mFileName);
    }

    @OnClick(R.id.bttLoadFromFile)
    public void bttLoadFromFile() {
        mPhotoView.loadFromFile(this, mAttacher, mFileName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAttacher.cleanup();
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
}
