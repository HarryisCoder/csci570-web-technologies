package com.example.tianshu.stock_search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Tianshu on 25/11/2017.
 */

public class TabHistoryFragment extends Fragment {

    private static final String TAG = "TabHistoryFragment";
    private Handler myHandler;
    private ProgressBar progressBar;
    private TextView errorText;
    private WebView myWebView;

    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_history, container, false);

        myWebView = (WebView) view.findViewById(R.id.webview_history);
        myWebView.setAlpha(0);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String symbol = ((StockDetailsActivity)getActivity()).getSymbol();
        myWebView.addJavascriptInterface(new WebAppInterface(getActivity(), symbol, "Price"), "Android");
        myWebView.loadUrl("file:///android_asset/highstock_price.html");

        progressBar = view.findViewById(R.id.highstock_progress_bar);
        errorText = view.findViewById(R.id.highstock_error);

        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    progressBar.setVisibility(View.INVISIBLE);
                    myWebView.setAlpha(1);
                }
                else if(msg.what== 0){
                    progressBar.setVisibility(View.INVISIBLE);
                    errorText.setVisibility(View.VISIBLE);
                }
                super.handleMessage(msg);
            }
        };

        return view;
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
            Log.d(TAG, "getSymbol: " + symbol);
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
