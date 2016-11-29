package com.zirkler.czannotationviewsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zirkler.czannotationviewsample.AnnotationView.CZAttacher;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionEraser;
import com.zirkler.czannotationviewsample.AnnotationView.CZDrawingActionFreehand;
import com.zirkler.czannotationviewsample.AnnotationView.CZIDrawingAction;
import com.zirkler.czannotationviewsample.AnnotationView.CZPhotoView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String DRAWN_ACTIONS = "drawn_actions";
    public static final String MATRIX_VALUES = "matrix_values";
    public static final String PREV_DISPLAY_RECT_LEFT = "prev_display_rect_left";
    public static final String PREV_DISPLAY_RECT_TOP = "prev_display_rect_top";

    private CZAttacher mAttacher;
    private CZPhotoView mPhotoView;
    private float mInitialDisplayRectLeft;
    private float mInitialDisplayRectTop;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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


                mInitialDisplayRectLeft = mAttacher.getDisplayRect().left;
                mInitialDisplayRectTop = mAttacher.getDisplayRect().top;

                // restore drawn stuff
                if (savedInstanceState != null && savedInstanceState.containsKey(DRAWN_ACTIONS)) {
                    List<CZIDrawingAction> drawnActions = (List<CZIDrawingAction>)savedInstanceState.getSerializable(DRAWN_ACTIONS);

                    // If user performed an orientation change, we have to adjust the x and y values of the drawnActions
                    float prevLeftOffset = savedInstanceState.getFloat(PREV_DISPLAY_RECT_LEFT);
                    float prevTopOffset = savedInstanceState.getFloat(PREV_DISPLAY_RECT_TOP);

                    mPhotoView.setmDrawnActions(drawnActions);
                    mPhotoView.invalidate();
                }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DRAWN_ACTIONS, (Serializable) mPhotoView.getDrawnActions());

        // Also store initial display rect offsets
        outState.putFloat(PREV_DISPLAY_RECT_LEFT, mInitialDisplayRectLeft);
        outState.putFloat(PREV_DISPLAY_RECT_TOP, mInitialDisplayRectTop);
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
