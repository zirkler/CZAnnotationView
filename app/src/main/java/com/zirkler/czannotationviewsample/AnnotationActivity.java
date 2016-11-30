package com.zirkler.czannotationviewsample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class AnnotationActivity extends AppCompatActivity {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.annotation_activity);
        ButterKnife.bind(this);
        setupToolbar();

        mPhotoView = (CZPhotoView) findViewById(R.id.iv_photo);

        Picasso.with(this).load(R.drawable.background).into(mPhotoView, new Callback() {
            @Override
            public void onSuccess() {
                // The MAGIC happens here!
                mAttacher = new CZAttacher(mPhotoView);
                mPhotoView.attacher = mAttacher;

                // Set default drawing tool
                mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(AnnotationActivity.this, null));
            }

            @Override
            public void onError() {
                Log.e(AnnotationActivity.class.getSimpleName(), "Picasso Error occurred.");
            }
        });

        mPhotoView.setOnItemLongClickListener(new CZIItemLongClickListener() {
            @Override
            public void onItemLongClicked(CZIDrawingAction item, MotionEvent e) {
                Toast.makeText(AnnotationActivity.this, "Item got clicked", Toast.LENGTH_SHORT).show();
                item.moveStart();
            }
        });

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

        } else if (item.getItemId() == R.id.action_serialize) {
            FileOutputStream fos;
            try {
                String fileName = "serialized.txt";

                // write file
                fos = this.openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(mPhotoView.getDrawnActions());
                os.close();
                fos.close();

                // read file
                FileInputStream fis = this.openFileInput(fileName);
                ObjectInputStream is = new ObjectInputStream(fis);
                List<CZIDrawingAction> simpleClass = (List<CZIDrawingAction>) is.readObject();
                is.close();
                fis.close();

            } catch (Exception e) {
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                Log.i("asd", sw.toString());
                Toast.makeText(this, sw.toString(), Toast.LENGTH_LONG).show();
            }

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
        mPhotoView.saveToFile(this, "CZPhotoViewSerialized");
    }

    @OnClick(R.id.bttLoadFromFile)
    public void bttLoadFromFile() {
        mPhotoView.loadFromFile(this, mAttacher, "CZPhotoViewSerialized");
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
