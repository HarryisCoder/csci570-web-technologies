package com.example.tianshu.stock_search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tianshu on 26/11/2017.
 */

public class ListviewAdapter extends ArrayAdapter<TableItem> {

//    LayoutInflater mInflater;
//    List<TableItem> items;

    public ListviewAdapter(Context context, ArrayList<TableItem> users) {
        super(context, 0, users);
    }

//    @Override
//    public int getCount() {
//        return names.size();
//    }

//

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_row, parent, false);
        }

        // get current item to be displayed
        TableItem currentItem = (TableItem) getItem(position);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView) convertView.findViewById(R.id.favlist_symbol);
        TextView textViewItemValue = (TextView) convertView.findViewById(R.id.favlist_price);
        ImageButton arrowImageButton = convertView.findViewById(R.id.arrow);

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(currentItem.name);
        textViewItemValue.setText(currentItem.value);
        if (currentItem.name.equals("Change")) {
            if (currentItem.value.indexOf("-") == -1) {
                arrowImageButton.setBackground(getContext().getDrawable(R.drawable.up));
            } else {
                arrowImageButton.setBackground(getContext().getDrawable(R.drawable.down));
            }
            arrowImageButton.setVisibility(View.VISIBLE);
        } else {
            arrowImageButton.setVisibility(View.GONE);
        }
        return convertView;
    }

//    public void addListItem(String name, String value) {
//        items.add(new TableItem(name, value));
//    }


}
