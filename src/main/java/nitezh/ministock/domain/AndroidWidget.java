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


import android.app.NotificationChannel;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.dataaccess.SPList;

import static yahoofinance.Utils.getString;

class AndroidWidget implements Widget{

    private final Storage storage;
    public static Context context;
    private final int id;

    private final List<String> preferencesToNotRestore = Arrays.asList("widgetSize", "widgetView");

    private int size;

    AndroidWidget(Context context, int id) {
        this.context = context;
        this.id = id;
        this.storage = this.getStorage();

        this.size = this._getSize(); // This is used a lot so don't get from storage each time
    }

    @Override
    public Storage getStorage() {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.prefs_name) + this.id, 0);
        } catch (Resources.NotFoundException ignored) {
        }

        return new PreferenceStorage(widgetPreferences);
    }

    @Override
    public void setWidgetPreferencesFromJson(JSONObject jsonPrefs) {
        String key;
        for (Iterator preferenceKey = jsonPrefs.keys(); preferenceKey.hasNext(); ) {
            key = (String) preferenceKey.next();

            if (preferencesToNotRestore.contains(key)) {
                continue;
            }

            try {
                Object value = jsonPrefs.get(key);
                if (value instanceof String) {
                    this.storage.putString(key, (String) value);
                } else if (value instanceof Boolean) {
                    this.storage.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    this.storage.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    this.storage.putFloat(key, ((Double) value).floatValue());
                } else if (value instanceof Long) {
                    this.storage.putLong(key, (Long) value);
                }
            } catch (JSONException ignored) {
            }
        }
        this.storage.apply();
    }

    @Override
    public JSONObject getWidgetPreferencesAsJson() {
        JSONObject jsonPrefs = new JSONObject();
        for (Map.Entry<String, ?> entry : this.storage.getAll().entrySet()) {
            try {
                jsonPrefs.put(entry.getKey(), entry.getValue());
            } catch (JSONException ignored) {
            }
        }

        return jsonPrefs;
    }

    @Override
    public void enablePercentChangeView() {
        this.storage.putBoolean("show_percent_change", true);
    }

    @Override
    public void enableDailyChangeView() {
        this.storage.putBoolean("show_absolute_change", true);
    }

    @Override
    public void setStock1() {
        this.storage.putString("Stock1", "^DJI");
    }

    @Override
    public void setStock1Summary() {
        this.storage.putString("Stock1_summary", "Dow Jones Industrial Average");
    }

    @Override
    public void save() {
        this.storage.apply();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
        this.storage.putInt("widgetSize", size);
        this.save();
    }

    @Override
    public boolean isNarrow() {
        return (size == 0 || size == 2 || size == 6 );
    }

    private int _getSize() {
        return this.storage.getInt("widgetSize", 0);
    }

    @Override
    public String getStock(int i) {
        return this.storage.getString("Stock" + (i + 1), "");
    }

    @Override
    public void setStock(int i,String stock) {
        this.storage.putString("Stock" + (i + 1), stock);
        this.save();
    }

    @Override
    public String getCurrencyChange(){return this.storage.getString("change_currency","normal");}
    @Override
    public int getPreviousView() {
        return this.storage.getInt("widgetView", 0);
    }

    @Override
    public void setView(int view) {
        if (view != this.getPreviousView()) {
            this.storage.putInt("widgetView", view);
            this.save();
        }
    }

    @Override
    public List<String> getSymbols() {
        boolean found = false;
        List<String> symbols = new ArrayList<>();
        String s;
        for (int i = 0; i < this.getSymbolCount(); i++) {
            s = this.getStock(i);
            symbols.add(s);
            if (!s.equals("")) {
                found = true;
            }
        }

        if (!found) {
            symbols.add("^DJI");
        }
        return symbols;
    }

    @Override
    public int getSymbolCount() {
        int size = this.getSize();
        int count = 0;
        if (size == 0 || size == 1) {
            count = 4;
        } else if (size == 2 || size == 3) {
            count = 10;
        } else if (size == 4){
            count = 1;
        }else if(size == 5 || size == 6){
            count= 15;
        }
        return count;
    }

    @Override
    public String getBackgroundStyle() {
        return this.storage.getString("background", "transparent");
    }



    @Override
    public boolean getHideSuffix() {
        return this.storage.getBoolean("hide_suffix", false);
    }

   /*@Override
    public String getTextStyle() {
        return this.storage.getString("text_style", "normal");
    }
*/
    @Override
    public boolean useBold(){


        return this.storage.getBoolean("show_bold",false);
    }

    public void sendNotification(Context context, String title, String text, int notificationId){


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);


        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());

    }

    @Override
    public boolean useUnderlined(){
        return this.storage.getBoolean("show_underlined",false);
    }

    @Override
    public boolean useItalic(){
        return this.storage.getBoolean("show_italic",false);
    }

    @Override
    public String getFontSize(){
        return this.storage.getString("font_size_custom", "medium");
    }

    @Override
    public boolean getColorsOnPrices() {
        return this.storage.getBoolean("colours_on_prices", false);
    }

    @Override
    public String getFooterVisibility() {
        return this.storage.getString("updated_display", "visible");
    }
    
    @Override
    public String getHeaderVisibility() {
        return this.storage.getString("updated_header", "visible");
    }

    @Override
    public int getHeaderColor() {
        return this.storage.getInt("updated_header_colour", 0xffff00ff );
    }

    @Override
    public int getFooterColor() {
        return this.storage.getInt("updated_footer_colour", 0xFF888888 );
    }

    @Override
    public int getStockNameColor() {
        return this.storage.getInt("stock_name_colour", 0xFFFFFFFF );
    }

    @Override
    public int getStockPriceColor() {
        return this.storage.getInt("stock_price_colour", 0xFFFFFFFF );
    }

    @Override
    public int getPriceIncreaseColor(){
        return this.storage.getInt("increase_alert_colour", 0xFFCCFF66);
    }

    @Override
    public int getPriceDecreaseColor(){
        return this.storage.getInt("decrease_alert_colour",0xFFff6666);
    }

    @Override
    public int getHighAlertColor(){
        return this.storage.getInt("high_alert_colour", 0xFFFFEE33);
    }

    @Override
    public int getLowAlertColor(){
        return this.storage.getInt("low_alert_colour", 0xFFFF66FF);
    }

    @Override
    public boolean showShortTime() {
        return this.storage.getBoolean("short_time", false);
    }

    @Override
    public boolean hasDailyChangeView() {
        return this.storage.getBoolean("show_absolute_change", false);
    }

    @Override
    public boolean hasDailyPercentView() {
        return this.storage.getBoolean("show_percent_change", false)
                && (size == 0 || size == 2);
    }

    @Override
    public boolean hasTotalChangeView() {
        return this.storage.getBoolean("show_portfolio_abs", false);
    }

    @Override
    public boolean hasTotalPercentView() {
        return this.storage.getBoolean("show_portfolio_change", false)
                && (size == 0 || size == 2);
    }

    @Override
    public boolean hasTotalChangeAerView() {
        return this.storage.getBoolean("show_portfolio_aer", false);
    }

    @Override
    public boolean hasDailyPlChangeView() {
        return this.storage.getBoolean("show_profit_daily_abs", false);
    }

    @Override
    public boolean hasDailyPlPercentView() {
        return this.storage.getBoolean("show_profit_daily_change", false)
                && (size == 0 || size == 2);
    }

    @Override
    public boolean hasTotalPlChangeView() {
        return this.storage.getBoolean("show_profit_abs", false);
    }

    @Override
    public boolean hasTotalPlPercentView() {
        return this.storage.getBoolean("show_profit_change", false)
                && (size == 0 || size == 2);
    }

    @Override
    public boolean hasTotalPlPercentAerView() {
        return this.storage.getBoolean("show_profit_aer", false);
    }

    @Override
    public boolean alwaysUseShortName() {
        return this.storage.getBoolean("always_use_short_name", false);
    }

    public boolean shouldUpdateOnRightTouch() {
        return this.storage.getBoolean("update_from_widget", false);
    }

    public boolean updateOnWifi() {
        return this.storage.getBoolean("update_only_on_wifi", true);
    }

    @Override
    public boolean updateOnCurrency() {
        return this.storage.getBoolean("update_currencies", false);
    }

    public String historicalData() {
        return this.storage.getString("historical_data", "past_year");
    }

    public boolean isUsingWifi() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    boolean isWifiConn = networkInfo.isConnected();
    return isWifiConn;
    }

    //method that checks if stocks from the S&P list are in
    // the widget
    public  List<String> checkSPStock() throws IOException{
        List <String> spList = new SPList().getSPList();
        List <String> symbols = this.getSymbols();
        List <String> spListInWidget = new ArrayList<String>();
        for (String spSymbol : spList){
            if (symbols.contains(spSymbol))
                spListInWidget.add(spSymbol);
        }
        return spListInWidget;
    }

    @Override
    public String getFont(){
        return this.storage.getString("font", "Sans-serif");
    }
}
