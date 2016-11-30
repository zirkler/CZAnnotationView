package com.zirkler.czannotationviewsample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.appbarLayout) AppBarLayout mAppbarLayout;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(this, AnnotationActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void setupToolbar() {
        setTitle("CZAnnotationView Example Usage");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    private void setupRecyclerView() {
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

        public void bind(int index) {
            mTxtIndex.setText("Drawing " + String.valueOf(index));

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AnnotationActivity.class);
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
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }


}
