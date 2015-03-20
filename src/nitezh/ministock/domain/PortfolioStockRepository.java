/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.domain;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nitezh.ministock.Cache;
import nitezh.ministock.DialogTools;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;

public class PortfolioStockRepository {
    public static final String PORTFOLIO_JSON = "portfolioJson";
    public static final String WIDGET_JSON = "widgetJson";
    private static final HashMap<String, PortfolioStock> mPortfolioStocks = new HashMap<>();
    // Cache markers
    private static boolean mDirtyPortfolioStockMap = true;
    public HashMap<String, StockQuote> mStockData = new HashMap<>();
    public HashMap<String, PortfolioStock> mPortfolioStockMap = new HashMap<>();
    public Set<String> mWidgetsStockMap = new HashSet<>();
    private Storage appStorage;
    private String mStockSymbol;

    public PortfolioStockRepository(Storage appStorage, Cache cache, WidgetRepository widgetRepository) {
        this.appStorage = appStorage;

        // Add any missing stocks from the widget stocks map to our local
        // portfolio stocks map
        mPortfolioStockMap = this.getStocks();
        mWidgetsStockMap = widgetRepository.getWidgetsStockSymbols();
        for (String symbol : mWidgetsStockMap) {
            if (!mPortfolioStockMap.containsKey(symbol)) {
                mPortfolioStockMap.put(symbol, null);
            }
        }

        // Get current prices
        Set<String> symbolSet = mPortfolioStockMap.keySet();
        mStockData = new StockQuoteRepository(appStorage, cache, widgetRepository)
                .getQuotes(Arrays.asList(symbolSet.toArray(new String[symbolSet.size()])), false);
    }

    public List<Map<String, String>> getDisplayInfo() {
        NumberFormat numberFormat = NumberFormat.getInstance();

        List<Map<String, String>> info = new ArrayList<>();
        for (String symbol : this.getSortedSymbols()) {
            StockQuote quote = this.mStockData.get(symbol);
            PortfolioStock stock = this.mPortfolioStockMap.get(symbol);
            Map<String, String> itemInfo = new HashMap<>();

            // Add name if we have one
            String name = "No description";
            if (quote != null) {
                if (!stock.getCustomName().equals("")) {
                    name = stock.getCustomName();
                    itemInfo.put("customName", name);
                }
                if (name.equals("")) {
                    name = quote.getName();
                }
            }
            itemInfo.put("name", name);

            // Get the current price if we have the data
            String currentPrice = "";
            if (quote != null)
                currentPrice = quote.getPrice();
            itemInfo.put("currentPrice", currentPrice);

            // Default labels
            itemInfo.put("limitHigh_label", "High alert:");
            itemInfo.put("limitLow_label", "Low alert:");

            // Add stock info the the list view
            if (!stock.getPrice().equals("")) {
                // Buy price and label
                String buyPrice = stock.getPrice();
                itemInfo.put("buyPrice", buyPrice);

                // Buy date and label
                String date = stock.getDate();
                itemInfo.put("date", date);

                // High alert and label
                String limitHigh = NumberTools.decimalPlaceFormat(stock.getHighLimit());
                if (limitHigh != null && !limitHigh.equals("")) {
                    itemInfo.put("limitHigh_label", "High alert:");
                }
                itemInfo.put("limitHigh", limitHigh);

                // Low alert and label
                String limitLow = NumberTools.decimalPlaceFormat(stock.getLowLimit());
                if (limitLow != null && !limitLow.equals("")) {
                    itemInfo.put("limitLow_label", "Low alert:");
                }
                itemInfo.put("limitLow", limitLow);

                // Quantity and label
                String quantity = stock.getQuantity();
                itemInfo.put("quantity", quantity);

                // Calculate last change, including percentage
                String lastChange = "";
                try {
                    if (quote != null) {
                        lastChange = quote.getPercent();
                        try {
                            Double change = numberFormat.parse(quote.getChange()).doubleValue();
                            Double totalChange = NumberTools.parseDouble(stock.getQuantity()) * change;
                            lastChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format("%.0f", (totalChange)), symbol);
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
                itemInfo.put("lastChange", lastChange);

                // Calculate total change, including percentage
                String totalChange = "";
                try {
                    Double price = numberFormat.parse(currentPrice).doubleValue();
                    Double buy = Double.parseDouble(buyPrice);
                    Double totalPercentChange = price - buy;
                    totalChange = String.format("%.0f", 100 * totalPercentChange / buy) + "%";

                    // Calculate change
                    try {
                        Double quanta = NumberTools.parseDouble(stock.getQuantity());
                        totalChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format("%.0f", quanta * totalPercentChange), symbol);
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
                itemInfo.put("totalChange", totalChange);

                // Calculate the holding value
                String holdingValue = "";
                try {
                    Double holdingQuanta = NumberTools.parseDouble(stock.getQuantity());
                    Double holdingPrice = numberFormat.parse(currentPrice).doubleValue();
                    holdingValue = CurrencyTools.addCurrencyToSymbol(String.format("%.0f", (holdingQuanta * holdingPrice)), symbol);
                } catch (Exception ignored) {
                }
                itemInfo.put("holdingValue", holdingValue);
            }
            itemInfo.put("symbol", symbol);
            info.add(itemInfo);
        }
        return info;
    }

    public void backupPortfolio(Context context) {
        String rawJson = this.appStorage.getString(PORTFOLIO_JSON, "");
        UserData.writeInternalStorage(context, rawJson, PORTFOLIO_JSON);
        DialogTools.showSimpleDialog(context, "PortfolioActivity backed up",
                "Your portfolio settings have been backed up to internal appStorage.");
    }

    public void restorePortfolio(Context context) {
        mDirtyPortfolioStockMap = true;
        String rawJson = UserData.readInternalStorage(context, PORTFOLIO_JSON);
        this.appStorage.putString(PORTFOLIO_JSON, rawJson);
        this.appStorage.apply();
        DialogTools.showSimpleDialog(context, "PortfolioActivity restored",
                "Your portfolio settings have been restored from internal appStorage.");
    }

    public JSONObject getStocksJson() {
        JSONObject stocksJson = new JSONObject();
        try {
            stocksJson = new JSONObject(this.appStorage.getString(PORTFOLIO_JSON, ""));
        } catch (JSONException ignored) {
        }
        return stocksJson;
    }

    public HashMap<String, PortfolioStock> getStocks() {
        if (!mDirtyPortfolioStockMap) {
            return mPortfolioStocks;
        }
        mPortfolioStocks.clear();

        // Use the Json data if present
        Iterator keys;
        JSONObject json = this.getStocksJson();
        keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            JSONObject itemJson = new JSONObject();
            try {
                itemJson = json.getJSONObject(key);
            } catch (JSONException ignored) {
            }

            HashMap<PortfolioField, String> stockInfoMap = new HashMap<>();
            for (PortfolioField f : PortfolioField.values()) {
                String data = "";
                try {
                    if (!itemJson.get(f.name()).equals("empty")) {
                        data = itemJson.get(f.name()).toString();
                    }
                } catch (JSONException ignored) {
                }
                stockInfoMap.put(f, data);
            }

            PortfolioStock stock = new PortfolioStock(key,
                    stockInfoMap.get(PortfolioField.PRICE),
                    stockInfoMap.get(PortfolioField.DATE),
                    stockInfoMap.get(PortfolioField.QUANTITY),
                    stockInfoMap.get(PortfolioField.LIMIT_HIGH),
                    stockInfoMap.get(PortfolioField.LIMIT_LOW),
                    stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY),
                    stockInfoMap.get(PortfolioField.SYMBOL_2));
            mPortfolioStocks.put(key, stock);
        }
        mDirtyPortfolioStockMap = false;

        return mPortfolioStocks;
    }

    public void persist() {
        JSONObject json = new JSONObject();
        for (String symbol : this.mPortfolioStockMap.keySet()) {
            PortfolioStock item = this.mPortfolioStockMap.get(symbol);
            if (!item.isEmpty()) {
                try {
                    json.put(symbol, item.toJson());
                } catch (JSONException ignored) {
                }
            }
        }
        this.appStorage.putString(PORTFOLIO_JSON, json.toString());
        this.appStorage.apply();
        mDirtyPortfolioStockMap = true;
    }

    public HashMap<String, PortfolioStock> getStocksForSymbols(List<String> symbols) {
        HashMap<String, PortfolioStock> stocksForSymbols = new HashMap<>();
        HashMap<String, PortfolioStock> stocks = this.getStocks();

        for (String symbol : symbols) {
            PortfolioStock stock = stocks.get(symbol);
            if (stock != null && !stock.isEmpty()) {
                stocksForSymbols.put(symbol, stock);
            }
        }

        return stocksForSymbols;
    }

    public List<String> getSortedSymbols() {
        ArrayList<String> symbols = new ArrayList<>();
        for (String key : this.mPortfolioStockMap.keySet()) {
            symbols.add(key);
        }

        try {
            // Ensure symbols beginning with ^ appear first
            Collections.sort(symbols, new RuleBasedCollator("< '^' < a"));
        } catch (ParseException ignored) {
        }
        return symbols;
    }

    public void updateStock(String price, String date, String quantity,
                            String limitHigh, String limitLow, String customDisplay) {
        PortfolioStock portfolioStock = new PortfolioStock(mStockSymbol, price, date, quantity,
                limitHigh, limitLow, customDisplay, null);
        this.mPortfolioStockMap.put(mStockSymbol, portfolioStock);
    }

    public void updateStock() {
        this.updateStock("", "", "", "", "", "");
    }

    public void removeUnused() {
        for (String symbol : this.mPortfolioStockMap.keySet()) {
            String price = this.mPortfolioStockMap.get(symbol).getPrice();
            if ((price == null || price.equals("")) && !this.mWidgetsStockMap.contains(symbol)) {
                this.mPortfolioStockMap.remove(symbol);
            }
        }
    }

    public void saveChanges() {
        this.removeUnused();
        this.persist();
    }

    public enum PortfolioField {
        PRICE, DATE, QUANTITY, LIMIT_HIGH, LIMIT_LOW, CUSTOM_DISPLAY, SYMBOL_2
    }
}