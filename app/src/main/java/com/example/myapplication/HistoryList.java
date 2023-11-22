package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

public class HistoryList {
    private String id;
    private String state;
    private String name;
    private String when_date;
    private String when_time;
    public HistoryList(String state,String name,String when_date, String when_time ){
        this.state = state;
        this.name = name;
        this.when_date = when_date;
        this.when_time = when_time;
    }
    public String getId(){
        return id;
    }
    public void setState(String state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWhen_date(String when_date) {
        this.when_date = when_date;
    }

    public void setWhen_time(String when_time) {
        this.when_time = when_time;
    }

    public String getState(){return state;}
    public String getName(){return name;}
    public String getWhen_date(){return when_date;}
    public String getWhen_time(){return when_time;}

    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        HistoryList history = (HistoryList) o;
        return state.equals(history.state) && name.equals(history.name) &&
                when_date.equals(history.when_date) && when_time.equals(history.when_time);
    }
    public int hashCode(){ return Objects.hash(state,name,when_date,when_time);}
    public static final DiffUtil.ItemCallback<HistoryList> itemCallback = new DiffUtil.ItemCallback<HistoryList>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryList oldUser, @NonNull HistoryList newUser) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldUser.getId() == newUser.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull HistoryList oldUser, @NonNull HistoryList newUser) {
                    return oldUser.equals(newUser);
                }
    };
}
