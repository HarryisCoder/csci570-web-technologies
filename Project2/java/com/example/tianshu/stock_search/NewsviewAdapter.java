package com.example.tianshu.stock_search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tianshu on 26/11/2017.
 */

public class NewsviewAdapter extends ArrayAdapter<NewsItem> {

//    LayoutInflater mInflater;
//    List<TableItem> items;

    public NewsviewAdapter(Context context, ArrayList<NewsItem> users) {
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.newsview_row, parent, false);
        }

        // get current item to be displayed
        NewsItem currentItem = getItem(position);

        // get the TextView for item name and item description
        TextView newsItemTitle = (TextView) convertView.findViewById(R.id.favlist_symbol);
        TextView newsItemAuthor = (TextView) convertView.findViewById(R.id.newsview_author);
        TextView newsItemDate = (TextView) convertView.findViewById(R.id.favlist_price);

        //sets the text for item name and item description from the current item object
        newsItemTitle.setText(currentItem.title);
        newsItemAuthor.setText(currentItem.author);
        newsItemDate.setText(currentItem.date);

        return convertView;
    }

//    public void addListItem(String name, String value) {
//        items.add(new TableItem(name, value));
//    }


}
