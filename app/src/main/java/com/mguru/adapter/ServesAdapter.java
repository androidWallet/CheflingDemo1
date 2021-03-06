package com.mguru.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mguru.cheflingtest.R;

/**
 * Created by Sk Maniruddin on 23-10-2016.
 */
public class ServesAdapter extends BaseAdapter {

    private String[] data = null;
    private Context mContext;
    private Typeface typeface;

    public ServesAdapter(Context context, String[] data) {
        this.mContext = context;
        this.data = data;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/helvetica_neue_lt_com_55_roman.ttf");
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.list_item_textview = (TextView) view.findViewById(R.id.list_item_textview);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.list_item_textview.setTypeface(typeface);

        viewHolder.list_item_textview.setText(data[i]);
        return view;
    }

    static class ViewHolder {
        TextView list_item_textview;
    }
}

