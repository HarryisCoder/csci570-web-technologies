package com.example.tianshu.stock_search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Log.*;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView stockTextView;
    private static final String TAG = "MainActivity";
    public static final String STOCK_MESSAGE = "com.example.tianshu.stock_search.MESSAGE";
    private Toast toast;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private ArrayList<FavListItem> favListItems;
    private String sortString;
    private String orderString;
    private FavlistviewAdapter favlistviewAdapter;
    private ListView favlistView;
    private ImageButton refreshButton;
    private Switch autorefreshSwitch;
    private RequestQueue requestQueue;
    private final static String ROOT_URL = "http://cs571-nodejs.us-east-2.elasticbeanstalk.com";
    private int countRefreshItem;
    private ProgressBar favlistProgressBar;
    private Handler myHandler;
    private Runnable codeBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        favListItems = new ArrayList<FavListItem>();
        favlistView = findViewById(R.id.favlist);
        favlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                FavListItem currentItem = (FavListItem)parent.getItemAtPosition(position);
                String symbol = currentItem.symbol;
                Log.d(TAG, "onItemClick symbol:" + symbol);
                getQuoteBySymbol(symbol);
            }
        });
        favlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                final PopupMenu popup = new PopupMenu(MainActivity.this, favlistView);
                FavListItem currentItem = (FavListItem)parent.getItemAtPosition(position);
                final String deleteSymbol = currentItem.symbol;
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        String response = item.getTitle().toString();
                        if (response.equals("Yes")) {
                            Log.d(TAG, "onMenuItemClick: delete symbol:" + deleteSymbol);
                            MainActivity.this.deleteFavItem(deleteSymbol);
                            popup.dismiss();
                        } else if (response.equals("No")) {
                            popup.dismiss();
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
                return true;
            }
        });

        final Spinner spinner1 = (Spinner) findViewById(R.id.sort_spinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.order_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.sort_string_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner1.setAdapter(adapter1);
        sortString = spinner1.getSelectedItem().toString();
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {

                sortString = spinner1.getSelectedItem().toString();
                orderString = spinner2.getSelectedItem().toString();
                Log.d(TAG, "Selected Sort: " + sortString);
                Log.d(TAG, "Selected Order: " + orderString);
                sortFavList(sortString, orderString);
                Log.d(TAG, "After Sorting: " + favListItems);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.order_string_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        orderString = spinner2.getSelectedItem().toString();
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
                sortString = spinner1.getSelectedItem().toString();
                orderString = spinner2.getSelectedItem().toString();
                Log.d(TAG, "Selected Sort: " + sortString);
                Log.d(TAG, "Selected Order: " + orderString);
                sortFavList(sortString, orderString);
                Log.d(TAG, "After Ordering: " + favListItems);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        // error message
        final Context context = getApplicationContext();
        CharSequence text = "Please enter a stock name or a symbol.";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);

        //auto complete
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
//        AutoCompleteTextView textView = (AutoCompleteTextView)
//                findViewById(R.id.auto_search_box);
//        textView.setAdapter(adapter);

        stockTextView = (AutoCompleteTextView) findViewById(R.id.auto_search_box);
        final AutocompleteAdapter auto_adapter = new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line);
//        Log.d("matches:", auto_adapter.getMatches().toString());
//        stockTextView.setDropDownHeight(5); // limit max number of items
        stockTextView.setAdapter(auto_adapter);

        //when autocomplete is clicked
        stockTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stockName = auto_adapter.getItem(position);
                stockTextView.setText(stockName);
            }
        });

        //sharedPreference
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPrefEditor = sharedPref.edit();

        //favlist
        loadFavList();
        // Attach the adapter to a ListView
        favlistView.setAdapter(favlistviewAdapter);

        //refresh
        countRefreshItem = 0;
        refreshButton = findViewById(R.id.refresh_button);
        autorefreshSwitch = findViewById(R.id.autorefresh_switch);
        favlistProgressBar = findViewById(R.id.favlist_progress_bar);

        // Create the Handler object (on the main thread by default)
        myHandler = new Handler();
        // Define the code block to be executed
        codeBlock = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                Log.d(TAG, "Refresh!");
                refreshOnce();
                // Repeat this the same runnable code block again another 2 seconds
                // 'this' is referencing the Runnable object
                myHandler.postDelayed(this, 6000);
            }
        };
        autorefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    myHandler.post(codeBlock);
                } else {
                    favlistProgressBar.setVisibility(View.INVISIBLE);
                    myHandler.removeCallbacks(codeBlock);
                }
            }
        });

    }

//    private static final String[] COUNTRIES = new String[] {
//            "Belgium", "France", "Italy", "Germany", "Spain"
//    };

    /** Called when the user clicks clear button */
    public void clearText(View view) {
        stockTextView.setText("");
    }
    /** Called when the user clicks get_quote button */
    public void getQuotes(View view) {
        boolean inputIsValid = checkInput();
        Log.d("Input is valid?", Boolean.toString(inputIsValid));
        if (inputIsValid) {
            Log.d(TAG, "getQuotes: Input is valid!");
            Intent intent = new Intent(this, StockDetailsActivity.class);
//            EditText editText = (EditText) findViewById(R.id.editText);
            String message = stockTextView.getText().toString();
            intent.putExtra(STOCK_MESSAGE, message);
            startActivity(intent);
        } else {
            toast.show();
        }
    }
    private void getQuoteBySymbol(String symbol) {
        Intent intent = new Intent(this, StockDetailsActivity.class);
//            EditText editText = (EditText) findViewById(R.id.editText);
        intent.putExtra(STOCK_MESSAGE, symbol);
        startActivity(intent);
    }

    private boolean checkInput() {
        String input = stockTextView.getText().toString();
        Pattern p = Pattern.compile("^\\s*$");
        Matcher m = p.matcher(input);
        return !m.matches();
    }

    private void loadFavList() {
        favListItems.clear();
        // Create the adapter to convert the array to views
        favlistviewAdapter = new FavlistviewAdapter(this, favListItems);
        // Add list items
        Map<String,?> map = sharedPref.getAll();

        Log.d(TAG, "map size: " + map.size());

        for(Map.Entry<String,?> entry : map.entrySet()){
//            Log.d(TAG, "map values " + entry.getKey() + ": " + entry.getValue().toString());
            String symbol = entry.getKey();
            try {
                JSONObject stockObj = new JSONObject((String)entry.getValue());
//                Log.d(TAG, "get entry: " + stockObj.toString());
                double price = stockObj.getDouble("Price");
                double change = stockObj.getDouble("Change");
                double changePer = stockObj.getDouble("ChangePer");
                favListItems.add(new FavListItem(symbol, price, change, changePer));
            } catch (Throwable t) {
                Log.e(TAG, "Could not parse malformed JSON!");
            }
        }
    }

    public void sortFavList(String sortMode, final String orderMode) {
        Log.d(TAG, "sortFavList: sortMode: " + sortMode);
        Log.d(TAG, "sortFavList: order: " + orderMode);
        if (sortMode.equals("Symbol")) {
            Collections.sort(favListItems, new Comparator<FavListItem>() {
                @Override
                public int compare(FavListItem a, FavListItem b) {
                    return  a.symbol.compareTo(b.symbol);
                }
            });
        } else if (sortMode.equals("Price")) {
            Collections.sort(favListItems, new Comparator<FavListItem>() {
                @Override
                public int compare(FavListItem a, FavListItem b) {
                    return Double.compare(a.price, b.price);
                }
            });
        } else if (sortMode.equals("Change")) {
            Collections.sort(favListItems, new Comparator<FavListItem>() {
                @Override
                public int compare(FavListItem a, FavListItem b) {
                    return Double.compare(a.change, b.change);
                }
            });
        } else {
            favListItems = new ArrayList<>();
            Map<String,?> map = sharedPref.getAll();
            for(Map.Entry<String,?> entry : map.entrySet()){
//                Log.d(TAG, "map values " + entry.getKey() + ": " + entry.getValue().toString());
                String symbol = entry.getKey();
                try {
                    JSONObject stockObj = new JSONObject((String)entry.getValue());
//                    Log.d(TAG, "get entry: " + stockObj.toString());
                    double price = stockObj.getDouble("Price");
                    double change = stockObj.getDouble("Change");
                    double changePer = stockObj.getDouble("ChangePer");
                    favListItems.add(new FavListItem(symbol, price, change, changePer));
                } catch (Throwable t) {
                    Log.e(TAG, "Could not parse malformed JSON!");
                }
            }
        }
        if (orderMode.equals("Descending")) {
            Collections.reverse(favListItems);
        }
        favlistviewAdapter = new FavlistviewAdapter(this, favListItems);
        Log.d(TAG, "sortFavList: size: " + favListItems.size());
        favlistviewAdapter.notifyDataSetChanged();
        favlistView.setAdapter(favlistviewAdapter);
    }

    private void deleteFavItem(String deleteString) {
        sharedPrefEditor.remove(deleteString);
        sharedPrefEditor.apply();

        int deleteIdx = 0;
        for(int i = 0; i < favListItems.size(); i++) {
            if (favListItems.get(i).symbol.equals(deleteString)) {
                deleteIdx = i;
                break;
            }
        }
        favListItems.remove(deleteIdx);
        sortFavList(sortString,orderString);
    }

    /** Called when the user clicks refresh button */
    public void refreshOnce(View view) {

//        favlistProgressBar.setVisibility(View.VISIBLE);
        refreshOnce();
    }

    private void refreshOnce() {
        favlistProgressBar.setVisibility(View.VISIBLE);
//        sharedPrefEditor.clear();
//        sharedPrefEditor.apply();
        countRefreshItem = 0;
        for (int i = 0; i < favListItems.size(); i++) {
            addItemToSP(favListItems.get(i).symbol);
        }
    }

    private void addItemToSP(String symbol) {
        requestQueue = Volley.newRequestQueue(this);
        //Showing progress bar just after button click.
//        progressBar.setVisibility(View.VISIBLE);

//        FavListItem favListItem = new FavListItem(symbol, 0, 0, 0);
        String url = ROOT_URL + "/?symbol=" + symbol + "&indicator=TIME_SERIES_DAILY";
        Log.d(TAG, "JSON RESPONSE URL " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response){
                        if (response.has("Error Message") || response == null) {
                        } else {
                            String[] rst = getJSON(response);
                            sharedPrefEditor.putString(rst[0], rst[1]);
                            sharedPrefEditor.apply();
                            countRefreshItem++;
                            Log.d(TAG, "onResponse: count:" + countRefreshItem);
                            if (countRefreshItem == favListItems.size()) {
                                favlistProgressBar.setVisibility(View.INVISIBLE);
                                loadFavList();
                                Log.d(TAG, "onResponse: sort/order: " + sortString + "/" + orderString);
                                sortFavList(sortString, orderString);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        //add request to queue
        requestQueue.add(jsonObjectRequest);
//        return favListItem;
    }

    private String[] getJSON(JSONObject json) {
        JSONObject stockJSON = new JSONObject();
        String[] rst = new String[2];
        try {
            String symbol = json.getJSONObject("Meta Data").getString("2. Symbol");
//            String time = json.getJSONObject("Meta Data").getString("3. Last Refreshed");
            JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
            JSONArray days = timeSeries.names();
//            double cOpen = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("1. open"), 2);
            double cClose = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("4. close"), 2);
            double pClose = roundDigits(timeSeries.getJSONObject(days.getString(1)).getDouble("4. close"), 2);
//            double cLow = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("3. low"), 2);
//            double cHigh = roundDigits(timeSeries.getJSONObject(days.getString(0)).getDouble("2. high"), 2);
//            int cVol = timeSeries.getJSONObject(days.getString(0)).getInt("5. volume");
            double change = roundDigits(cClose - pClose, 2);
            double changePer = roundDigits(change / pClose, 2);
//            String changeStr = change+" ("+changePer+"%)";
            stockJSON.put("Price", cClose);
            stockJSON.put("Change", change);
            stockJSON.put("ChangePer", changePer);
            rst[0] = symbol;
            rst[1] = stockJSON.toString();
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return rst;
    }

    private double roundDigits(double number, int numDigits) {
        double temp = Math.pow(10, numDigits);
        return Math.round(number * temp) / temp;
    }
}
