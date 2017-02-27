package com.android.notebook.views.spinner;

import android.widget.TextView;

/**
 * The spinner item for the font color.
 */
public class FontColorSpinnerItem extends ColorSpinnerItem {

    /**
     * @param color    This item's color
     * @param title    This item's title
     * @param isEmpty  True if we have the empty color entry (to remove any color setting)
     * @param isCustom True if we have the custom color entry opening the color wheel
     */
    public FontColorSpinnerItem(int color, String title, boolean isEmpty, boolean isCustom) {
        super(color, title, isEmpty, isCustom);
    }

    @Override
    void formatNameView(TextView view) {
        super.formatNameView(view);

        if (!isCustom()) {
            view.setTextColor(mColor);
        }
    }

}