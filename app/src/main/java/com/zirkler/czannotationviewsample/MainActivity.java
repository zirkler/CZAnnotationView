package com.zirkler.czannotationviewsample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String DRAWING_KEY = "drawing";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.appbarLayout) AppBarLayout mAppbarLayout;
    List<Drawing> mDrawings;
    private DrawingsAdapter mRecyclerAdapter;
    private Drawing mSelectedDrawing;

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
                        intent.putExtra(DRAWING_KEY, newDrawing);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void askReallyDelete() {
        new MaterialDialog.Builder(this)
                .title("Zeichnung löschen")
                .content("Sind Sie sicher, dass Sie die Zeichnung löschen möchten?")
                .positiveText("Löschen")
                .negativeText("Abbrechen")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Deletion got canceled.
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Perform actual delete.
                        // TODO: Delete drawing file from disk.
                        mSelectedDrawing.delete();
                        mDrawings.remove(mSelectedDrawing);
                        mRecyclerAdapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    public class DrawingItemHolder extends RecyclerView.ViewHolder {

        View mItemView;
        Drawing mDrawing;
        @BindView(R.id.txtIndex) TextView mTxtIndex;
        @BindView(R.id.bttMore) ImageButton mBttMore;

        public DrawingItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItemView = itemView;
        }

        public void bind(final Drawing drawing) {
            mDrawing = drawing;
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

        @OnClick(R.id.bttMore)
        public void bttMoreClicked() {
            mSelectedDrawing = mDrawing;
            PopupMenu popup = new PopupMenu(MainActivity.this, mBttMore);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_activity_item_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_delete) {
                        askReallyDelete();
                    } else if (item.getItemId() == R.id.action_edit) {

                    }
                    return true;
                }
            });
            popup.show();
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
