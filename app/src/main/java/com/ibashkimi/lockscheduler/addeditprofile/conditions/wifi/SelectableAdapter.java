package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi;

import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    @SuppressWarnings("unused")
    private static final String TAG = SelectableAdapter.class.getSimpleName();

    private SparseBooleanArray selectedItems;

    public SelectableAdapter() {
        this(new SparseBooleanArray());
    }

    public SelectableAdapter(SparseBooleanArray booleanArray) {
        selectedItems = booleanArray != null ? booleanArray : new SparseBooleanArray();
    }

    /**
     * Indicates if the item at position position is profile_selected
     *
     * @param position Position of the item to check
     * @return true if the item is profile_selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /*public void setSelected(int position, boolean selected) {
        if (selected) {
            selectedItems.put(position, true);
        } else {
            selectedItems.delete(position);
        }
    }*/

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        //notifyItemChanged(position);
    }

    public void setSelected(int position, boolean selected) {
        selectedItems.put(position, selected);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Count the profile_selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of profile_selected items
     *
     * @return List of profile_selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}
