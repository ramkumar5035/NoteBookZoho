package com.android.notebook.views.spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of SpinnerItem objects.
 * It's used to populate the SpinnerItemAdapter.
 */
public class SpinnerItems<T extends SpinnerItem> {

    private List<T> mItems = new ArrayList<T>();
    private int mSelectedItem = -1;

    /**
     * Constructor for default values (no entries, no selected entries)
     */
    public SpinnerItems() {
    }

    /**
     * @param items        The list of SpinnerItem objects to display in the spinner
     * @param selectedItem The index of the selected item or -1 if none has been selected yet
     */
    public SpinnerItems(List<T> items, int selectedItem) {
        mItems = items;
        mSelectedItem = selectedItem;
    }

    public synchronized void add(T item) {
        getItemsInternal().add(item);
    }

    public synchronized void clear() {
        getItemsInternal().clear();
    }

    public synchronized void setItems(List<T> items) {
        mItems = items;
    }

    public synchronized List<T> getItems() {
        return getItemsInternal();
    }

    public synchronized int size() {
        return getItemsInternal().size();
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }

    public int getSelectedItem() {
        return mSelectedItem;
    }

    // lazy initialization
    private synchronized List<T> getItemsInternal() {
        if (mItems == null) mItems = new ArrayList<T>();
        return mItems;
    }
}