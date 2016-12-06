package com.zirkler.czannotationviewsample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String DRAWING_KEY = "drawing";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.appbarLayout) AppBarLayout mAppbarLayout;
    List<Drawing> mDrawings;
    private DrawingsAdapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        setupToolbar();
        setupRecyclerView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addDrawing();

        }
        return true;
    }

    private void addDrawing() {
        new MaterialDialog.Builder(this)
                .title("New Drawing")
                .content("Enter name of the new drawing.")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Name", "My Awsome Drawing", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Create the drawing as database object
                        Log.i("asd", input.toString());
                        String inputSanitized = input.toString().replace(" ", "").trim();
                        Drawing newDrawing = new Drawing();
                        newDrawing.setDrawingTitle(inputSanitized);
                        newDrawing.save();
                        dialog.hide();

                        // Go to the new drawing
                        Intent intent = new Intent(MainActivity.this, AnnotationActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }

    private void setupToolbar() {
        setTitle("CZAnnotationView Example Usage");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    private void setupRecyclerView() {
        mDrawings = new Select().all().from(Drawing.class).execute();
        mRecyclerAdapter = new DrawingsAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public class DrawingItemHolder extends RecyclerView.ViewHolder {

        View mItemView;
        @BindView(R.id.txtIndex) TextView mTxtIndex;

        public DrawingItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItemView = itemView;
        }

        public void bind(final Drawing drawing) {
            mTxtIndex.setText(drawing.getDrawingTitle());
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AnnotationActivity.class);
                    intent.putExtra(DRAWING_KEY, drawing);
                    startActivity(intent);
                }
            });
        }
    }

    public class DrawingsAdapter extends RecyclerView.Adapter<DrawingItemHolder> {

        @Override
        public DrawingItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            return new DrawingItemHolder(view);
        }

        @Override
        public void onBindViewHolder(DrawingItemHolder holder, int position) {
            holder.bind(mDrawings.get(position));
        }

        @Override
        public int getItemCount() {
            return mDrawings.size();
        }
    }


}
