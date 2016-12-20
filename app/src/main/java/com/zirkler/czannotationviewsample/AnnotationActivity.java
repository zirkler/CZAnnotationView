package com.zirkler.czannotationviewsample;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnnotationActivity extends AppCompatActivity implements FileChooserDialog.FileCallback {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    AnnotationFragment mAnnotationFragment;
    String mFileName;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.annotation_activity);
        ButterKnife.bind(this);
        setupToolbar();

        // Get the Fragment itself
        mAnnotationFragment = (AnnotationFragment) getSupportFragmentManager().findFragmentById(R.id.annotationFragment);

        // Receive the drawing db object and check if a saved file of this drawing already exists
        Drawing drawing = (Drawing) getIntent().getSerializableExtra(MainActivity.DRAWING_KEY);
        mFileName = drawing.getDrawingTitle();

        // Setup the annotation fragment
        mAnnotationFragment.setup(mFileName);
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
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        try {
            PdfRenderer renderer = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));

                PdfRenderer.Page page = renderer.openPage(0);
                int width = page.getWidth();
                int height = page.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();

                mAnnotationFragment.setBackgroundPicture(bitmap, mFileName);

            } else {
                Toast.makeText(this, "Sorry, PDF Import only support since Android " + Build.VERSION_CODES.LOLLIPOP, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
