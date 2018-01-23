package com.example.tianshu.stock_search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

/**
 * Created by Tianshu on 25/11/2017.
 */

public class TabCurrentFragment extends Fragment {

    private static final String TAG = "TabCurrentFragment";
    private ListView listView;
    private ProgressBar tableProgressBar;
    private ProgressBar chartProgressBar;
    private TextView tableError;
    private TextView chartError;
    private String jsonObj;
    private static final String ROOT_URL = "http://cs571-nodejs.us-east-2.elasticbeanstalk.com";
    private String symbol;
    private JSONObject stockJSON;
    private RequestQueue requestQueue;
    private View view;
    private boolean isStarEmpty;
    private WebView myWebView;
    private ShareDialog shareDialog;
    private WebAppInterface myWebInterface;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private ImageButton starButton;
    private Handler myHandler;
    private Button changeButton;
    private Spinner spinner;
    private String currentIndicator;


    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.tab_current, container, false);

        listView = view.findViewById(R.id.stock_table);
        tableProgressBar = view.findViewById(R.id.table_progress_bar);
        chartProgressBar = view.findViewById(R.id.chart_progress_bar);
        tableError = view.findViewById(R.id.table_error);
        chartError = view.findViewById(R.id.chart_error);
        symbol = ((StockDetailsActivity)getActivity()).getSymbol();
        isStarEmpty = false;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPrefEditor = sharedPref.edit();
        starButton = view.findViewById(R.id.star_button);
        stockJSON = new JSONObject();
        setStarButton(view);
        requestPrice();

//        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        // Indicator spinner
        spinner = (Spinner) view.findViewById(R.id.indicator_spinner);
        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.indicator_string_array, android.R.layout.simple_spinner_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinner_adapter);

        //webview
        myWebView = view.findViewById(R.id.indicator_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        myWebInterface = new WebAppInterface(getActivity(), symbol, "Price");
//        myWebView.addJavascriptInterface(myWebInterface, "Android");
//        myWebView.loadUrl("file:///android_asset/highchart_price.html");
        currentIndicator = spinner.getSelectedItem().toString();
        drawHighChart(currentIndicator);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                if (!spinner.getSelectedItem().toString().equals(currentIndicator)) {
                    changeButton.setTextColor(Color.BLACK);
                    changeButton.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });

        // FB api
        shareDialog = new ShareDialog(this);

        //handler
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    chartProgressBar.setVisibility(View.INVISIBLE);
                    myWebView.setAlpha(1);
                }
                else if(msg.what== 0){
                    chartProgressBar.setVisibility(View.INVISIBLE);
                    chartError.setVisibility(View.VISIBLE);
                }
                super.handleMessage(msg);
            }
        };

        // change button
        changeButton = view.findViewById(R.id.change_button);
        changeButton.setEnabled(false);
        changeButton.setTextColor(Color.GRAY);

        // star button
        starButton.setEnabled(false);

        return view;
    }

    private void requestPrice() {
        requestQueue = Volley.newRequestQueue(getActivity());
        //Showing progress bar just after button click.
//        progressBar.setVisibility(View.VISIBLE);

        String url = ROOT_URL + "/?symbol=" + symbol + "&indicator=TIME_SERIES_DAILY";
        Log.d(TAG, "JSON RESPONSE URL " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response){

                        if (response.has("Error Message") || response == null) {
                            showTableError();
                        } else {
                            drawPriceTable(response);
                            starButton.setEnabled(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showTableError();
                    }
                });
        //add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void drawPriceTable(JSONObject json) {
        // Construct the data source
        ArrayList<TableItem> items = new ArrayList<>();
        // Create the adapter to convert the array to views
        if (getActivity()!=null) {
            ListviewAdapter listviewAdapter = new ListviewAdapter(getActivity(), items);
            // Add list items
            try {
                String symbol = json.getJSONObject("Meta Data").getString("2. Symbol");
                String time = json.getJSONObject("Meta Data").getString("3. Last Refreshed");
                JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
                JSONArray days = timeSeries.names();
                double cOpen = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("1. open"), 2);
                double cClose = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("4. close"), 2);
                double pClose = roundDigits(timeSeries.getJSONObject(days.getString(1)).getDouble("4. close"), 2);
                double cLow = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("3. low"), 2);
                double cHigh = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("2. high"), 2);
                int cVol = timeSeries.getJSONObject(days.getString(0)).getInt("5. volume");
                double change = roundDigits(cClose - pClose, 2);
                double changePer = roundDigits(change / pClose, 2);
                String changeStr = change + " (" + changePer + "%)";
                listviewAdapter.add(new TableItem("Stock Symbol", symbol));
                listviewAdapter.add(new TableItem("Last Price", cClose + ""));
                listviewAdapter.add(new TableItem("Change", changeStr));
                listviewAdapter.add(new TableItem("Timestamp", time));
                listviewAdapter.add(new TableItem("Open", cOpen + ""));
                listviewAdapter.add(new TableItem("Close", pClose + ""));
                listviewAdapter.add(new TableItem("Day's Range", cLow + " - " + cHigh));
                listviewAdapter.add(new TableItem("Volume", cVol + ""));

                stockJSON.put("Price", cClose);
                stockJSON.put("Change", change);
                stockJSON.put("ChangePer", changePer);
//            Log.d(TAG, "json symbol: " + symbol);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

//        var arrowUrl = cClose >= pClose ? "http://cs-server.usc.edu:45678/hw/hw6/images/Green_Arrow_Up.png" : "http://cs-server.usc.edu:45678/hw/hw6/images/Red_Arrow_Down.png";

            // Attach the adapter to a ListView
            ListView listView = view.findViewById(R.id.stock_table);
            tableProgressBar.setVisibility(View.GONE);
            listView.setAdapter(listviewAdapter);
        }
    }

    public void showTableError() {
        tableProgressBar.setVisibility(View.GONE);
        tableError.setVisibility(View.VISIBLE);
    }

    private double roundDigits(double number, int numDigits) {
        double temp = Math.pow(10, numDigits);
        return Math.round(number * temp) / temp;
    }

    public void setStarButton(View view) {
//        Log.d(TAG, "changeStar: clicked!");
        if (!sharedPref.contains(symbol)) {
            starButton.setBackground(getContext().getDrawable(R.drawable.empty));
//            isStarEmpty = false;
        } else {
            starButton.setBackground(getContext().getDrawable(R.drawable.filled));
//            isStarEmpty = true;
        }
    }

    /**
     * called when star_button is clicked
     */
    public void changeStar(View view) {
        Log.d(TAG, "changeStar: clicked!");
        if (!sharedPref.contains(symbol)) {
            starButton.setBackground(getContext().getDrawable(R.drawable.filled));
            sharedPrefEditor.putString(symbol, stockJSON.toString());
            Log.d(TAG, "add to pref: " + symbol + ": " + stockJSON.toString());
            sharedPrefEditor.apply();
//            isStarEmpty = false;
        } else {
            starButton.setBackground(getContext().getDrawable(R.drawable.empty));
            sharedPrefEditor.remove(symbol);
            Log.d(TAG, "pref remove: " + symbol);
            sharedPrefEditor.apply();
//            isStarEmpty = true;
        }
    }

    /**
     * called when fb_button is clicked
     */
    public void shareToFB(View view) {
        Log.d(TAG, "shareToFB: clicked!");
        String url = myWebInterface.getUrl();
        Log.d(TAG, "get URL: " + url);
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .build();
        shareDialog.show(linkContent);
    }
    /**
     * called when change_button is clicked
     */
    public void changeChart() {
        chartError.setVisibility(View.INVISIBLE);
        chartProgressBar.setVisibility(View.VISIBLE);
        changeButton.setTextColor(Color.GRAY);
        changeButton.setEnabled(false);
        String indicator = spinner.getSelectedItem().toString();
        currentIndicator = indicator;
        Log.d(TAG, "Selected  : " + indicator);
        drawHighChart(indicator);
    }

    private void drawHighChart(String indicator) {
//        String symbol = ((StockDetailsActivity)getActivity()).getSymbol();
        Log.d(TAG, "drawHighChart: selected indicator" + symbol);
        if (indicator.equals("Price")) {
            myWebInterface = new WebAppInterface(getActivity(), symbol, "Price");
            myWebView.addJavascriptInterface(myWebInterface, "Android");
            myWebView.loadUrl("file:///android_asset/highchart_price.html");
        } else {
            myWebInterface = new WebAppInterface(getActivity(), symbol, indicator);
            myWebView.addJavascriptInterface(myWebInterface, "Android");
            myWebView.loadUrl("file:///android_asset/highchart_indicator.html");
        }
    }

    private class WebAppInterface {
        Context mContext;
        String symbol;
        String indicator;
        String url;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c, String symbol, String indicator) {
            this.mContext = c;
            this.symbol = symbol;
            this.indicator = indicator;
            this.url = "";
        }

//    /** Show a toast from the web page */
//    @JavascriptInterface
//    public void showToast(String toast) {
//        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
//    }

        @JavascriptInterface
        public String getSymbol() {
            return symbol;
        }

        @JavascriptInterface
        public String getIndicator() {
            return indicator;
        }

        @JavascriptInterface
        public void setUrl(String url) {

            this.url = url;
        }

        @JavascriptInterface
        public String getUrl() {
            return url;
        }

        @JavascriptInterface
        public void showChart() {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Message msg = myHandler.obtainMessage();
                    msg.what = 1;
                    msg.sendToTarget();
                }});
            thread.start();
        }

        @JavascriptInterface
        public void showError() {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Message msg = myHandler.obtainMessage();
                    msg.what = 0;
                    msg.sendToTarget();
                }});
            thread.start();
        }

    }
}
