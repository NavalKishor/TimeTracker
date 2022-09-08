package com.ctlev.app.ui.puninout;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctlev.app.databinding.FragmentItemWkDayBinding;
import com.ctlev.app.ui.puninout.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class WeeklyTrackerRecyclerViewAdapter extends RecyclerView.Adapter<WeeklyTrackerRecyclerViewAdapter.ViewHolder> {

    private final List<PlaceholderItem> mValues;

    public WeeklyTrackerRecyclerViewAdapter(List<PlaceholderItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentItemWkDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.startTime.setText(mValues.get(position).startTime);
        holder.endTime.setText(mValues.get(position).endTime);
        holder.diffTime.setText(mValues.get(position).diffTime);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView startTime;
        public final TextView endTime;
        public final TextView diffTime;
        public PlaceholderItem mItem;

        public ViewHolder(FragmentItemWkDayBinding binding) {
            super(binding.getRoot());
            startTime = binding.startTime;
            endTime = binding.endTime;
            diffTime = binding.diffTime;
        }

    }
}