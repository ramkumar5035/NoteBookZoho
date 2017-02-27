package com.android.notebook.views.spinner;

import android.widget.TextView;

/**
 * The spinner item for the font size.
 */
public class FontSizeSpinnerItem extends SpinnerItem {
    final private int mFontSize;
    private final boolean mIsEmpty;

    public FontSizeSpinnerItem(int size, String title, boolean isEmpty) {
        super(title);
        mFontSize = size;
        mIsEmpty = isEmpty;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    @Override
    void formatNameView(TextView view) {
        super.formatNameView(view);
        view.setTextSize(mFontSize);
    }
}