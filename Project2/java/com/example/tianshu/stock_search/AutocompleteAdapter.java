package com.example.tianshu.stock_search;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by Tianshu on 23/11/2017.
 */

public class AutocompleteAdapter extends ArrayAdapter implements Filterable {
    private ArrayList<String> matches;
    private String STOCK_MATCH_URL =
            "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";

    final private String TAG = "AutocompleteAdapter";

    public AutocompleteAdapter(Context context, int resource) {
        super(context, resource);
        matches = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return matches == null ? 0 : matches.size();
//        return matches == null ? 0 : Math.min(5, matches.size());
    }
//
    @Override
    public String getItem(int position) {
        return matches.get(position);
    }

//    public ArrayList<String> getMatches() {
//        return matches;
//    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<String> tempMatches = new ArrayList<>();;
                if(constraint != null){
                    try{
                        //get data from the web
                        String term = constraint.toString();
                        tempMatches = new DownloadMatches().execute(term).get();
                        Log.d(TAG,"matches: " + tempMatches.toString());
                    }catch (Exception e){
                        Log.d("HUS","EXCEPTION "+e);
                    }
                }
                filterResults.values = tempMatches;
                filterResults.count = tempMatches.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                matches = (ArrayList<String>)results.values;
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        View view = inflater.inflate(R.layout.activity_main, parent,false);
//
//        //get Stock
//        String stockStr = matches.get(position);
//
//        TextView stockTextView = view.findViewById(R.id.stockMatches);
//
//        stockTextView.setText(stockStr);
//
//        return view;
//    }

    //download matches list
    private class DownloadMatches extends AsyncTask<String, Void, ArrayList>{

        @Override
        protected ArrayList doInBackground(String... params) {
            try {
                //Create a new COUNTRY SEARCH url Ex "search.php?term=india"
                String NEW_URL = STOCK_MATCH_URL + URLEncoder.encode(params[0],"UTF-8");
                Log.d("HUS", "JSON RESPONSE URL " + NEW_URL);

                URL url = new URL(NEW_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }

                //parse JSON and store it in the list
                String jsonString =  sb.toString();
                ArrayList<String> stockStrList = new ArrayList<>();

                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
//                    Log.d("JSON Obj:", jsonObj.toString());
                    String displayStr = jsonObj.getString("Symbol") + " - " + jsonObj.getString("Name") + "(" + jsonObj.getString("Exchange") + ")";
//                    Log.d("displayStr:", displayStr);
                    stockStrList.add(displayStr);
                }
                return stockStrList;

            } catch (Exception e) {
                Log.d("HUS", "EXCEPTION " + e);
                return null;
            }
        }
    }
}
