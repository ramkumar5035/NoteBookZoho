package com.android.notebook.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.android.notebook.R;

public class EditorToolbarImageButton extends ImageButton {
    private static final int[] CHECKED_STATE_SET = {R.attr.state_checked};

    private boolean mChecked;

    public EditorToolbarImageButton(Context context) {
        this(context, null);
    }

    public EditorToolbarImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.rte_ToolbarButton);
    }

    public EditorToolbarImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarButton, defStyle, 0);
        mChecked = a.getBoolean(R.styleable.ToolbarButton_checked, false);
        a.recycle();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + CHECKED_STATE_SET.length);
        if (mChecked) mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }
}