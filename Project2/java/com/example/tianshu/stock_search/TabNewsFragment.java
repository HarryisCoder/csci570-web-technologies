package com.example.tianshu.stock_search;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Tianshu on 25/11/2017.
 */

public class TabNewsFragment extends Fragment {

    private static final String TAG = "TabNewsFragment";
    private ListView listView;
    private ProgressBar newsProgressBar;
    private TextView newsError;
    private String jsonObj;
    private static final String ROOT_URL = "http://cs571-nodejs.us-east-2.elasticbeanstalk.com";
    private String symbol;
    private RequestQueue requestQueue;
    private View view;
    private NewsviewAdapter newsviewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_news, container, false);

        listView = view.findViewById(R.id.news_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(newsviewAdapter.getItem(position).link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });
        newsProgressBar = view.findViewById(R.id.news_progress_bar);
        newsError = view.findViewById(R.id.news_error);
        symbol = ((StockDetailsActivity)getActivity()).getSymbol();

        requestNews();

        return view;
    }

    private void requestNews() {
        requestQueue = Volley.newRequestQueue(getActivity());
        //Showing progress bar just after button click.
//        newsProgressBar.setVisibility(View.VISIBLE);

        String url = ROOT_URL + "/?symbol=" + symbol + "&indicator=news";
        Log.d(TAG, "JSON RESPONSE URL " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response){
                        if (response.has("Error")) {
                            showError();
                        } else {
                            drawNewsTable(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError();
                    }
                });
        //add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void drawNewsTable(JSONObject json) {
        // Construct the data source
        ArrayList<NewsItem> items = new ArrayList<>();
        // Create the adapter to convert the array to views
        newsviewAdapter = new NewsviewAdapter(getActivity(), items);
        // Add list items
        try {
            JSONArray objects = json.getJSONObject("rss").getJSONObject("channel").getJSONArray("item");
            int count = 0;
            int i = 0;
            while(i < objects.length()) {
                JSONObject item = objects.getJSONObject(i);
                if (item.getString("link").indexOf("article") != -1) {
                    String newsLink = item.getString("link");
                    String newsTitle = item.getString("title");
                    String newsAuthor = "Author: " + item.getString("sa:author_name");
                    String dateStr = item.getString("pubDate");
                    String newsDate = "Date: " + dateStr.substring(0, dateStr.length() - 6) + " EDT";
                    newsviewAdapter.add(new NewsItem(newsTitle, newsDate, newsAuthor, newsLink));
                    count++;
                }
                if (count == 10) {
                    break;
                }
                i++;
            }
            newsviewAdapter.add(new NewsItem("", "","",""));

//            Log.d(TAG, "json symbol: " + symbol);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }

//        var arrowUrl = cClose >= pClose ? "http://cs-server.usc.edu:45678/hw/hw6/images/Green_Arrow_Up.png" : "http://cs-server.usc.edu:45678/hw/hw6/images/Red_Arrow_Down.png";

        // Attach the adapter to a ListView
        ListView listView = view.findViewById(R.id.news_list);
        listView.setAdapter(newsviewAdapter);
        newsProgressBar.setVisibility(View.GONE);
    }

    private void showError() {
        newsProgressBar.setVisibility(View.GONE);
        newsError.setVisibility(View.VISIBLE);
    }

}
