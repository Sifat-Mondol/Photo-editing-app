package com.sifat.bcsbnagla.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sifat.bcsbnagla.R;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private Context context;
    private List<String> filterNames;
    private OnFilterClickListener listener;
    private Bitmap originalBitmap;

    public interface OnFilterClickListener {
        void onFilterClick(int position);
    }

    public FilterAdapter(Context context, List<String> filterNames, OnFilterClickListener listener) {
        this.context = context;
        this.filterNames = filterNames;
        this.listener = listener;
    }

    public void setOriginalBitmap(Bitmap bitmap) {
        this.originalBitmap = bitmap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        String filterName = filterNames.get(position);
        holder.filterName.setText(filterName);

        if (originalBitmap != null) {
            holder.filterThumbnail.setImageBitmap(originalBitmap);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterNames != null ? filterNames.size() : 0;
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView filterThumbnail;
        TextView filterName;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterThumbnail = itemView.findViewById(R.id.filterThumbnail);
            filterName = itemView.findViewById(R.id.filterName);
        }
    }
}