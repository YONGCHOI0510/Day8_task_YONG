package com.example.hs20150126.day8_task;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class ScheduleListAdapter extends BaseAdapter {
	private Context mContext;

	// schedule List
	ArrayList<com.example.hs20150126.day8_task.ScheduleListItem> scheduleList;

	public ScheduleListAdapter(Context context) {
		mContext = context;

		scheduleList = new ArrayList<com.example.hs20150126.day8_task.ScheduleListItem>();
	}

	public void clear() {
		scheduleList.clear();
	}

	public void addItem(com.example.hs20150126.day8_task.ScheduleListItem item) {
		scheduleList.add(item);
	}

	public void removeItem(com.example.hs20150126.day8_task.ScheduleListItem item) {
		scheduleList.remove(item);
	}

	public void addAll(ArrayList<com.example.hs20150126.day8_task.ScheduleListItem> items) {
		scheduleList.addAll(items);
	}

	public int getCount() {
		return scheduleList.size();
	}

	public Object getItem(int position) {
		return scheduleList.get(position);
	}

	public boolean isSelectable(int position) {
		return true;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		com.example.hs20150126.day8_task.ScheduleListItemView itemView = null;

		if (convertView == null) {
			itemView = new com.example.hs20150126.day8_task.ScheduleListItemView(mContext);
		} else {
			itemView = (com.example.hs20150126.day8_task.ScheduleListItemView) convertView;
		}

		com.example.hs20150126.day8_task.ScheduleListItem curItem = (com.example.hs20150126.day8_task.ScheduleListItem) scheduleList.get(position);
		itemView.setTime(curItem.getTime());
		itemView.setMessage(curItem.getMessage());

		return itemView;
	}
}
