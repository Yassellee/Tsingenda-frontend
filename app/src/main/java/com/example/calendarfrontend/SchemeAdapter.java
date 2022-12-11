package com.example.calendarfrontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class SchemeAdapter extends ArrayAdapter<Scheme> {

    private final int resourceId;

    public SchemeAdapter(@NonNull Context context, int resource, List<Scheme> obj) {
        super(context, resource, obj);
        resourceId = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Scheme mScheme = getItem(position);
        View view = null;
        ViewHolder viewHolder = null;
        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            viewHolder.location = (TextView) view.findViewById(R.id.location);
            viewHolder.startTime = (TextView) view.findViewById(R.id.startTime);
            viewHolder.endTime = (TextView) view.findViewById(R.id.endTime);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.title.setText(mScheme.getTitle());
        viewHolder.location.setText(mScheme.getLocation());
        viewHolder.startTime.setText(mScheme.getStartTime());
        viewHolder.endTime.setText(mScheme.getEndTime());
        return view;
    }

    static class ViewHolder {
        TextView title;
        TextView location;
        TextView startTime;
        TextView endTime;
    }
}
