package com.zirkler.czannotationviewsample;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
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
import com.zirkler.czannotationviewsample.AnnotationView.CZUndoRedoAction;
import com.zirkler.czannotationviewsample.AnnotationView.MagnifierView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.EasyPermissions;


public class AnnotationFragment extends Fragment implements CZItemLongClickListener, CZItemShortClickListener, CZItemSelectionChangeListener {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    public static final int EXTERNAL_STORAGE_WRITE_PERMISSION = 101;
    private static final int PDF_PICK_RC = 102;

    @BindView(R.id.bttDeleteItem) Button mBttDelete;
    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;
    private String mFileName;
    private MagnifierView mMagnifierView;
    private View mView;

    public AnnotationFragment() {
        // Required empty public constructor
    }

    public static AnnotationFragment createInstance(String fileName) {
        AnnotationFragment annotationFragment = new AnnotationFragment();
        annotationFragment.mFileName = fileName;
        return annotationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.annotation_fragment, container, false);
        mView = v;
        ButterKnife.bind(this, v);
        setHasOptionsMenu(true);
        setup(mFileName);
        return v;
    }

    public void setup(String fileName) {
        mPhotoView = (CZPhotoView) mView.findViewById(R.id.iv_photo);
        mAttacher = new CZAttacher(mPhotoView);
        mPhotoView.attacher = mAttacher;

        // Set default drawing tool
        mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(getContext(), null));

        // Set event listeners
        mPhotoView.setItemLongClickListener(this);
        mPhotoView.setItemShortClickListener(this);
        mPhotoView.setItemSelectionChangeListener(this);

        // Receive the drawing db object and check if a saved file of this drawing already exists
        //Drawing drawing = (Drawing) getIntent().getSerializableExtra(MainActivity.DRAWING_KEY);
        mFileName = fileName;
        File file = new File(getContext().getFilesDir() + "/" + mFileName);

        try {
            // If there is a file for this drawing, load it
            if (file.exists()) {
                mPhotoView.loadFromFile(getContext(), mAttacher, mFileName);
            } else {
                // Otherwise set the default background
                Bitmap defaultBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                mPhotoView.setBackgroundPicture(defaultBackground, mAttacher, getContext(), mFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup magnifier view, size is always 1/4 of the views width
        mMagnifierView = (MagnifierView) mView.findViewById(R.id.magnifierView);
        mMagnifierView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMagnifierView.getLayoutParams().width  = mView.getWidth() / 4;
                mMagnifierView.getLayoutParams().height = mView.getHeight() / 4;
                mMagnifierView.mCZPhotoView = mPhotoView;
                mPhotoView.setMagnifierView(mMagnifierView);
            }
        });

        // Ask for permission for writing to external storage (needed for exporting to gallery)
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.annotation_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        } else if (item.getItemId() == R.id.action_freehand_drawing) {
            changeTool(new CZDrawingActionFreehand(getContext(), null));
        } else if (item.getItemId() == R.id.action_eraser) {
            changeTool(new CZDrawingActionEraser(getContext(), null));
        } else if (item.getItemId() == R.id.action_pick_background) {
            EasyImage.openChooserWithDocuments(this, "Choose Background Image", 0);
        } else if (item.getItemId() == R.id.action_pick_background_fromPDF) {
            new FileChooserDialog.Builder((AnnotationActivity)getActivity())
                    .initialPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())  // changes initial path, defaults to external storage directory
                    .extensionsFilter(".pdf")
                    .tag("optional-identifier")
                    .show();
        } else if (item.getItemId() == R.id.action_line) {
            changeTool(new CZDrawingActionLine(getContext(), null));
        } else if (item.getItemId() == R.id.action_text) {
            // Ask user for text
            new MaterialDialog.Builder(getContext())
                    .title("Enter text")
                    .content("Text Please.")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                    .input("Text", "My Annotation Text", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            changeTool(new CZDrawingActionText(getContext(), null, input.toString()));
                            mPhotoView.getCurrentDrawingAction().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
                            mPhotoView.userFinishedDrawing();
                            mAttacher.setCurrentState(CZAttacher.CZState.READY_TO_DRAW);
                        }
                    }).show();
        } else if (item.getItemId() == R.id.action_export) {
            // Export image and open in gallery, then open gallery (or do something different, put in an email or stuff)
            String imagePath = mPhotoView.exportToGallery(getContext());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                try {
                    // Automatically correctly rotate the taken image
                    ExifInterface exif = new ExifInterface(imageFile.getPath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        angle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        angle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        angle = 270;
                    }

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;

                    // Load the original captured image as bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);

                    // Actually rotate the bitmap
                    Bitmap rotatetBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                    mPhotoView.setBackgroundPicture(rotatetBitmap, mAttacher, getContext(), mFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAttacher.getSelectedItem() != null) {
            mAttacher.getSelectedItem().setActionState(CZIDrawingAction.CZDrawingActionState.ITEM_DRAWN);
            mAttacher.setSelectedItem(null);
        }
        try {
            mPhotoView.saveToFile(getContext(), mFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        mAttacher.cleanup();
    }

    @OnClick(R.id.bttDeleteItem)
    public void bttDeleteClicked() {
        // Delete the item
        CZIDrawingAction selectedItem = mAttacher.getSelectedItem();
        mPhotoView.deleteItem(selectedItem);

        // Hide delete button
        mBttDelete.setVisibility(View.GONE);
    }

    @Override
    public void onItemLongClicked(CZIDrawingAction item, MotionEvent event) {
    }

    @Override
    public void onItemShortClicked(CZIDrawingAction item, MotionEvent event) {
        // If user clicks a text view, show him dialog to edit the text.
        if (item instanceof CZDrawingActionText) {
            final CZDrawingActionText textItem = (CZDrawingActionText) item;
            new MaterialDialog.Builder(getContext())
                    .title("Change text")
                    .content("Text Please.")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                    .input("Text", textItem.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            CZUndoRedoAction action = textItem.setText(input.toString());
                            mPhotoView.addRedoableAction(action);
                        }
                    }).show();
        }
    }

    @Override
    public void onItemSelectionChanged(CZIDrawingAction newSelectedItem, CZIDrawingAction prevSelectedItem, MotionEvent event) {
        if (newSelectedItem == null) {
            // Hide the delete button if there is nothing selected to delete.
            mBttDelete.setVisibility(View.GONE);
        } else {
            // Show the delete button if user has something selected
            mBttDelete.setVisibility(View.VISIBLE);
        }
    }

    public void setBackgroundPicture(Bitmap bitmap, String mFileName) throws IOException {
        mPhotoView.setBackgroundPicture(bitmap, mAttacher, getContext(), mFileName);
    }
}
