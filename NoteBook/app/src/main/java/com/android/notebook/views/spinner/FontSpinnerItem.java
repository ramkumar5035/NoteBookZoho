package com.android.notebook.views.spinner;

import com.onegravity.rteditor.fonts.RTTypeface;

/**
 * The spinner item for the font.
 */
public class FontSpinnerItem extends SpinnerItem {
    final private RTTypeface mTypeface;

    public FontSpinnerItem(RTTypeface typeface) {
        super(typeface == null ? "" : typeface.getName());
        mTypeface = typeface;
    }

    public RTTypeface getTypeface() {
        return mTypeface;
    }
}