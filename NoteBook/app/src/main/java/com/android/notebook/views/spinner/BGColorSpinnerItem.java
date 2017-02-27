package com.android.notebook.views.spinner;

import android.graphics.Color;
import android.widget.TextView;

/**
 * The spinner item for the background color.
 */
public class BGColorSpinnerItem extends ColorSpinnerItem {
    private static final double rY = 0.212655;
    private static final double gY = 0.715158;
    private static final double bY = 0.072187;

    /**
     * @param color    This item's color
     * @param title    This item's title
     * @param isEmpty  True if we have the empty color entry (to remove any color setting)
     * @param isCustom True if we have the custom color entry opening the color wheel
     */
    public BGColorSpinnerItem(int color, String title, boolean isEmpty, boolean isCustom) {
        super(color, title, isEmpty, isCustom);
    }

    @Override
    void formatNameView(TextView view) {
        super.formatNameView(view);

        if (isEmpty()) {
            view.setBackgroundColor(0x00000000);
        } else {
            view.setBackgroundColor(mColor);

            int r = (mColor) & 0xFF;
            int g = (mColor >> 8) & 0xFF;
            int b = (mColor >> 16) & 0xFF;
            double Y = rY * r + gY * g + bY * b;
            view.setTextColor(Y > 0x88 ? Color.BLACK : Color.WHITE);
        }
    }

}