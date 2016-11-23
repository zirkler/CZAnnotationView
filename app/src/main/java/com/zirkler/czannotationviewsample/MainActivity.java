package com.zirkler.czannotationviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zirkler.czannotationviewsample.AnnotationView.CZAttacher;
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
                mAttacher.update();
            }

            @Override
            public void onError() {
                Log.i("asd", "error");
            }
        });

        // The MAGIC happens here!
        mAttacher = new CZAttacher(mPhotoView);
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
