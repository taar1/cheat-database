package com.cheatdatabase.widgets;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.cheatdatabase.R;

public class EmptyRecyclerView extends RecyclerView {
    protected View emptyView;
    protected View mLoadingView;

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            if (getAdapter().getItemCount() > 0) {
                emptyView.setVisibility(GONE);
                setVisibility(VISIBLE);
            } else {
                setVisibility(GONE);
                emptyView.setVisibility(VISIBLE);
            }
        } else {
            setVisibility(VISIBLE);
        }
        setBackgroundResource(R.drawable.bg);
    }

    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            checkIfEmpty();
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    public void setLoadingView(View loadingView) {
        mLoadingView = loadingView;
    }

    public void showLoading() {
        setVisibility(GONE);
        emptyView.setVisibility(GONE);
        mLoadingView.setVisibility(VISIBLE);
    }

    public void hideLoading() {
        mLoadingView.setVisibility(GONE);
        checkIfEmpty();
    }
}