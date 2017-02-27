package com.android.notebook.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.notebook.R;
import com.onegravity.colorpicker.ColorPickerDialog;
import com.onegravity.colorpicker.ColorPickerListener;
import com.onegravity.colorpicker.SetColorPickerListenerEvent;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.RTToolbarListener;
import com.onegravity.rteditor.effects.Effects;
import com.onegravity.rteditor.fonts.FontManager;
import com.onegravity.rteditor.fonts.RTTypeface;
import com.android.notebook.views.spinner.BGColorSpinnerItem;
import com.android.notebook.views.spinner.ColorSpinnerItem;
import com.android.notebook.views.spinner.FontColorSpinnerItem;
import com.android.notebook.views.spinner.FontSizeSpinnerItem;
import com.android.notebook.views.spinner.FontSpinnerItem;
import com.android.notebook.views.spinner.SpinnerItem;
import com.android.notebook.views.spinner.SpinnerItemAdapter;
import com.android.notebook.views.spinner.SpinnerItems;
import com.onegravity.rteditor.utils.Helper;

import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is a concrete implementation of the RTToolbar interface. It uses
 * toggle buttons for the effects with a simple on/off (like bold/not bold) and
 * Spinners for the more complex formatting (background color, font color, font
 * size).
 * <p/>
 * While the included rte_toolar layout puts all icons in a row it's easy to use
 * multiple toolbars, each with a subset of formatting options (e.g. one for the
 * character formatting, one for the paragraph formatting, one for all the rest
 * like insert image, undo/redo etc.).
 */
public class EditorHorizontalToolbar extends LinearLayout implements RTToolbar, View.OnClickListener {

    /*
     * We need a unique id for the toolbar because the RTManager is capable of managing multiple toolbars
     */
    private static AtomicInteger sIdCounter = new AtomicInteger(0);
    private int mId;

    private RTToolbarListener mListener;

    private ViewGroup mToolbarContainer;

    /*
     * The buttons
     */
    private EditorToolbarImageButton mBold;
    private EditorToolbarImageButton mItalic;
    private EditorToolbarImageButton mUnderline;
    private EditorToolbarImageButton mAlignLeft;
    private EditorToolbarImageButton mAlignCenter;
    private EditorToolbarImageButton mAlignRight;

    /*
     * The Spinners and their SpinnerAdapters
     */
    private Spinner mFont;
    private SpinnerItemAdapter<FontSpinnerItem> mFontAdapter;

    private Spinner mFontSize;
    private SpinnerItemAdapter<FontSizeSpinnerItem> mFontSizeAdapter;

    private Spinner mFontColor;
    private SpinnerItemAdapter<? extends ColorSpinnerItem> mFontColorAdapter;

    private Spinner mBGColor;
    private SpinnerItemAdapter<? extends ColorSpinnerItem> mBGColorAdapter;

    private int mCustomColorFont = Color.BLACK;
    private int mCustomColorBG = Color.BLACK;

    private int mPickerId = -1;
    private ColorPickerListener mColorPickerListener;

    // ****************************************** Initialize Methods *******************************************

    public EditorHorizontalToolbar(Context context) {
        super(context);
        init();
    }

    public EditorHorizontalToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditorHorizontalToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        synchronized (sIdCounter) {
            mId = sIdCounter.getAndIncrement();
        }
        SetColorPickerListenerEvent.setListener(mPickerId, mColorPickerListener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // configure regular action buttons
        mBold = initImageButton(R.id.toolbar_bold);
        mItalic = initImageButton(R.id.toolbar_italic);
        mUnderline = initImageButton(R.id.toolbar_underline);
        mAlignLeft = initImageButton(R.id.toolbar_align_left);
        mAlignCenter = initImageButton(R.id.toolbar_align_center);
        mAlignRight = initImageButton(R.id.toolbar_align_right);

        // configure font button
        mFont = (Spinner) findViewById(R.id.toolbar_font);
        mFontAdapter = createDropDownNav(mFont,
                R.layout.editor_toolbar_font_spinner,
                R.layout.editor_toolbar_spinner_item,
                getFontItems(), mFontListener);

        // configure font size button
        mFontSize = (Spinner) findViewById(R.id.toolbar_fontsize);
        mFontSizeAdapter = createDropDownNav(mFontSize,
                R.layout.editor_toolbar_fontsize_spinner,
                R.layout.editor_toolbar_spinner_item,
                getTextSizeItems(), mFontSizeListener);

        // configure font color button
        mFontColor = (Spinner) findViewById(R.id.toolbar_fontcolor);
        mFontColorAdapter = createDropDownNav(mFontColor,
                R.layout.editor_toolbar_fontcolor_spinner,
                R.layout.editor_toolbar_fontcolor_spinner_item,
                getFontColorItems(), mFontColorListener);

        // configure bg color button
        mBGColor = (Spinner) findViewById(R.id.toolbar_bgcolor);
        mBGColorAdapter = createDropDownNav(mBGColor,
                R.layout.editor_toolbar_bgcolor_spinner,
                R.layout.editor_toolbar_bgcolor_spinner_item,
                getBGColorItems(), mBGColorListener);
    }

    private EditorToolbarImageButton initImageButton(int id) {
        EditorToolbarImageButton button = (EditorToolbarImageButton) findViewById(id);
        if (button != null) {
            button.setOnClickListener(this);
        }
        return button;
    }

    private SpinnerItems<FontSpinnerItem> getFontItems() {
        /*
         * Retrieve the fonts.
         */
        SortedSet<RTTypeface> fonts = FontManager.getFonts(getContext());

        /*
         * Create the spinner items
         */
        SpinnerItems<FontSpinnerItem> spinnerItems = new SpinnerItems<FontSpinnerItem>();
        spinnerItems.add(new FontSpinnerItem(null));        // empty element
        for (RTTypeface typeface : fonts) {
            spinnerItems.add(new FontSpinnerItem(typeface));
        }

        return spinnerItems;
    }

    private SpinnerItems<FontSizeSpinnerItem> getTextSizeItems() {
        SpinnerItems<FontSizeSpinnerItem> spinnerItems = new SpinnerItems<FontSizeSpinnerItem>();
        Resources res = getResources();

        // empty size
        spinnerItems.add(new FontSizeSpinnerItem(-1, "", true));

        // regular sizes
        String[] fontSizeEntries = res.getStringArray(R.array.rte_toolbar_fontsizes_entries);
        int[] fontSizeValues = res.getIntArray(R.array.rte_toolbar_fontsizes_values);
        for (int i = 0; i < fontSizeEntries.length; i++) {
            spinnerItems.add(new FontSizeSpinnerItem(fontSizeValues[i], fontSizeEntries[i], false));
        }

        return spinnerItems;
    }

    private SpinnerItems<FontColorSpinnerItem> getFontColorItems() {
        SpinnerItems<FontColorSpinnerItem> spinnerItems = new SpinnerItems<FontColorSpinnerItem>();
        Context context = getContext();

        // empty color
        String name = context.getString(R.string.rte_toolbar_color_text);
        FontColorSpinnerItem spinnerItem = new FontColorSpinnerItem(mCustomColorFont, name, true, false);
        spinnerItems.add(spinnerItem);

        // regular colors
        for (String fontColor : getResources().getStringArray(R.array.rte_toolbar_fontcolors_values)) {
            int color = Integer.parseInt(fontColor, 16);
            spinnerItem = new FontColorSpinnerItem(color, name, false, false);
            spinnerItems.add(spinnerItem);
        }

        // custom color
        name = context.getString(R.string.rte_toolbar_color_custom);
        spinnerItem = new FontColorSpinnerItem(mCustomColorFont, name, false, true);
        spinnerItems.add(spinnerItem);

        return spinnerItems;
    }

    private SpinnerItems<BGColorSpinnerItem> getBGColorItems() {
        SpinnerItems<BGColorSpinnerItem> spinnerItems = new SpinnerItems<BGColorSpinnerItem>();
        Context context = getContext();

        // empty color
        String name = context.getString(R.string.rte_toolbar_color_text);
        BGColorSpinnerItem spinnerItem = new BGColorSpinnerItem(mCustomColorFont, name, true, false);
        spinnerItems.add(spinnerItem);

        // regular colors
        for (String fontColor : getResources().getStringArray(R.array.rte_toolbar_fontcolors_values)) {
            int color = Integer.parseInt(fontColor, 16);
            spinnerItem = new BGColorSpinnerItem(color, name, false, false);
            spinnerItems.add(spinnerItem);
        }

        // custom color
        name = context.getString(R.string.rte_toolbar_color_custom);
        spinnerItem = new BGColorSpinnerItem(mCustomColorFont, name, false, true);
        spinnerItems.add(spinnerItem);

        return spinnerItems;
    }

    private <T extends SpinnerItem> SpinnerItemAdapter<T> createDropDownNav(Spinner spinner, int spinnerId, int spinnerItemId,
                                                                            SpinnerItems<T> spinnerItems,
                                                                            final DropDownNavListener<T> listener) {
        if (spinner != null) {
            Context context = getContext();

            // create custom adapter
            final SpinnerItemAdapter<T> dropDownNavAdapter = new SpinnerItemAdapter<T>(context, spinnerItems, spinnerId, spinnerItemId);

            // configure spinner
            spinner.setPadding(spinner.getPaddingLeft(), 0, spinner.getPaddingRight(), 0);
            spinner.setAdapter(dropDownNavAdapter);
            // we need this because otherwise the first item will be selected by
            // default and the OnItemSelectedListener won't get called
            spinner.setSelection(spinnerItems.getSelectedItem());
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private AtomicBoolean mFirstCall = new AtomicBoolean(true);

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!mFirstCall.getAndSet(false) && dropDownNavAdapter.getSelectedItem() != position) {
                        listener.onItemSelected(dropDownNavAdapter.getItem(position), position);
                    }
                    dropDownNavAdapter.setSelectedItem(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            return dropDownNavAdapter;
        }

        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mColorPickerListener != null && mPickerId != -1) {
            SetColorPickerListenerEvent.setListener(mPickerId, mColorPickerListener);
        }
    }

    // ****************************************** RTToolbar Methods *******************************************

    @Override
    public void setToolbarContainer(ViewGroup toolbarContainer) {
        mToolbarContainer = toolbarContainer;
    }

    @Override
    public ViewGroup getToolbarContainer() {
        return mToolbarContainer == null ? this : mToolbarContainer;
    }

    @Override
    public void setToolbarListener(RTToolbarListener listener) {
        mListener = listener;
    }

    @Override
    public void removeToolbarListener() {
        mListener = null;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public void setBold(boolean enabled) {
        if (mBold != null) mBold.setChecked(enabled);
    }

    @Override
    public void setItalic(boolean enabled) {
        if (mItalic != null) mItalic.setChecked(enabled);
    }

    @Override
    public void setUnderline(boolean enabled) {
        if (mUnderline != null) mUnderline.setChecked(enabled);
    }

    @Override
    public void setStrikethrough(boolean enabled) {

    }

    @Override
    public void setSuperscript(boolean enabled) {

    }

    @Override
    public void setSubscript(boolean enabled) {

    }

    @Override
    public void setBullet(boolean enabled) {

    }

    @Override
    public void setNumber(boolean enabled) {

    }


    @Override
    public void setAlignment(Layout.Alignment alignment) {
        if (mAlignLeft != null) mAlignLeft.setChecked(alignment == Layout.Alignment.ALIGN_NORMAL);
        if (mAlignCenter != null)
            mAlignCenter.setChecked(alignment == Layout.Alignment.ALIGN_CENTER);
        if (mAlignRight != null)
            mAlignRight.setChecked(alignment == Layout.Alignment.ALIGN_OPPOSITE);
    }

    @Override
    public void setFont(RTTypeface typeface) {
        if (mFont != null) {
            if (typeface != null) {
                for (int pos = 0; pos < mFontAdapter.getCount(); pos++) {
                    FontSpinnerItem item = mFontAdapter.getItem(pos);
                    if (typeface.equals(item.getTypeface())) {
                        mFontAdapter.setSelectedItem(pos);
                        mFont.setSelection(pos);
                        break;
                    }
                }
            } else {
                mFontAdapter.setSelectedItem(0);
                mFont.setSelection(0);
            }
        }
    }

    /**
     * Set the text size.
     *
     * @param size the text size, if -1 then no text size is set (e.g. when selection spans more than one text size)
     */
    @Override
    public void setFontSize(int size) {
        if (mFontSize != null) {
            if (size <= 0) {
                mFontSizeAdapter.updateSpinnerTitle("");
                mFontSizeAdapter.setSelectedItem(0);
                mFontSize.setSelection(0);
            } else {
                size = Helper.convertSpToPx(size);
                mFontSizeAdapter.updateSpinnerTitle(Integer.toString(size));
                for (int pos = 0; pos < mFontSizeAdapter.getCount(); pos++) {
                    FontSizeSpinnerItem item = mFontSizeAdapter.getItem(pos);
                    if (size == item.getFontSize()) {
                        mFontSizeAdapter.setSelectedItem(pos);
                        mFontSize.setSelection(pos);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setFontColor(int color) {
        if (mFontColor != null) setFontColor(color, mFontColor, mFontColorAdapter);
    }

    @Override
    public void setBGColor(int color) {
        if (mBGColor != null) setFontColor(color, mBGColor, mBGColorAdapter);
    }

    @Override
    public void removeFontColor() {
        if (mFontColor != null) {
            mFontColorAdapter.setSelectedItem(0);
            mFontColor.setSelection(0);
        }
    }

    @Override
    public void removeBGColor() {
        if (mBGColor != null) {
            mBGColorAdapter.setSelectedItem(0);
            mBGColor.setSelection(0);
        }
    }

    private void setFontColor(int color, Spinner spinner, SpinnerItemAdapter<? extends ColorSpinnerItem> adapter) {
        int color2Compare = color & 0xffffff;
        for (int pos = 0; pos < adapter.getCount(); pos++) {
            ColorSpinnerItem item = adapter.getItem(pos);
            if (!item.isEmpty() && color2Compare == (item.getColor() & 0xffffff)) {
                adapter.setSelectedItem(pos);
                spinner.setSelection(pos);
                break;
            }
        }
    }

    // ****************************************** Item Selected Methods *******************************************

    interface DropDownNavListener<T extends SpinnerItem> {
        void onItemSelected(T spinnerItem, int position);
    }

    private DropDownNavListener<FontSpinnerItem> mFontListener = new DropDownNavListener<FontSpinnerItem>() {
        @Override
        public void onItemSelected(FontSpinnerItem spinnerItem, int position) {
            RTTypeface typeface = spinnerItem.getTypeface();
            mListener.onEffectSelected(Effects.TYPEFACE, typeface);
        }
    };

    private DropDownNavListener<FontSizeSpinnerItem> mFontSizeListener = new DropDownNavListener<FontSizeSpinnerItem>() {
        @Override
        public void onItemSelected(FontSizeSpinnerItem spinnerItem, int position) {
            int size = spinnerItem.getFontSize();
            mFontSizeAdapter.updateSpinnerTitle(spinnerItem.isEmpty() ? "" : Integer.toString(size));
            size = Helper.convertPxToSp(size);
            mListener.onEffectSelected(Effects.FONTSIZE, size);
        }
    };

    private DropDownNavListener<FontColorSpinnerItem> mFontColorListener = new DropDownNavListener<FontColorSpinnerItem>() {
        @Override
        public void onItemSelected(final FontColorSpinnerItem spinnerItem, int position) {
            if (spinnerItem.isCustom()) {
                mColorPickerListener = new ColorPickerListener() {
                    @Override
                    public void onColorChanged(int color) {
                        mCustomColorFont = color;
                        spinnerItem.setColor(color);
                        mFontColorAdapter.notifyDataSetChanged();
                        if (mListener != null) {
                            mListener.onEffectSelected(Effects.FONTCOLOR, color);
                        }
                    }

                    @Override
                    public void onDialogClosing() {
                        mPickerId = -1;
                    }
                };
                mPickerId = new ColorPickerDialog(getContext(), mCustomColorFont, false).show();
                SetColorPickerListenerEvent.setListener(mPickerId, mColorPickerListener);
            } else if (mListener != null) {
                Integer color = spinnerItem.isEmpty() ? null : spinnerItem.getColor();
                mListener.onEffectSelected(Effects.FONTCOLOR, color);
            }
        }
    };

    private DropDownNavListener<BGColorSpinnerItem> mBGColorListener = new DropDownNavListener<BGColorSpinnerItem>() {
        @Override
        public void onItemSelected(final BGColorSpinnerItem spinnerItem, int position) {
            if (spinnerItem.isCustom()) {
                mColorPickerListener = new ColorPickerListener() {
                    @Override
                    public void onColorChanged(int color) {
                        mCustomColorBG = color;
                        spinnerItem.setColor(color);
                        mBGColorAdapter.notifyDataSetChanged();
                        if (mListener != null) {
                            mListener.onEffectSelected(Effects.BGCOLOR, color);
                        }
                    }

                    public void onDialogClosing() {
                        mPickerId = -1;
                    }
                };
                mPickerId = new ColorPickerDialog(getContext(), mCustomColorBG, false).show();
                SetColorPickerListenerEvent.setListener(mPickerId, mColorPickerListener);
            } else if (mListener != null) {
                Integer color = spinnerItem.isEmpty() ? null : spinnerItem.getColor();
                mListener.onEffectSelected(Effects.BGCOLOR, color);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (mListener != null) {

            int id = v.getId();
            if (id == R.id.toolbar_bold) {
                mBold.setChecked(!mBold.isChecked());
                mListener.onEffectSelected(Effects.BOLD, mBold.isChecked());
            } else if (id == R.id.toolbar_italic) {
                mItalic.setChecked(!mItalic.isChecked());
                mListener.onEffectSelected(Effects.ITALIC, mItalic.isChecked());
            } else if (id == R.id.toolbar_underline) {
                mUnderline.setChecked(!mUnderline.isChecked());
                mListener.onEffectSelected(Effects.UNDERLINE, mUnderline.isChecked());
            } else if (id == R.id.toolbar_align_left) {
                if (mAlignLeft != null) mAlignLeft.setChecked(true);
                if (mAlignCenter != null) mAlignCenter.setChecked(false);
                if (mAlignRight != null) mAlignRight.setChecked(false);
                mListener.onEffectSelected(Effects.ALIGNMENT, Layout.Alignment.ALIGN_NORMAL);
            } else if (id == R.id.toolbar_align_center) {
                if (mAlignLeft != null) mAlignLeft.setChecked(false);
                if (mAlignCenter != null) mAlignCenter.setChecked(true);
                if (mAlignRight != null) mAlignRight.setChecked(false);
                mListener.onEffectSelected(Effects.ALIGNMENT, Layout.Alignment.ALIGN_CENTER);
            } else if (id == R.id.toolbar_align_right) {
                if (mAlignLeft != null) mAlignLeft.setChecked(false);
                if (mAlignCenter != null) mAlignCenter.setChecked(false);
                if (mAlignRight != null) mAlignRight.setChecked(true);
                mListener.onEffectSelected(Effects.ALIGNMENT, Layout.Alignment.ALIGN_OPPOSITE);
            }
        }
    }

}
