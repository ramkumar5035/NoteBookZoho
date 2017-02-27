package com.android.notebook.views.spinner;

import android.view.View;
import android.widget.TextView;

/**
 * An abstract spinner item.
 * The base class for the color and font spinners.
 */
public abstract class SpinnerItem {
    final protected String mTitle;

    protected OnChangedListener mOnChangedListener;
    protected Object mListenerTag;

    public interface OnChangedListener {
        void onSpinnerItemChanged(Object tag);
    }

    public SpinnerItem(String title) {
        mTitle = title;
    }

    public String getName() {
        return mTitle;
    }

    void formatNameView(TextView view) {
        if (view != null) {
            view.setText(getName());
            view.setHorizontallyScrolling(true);
        }
    }

    void formatColorView(View view) {
    }

    void setOnChangedListener(OnChangedListener listener, Object tag) {
        mOnChangedListener = listener;
        mListenerTag = tag;
    }

}