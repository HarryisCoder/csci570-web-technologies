package com.example.tianshu.stock_search;

/**
 * Created by Tianshu on 26/11/2017.
 */

public class FavListItem {

    public String symbol;
    public double price;
    public double change;
    public double changePer;

    public FavListItem(String symbol, double price, double change, double changePer) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePer = changePer;
    }

    @Override
    public String toString() {
        return "Symbol: " + symbol + " price: " + price + " change: " + change;
    }
}
