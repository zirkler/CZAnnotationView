package com.zirkler.czannotationviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zirkler.czannotationviewsample.AnnotationView.CZAttacher;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionEraser;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionFreehand;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPhotoView = (CZPhotoView) findViewById(R.id.iv_photo);
        Picasso.with(this).load(R.drawable.img_3mb).into(mPhotoView, new Callback() {
            @Override
            public void onSuccess() {
                // The MAGIC happens here!
                mAttacher = new CZAttacher(mPhotoView);

                // set default paint tool
                mPhotoView.setmCurrentDrawingAction(new CZDrawingActionFreehand(MainActivity.this, null));
            }

            @Override
            public void onError() {
                Log.i("asd", "error");
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_freehand_drawing) {
            mPhotoView.setmCurrentDrawingAction(new CZDrawingActionFreehand(this, null));
        } else if (item.getItemId() == R.id.action_eraser) {
            mPhotoView.setmCurrentDrawingAction(new CZDrawingActionEraser(this, null));
        } else if (item.getItemId() == R.id.action_pick_background) {

        } else if (item.getItemId() == R.id.action_measurement_line) {

        } else if (item.getItemId() == R.id.action_serialize) {

        }
        return true;
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
}
