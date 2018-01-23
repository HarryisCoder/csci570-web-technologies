package com.example.tianshu.stock_search;

import android.content.Context;
import android.graphics.Color;
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

public class FavlistviewAdapter extends ArrayAdapter<FavListItem> {

//    LayoutInflater mInflater;
//    List<TableItem> items;

    public FavlistviewAdapter(Context context, ArrayList<FavListItem> users) {
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.favlist_row, parent, false);
        }

        // get current item to be displayed
        FavListItem currentItem = (FavListItem) getItem(position);

        // get the TextView for item name and item description
        TextView favlistItemSymbol = (TextView) convertView.findViewById(R.id.favlist_symbol);
        TextView favlistItemPrice = (TextView) convertView.findViewById(R.id.favlist_price);
        TextView favlistItemChange = (TextView) convertView.findViewById(R.id.favlist_change);

        //sets the text for item name and item description from the current item object
        favlistItemSymbol.setText(currentItem.symbol);
        favlistItemPrice.setText(currentItem.price + "");
        favlistItemChange.setText(currentItem.change + " (" + currentItem.changePer + "%)");
        if (currentItem.change < 0) {
            favlistItemChange.setTextColor(Color.RED);
        } else {
            favlistItemChange.setTextColor(Color.GREEN);
        }
        return convertView;
    }

//    public void addListItem(String name, String value) {
//        items.add(new TableItem(name, value));
//    }


}
