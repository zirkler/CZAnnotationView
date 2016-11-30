package com.zirkler.czannotationviewsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPhotoView = (CZPhotoView) findViewById(R.id.iv_photo);
        Picasso.with(this).load(R.drawable.ffuf_office).into(mPhotoView, new Callback() {
            @Override
            public void onSuccess() {
                // The MAGIC happens here!
                mAttacher = new CZAttacher(mPhotoView);

                // set default paint tool
                mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(MainActivity.this, null));
            }

            @Override
            public void onError() {
                Log.e(MainActivity.class.getSimpleName(), "Picasso Error occurred.");
            }
        });

        mPhotoView.setOnItemLongClickListener(new CZIItemLongClickListener() {
            @Override
            public void onItemLongClicked(CZIDrawingAction item, MotionEvent e) {
                Toast.makeText(MainActivity.this, "Item got clicked", Toast.LENGTH_SHORT).show();
                item.moveStart();
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
            mPhotoView.setCurrentDrawingAction(new CZDrawingActionFreehand(this, null));
        } else if (item.getItemId() == R.id.action_eraser) {
            mPhotoView.setCurrentDrawingAction(new CZDrawingActionEraser(this, null));
        } else if (item.getItemId() == R.id.action_pick_background) {

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
