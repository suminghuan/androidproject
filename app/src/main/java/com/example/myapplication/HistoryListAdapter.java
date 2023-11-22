package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryListAdapter extends ListAdapter<HistoryList, HistoryListAdapter.MovieViewHolder> {

    HistroyClickInterface histroyClickInterface;

    protected HistoryListAdapter(@NonNull DiffUtil.ItemCallback<HistoryList> diffCallback, HistroyClickInterface histroyClickInterface) {
        super(diffCallback);
        this.histroyClickInterface = histroyClickInterface;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MovieViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.doorhistory,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        HistoryList movie = getItem(position);
        holder.bind(movie);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView state_history, name_history, date_history, time_history;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            state_history = itemView.findViewById(R.id.historystate);
            name_history = itemView.findViewById(R.id.historyname);
            date_history = itemView.findViewById(R.id.historywhen_date);
            time_history = itemView.findViewById(R.id.historywhen_time);
        }
        public void bind(HistoryList movie){
            state_history.setText(movie.getState());
            name_history.setText(movie.getName());
            date_history.setText(movie.getWhen_date());
            time_history.setText(movie.getWhen_time());

        }
    }
    interface HistroyClickInterface {
        public void onDelete(int position);
    }

}
