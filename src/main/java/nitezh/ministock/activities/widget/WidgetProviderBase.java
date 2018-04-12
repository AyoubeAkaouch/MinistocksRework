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

package nitezh.ministock.activities.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.StepMode;


import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Arrays;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import nitezh.ministock.CustomAlarmManager;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.domain.CurrencyRepository;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.utils.StorageCache;
import nitezh.ministock.utils.GraphTools;
import nitezh.ministock.UserData;
import nitezh.ministock.activities.PreferencesActivity;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.utils.DateTools;

import static nitezh.ministock.utils.NumberTools.tryParseDouble;


public class WidgetProviderBase extends AppWidgetProvider {

    private static void applyUpdate(Context context, int appWidgetId, UpdateType updateMode,String currencies,
                                    HashMap<String, StockQuote> quotes, String quotesTimeStamp) {
        WidgetView widgetView = new WidgetView(context, appWidgetId, updateMode, currencies,
                quotes, quotesTimeStamp);
        widgetView.setOnClickPendingIntents();
        if (widgetView.hasPendingChanges()) {
            widgetView.applyPendingChanges();
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, widgetView.getRemoteViews());
        }
    }

    public static void updateWidgetAsync(Context context, int appWidgetId, UpdateType updateType,
                                         Notification notification) {
        try {
            new GetDataTask().build(context, appWidgetId, updateType, notification).execute();
        }
        // usually occurs when queued tasks = 128
        catch (RejectedExecutionException ignored) {
        }
    }

    public static void updateWidgets(Context context, UpdateType updateType, Notification notification) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        for (int appWidgetId : widgetRepository.getIds()) {
            WidgetProviderBase.updateWidgetAsync(context, appWidgetId, updateType, notification);
        }

        CustomAlarmManager alarmManager = new CustomAlarmManager(context);
        alarmManager.setUpdateTimestamp();
        alarmManager.reinitialize();
    }

    private static void doScheduledUpdates(Context context, Notification notification) {
        boolean doUpdates = true;
        Storage prefs = PreferenceStorage.getInstance(context);

        // Only update after start time
        String firstUpdateTime = prefs.getString("update_start", null);
        if (firstUpdateTime != null && !firstUpdateTime.equals("")) {
            if (DateTools.compareToNow(DateTools.parseSimpleDate(firstUpdateTime)) == 1) {
                doUpdates = false;
            }
        }

        // Only update before end time
        String lastUpdateTime = prefs.getString("update_end", null);
        if (lastUpdateTime != null && !lastUpdateTime.equals("")) {
            if (DateTools.compareToNow(DateTools.parseSimpleDate(lastUpdateTime)) == -1) {
                doUpdates = false;
            }
        }

        // Do not update on weekends
        Boolean doWeekendUpdates = prefs.getBoolean("update_weekend", false);
        if (!doWeekendUpdates) {
            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1 || dayOfWeek == 7) {
                doUpdates = false;
            }
        }

        updateWidgets(context, doUpdates ? UpdateType.VIEW_UPDATE : UpdateType.VIEW_NO_UPDATE
        , Notification.CHECK_FOR_NOTIFICATIONS);
    }

    private void handleTouch(Context context, int appWidgetId, String action) {
        if (action.equals("LEFT")) {
            startPreferencesActivity(context, appWidgetId);
        } else if (action.equals("RIGHT")) {
            UpdateType updateType = getUpdateTypeForTouchRight(context, appWidgetId);
            updateWidgetAsync(context, appWidgetId, updateType, Notification.DONT_CHECK);
        }
    }

    private UpdateType getUpdateTypeForTouchRight(Context context, int widgetId) {
        WidgetRepository repository = new AndroidWidgetRepository(context);
        Widget widget = repository.getWidget(widgetId);

        if (widget.shouldUpdateOnRightTouch()) {
            return UpdateType.VIEW_UPDATE;
        }

        return UpdateType.VIEW_CHANGE;
    }

    private void startPreferencesActivity(Context context, int appWidgetId) {
        PreferencesActivity.mAppWidgetId = appWidgetId;
        Intent activity = new Intent(context, PreferencesActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);
    }

    @Override
    public void onReceive(@SuppressWarnings("NullableProblems") Context context,
                          @SuppressWarnings("NullableProblems") Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case CustomAlarmManager.ALARM_UPDATE:
                    //Only check for notifications when update
                    //interval is reached
                    doScheduledUpdates(context, Notification.CHECK_FOR_NOTIFICATIONS);
                    break;

                case "LEFT":
                case "RIGHT":
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        int appWidgetId = extras.getInt(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                AppWidgetManager.INVALID_APPWIDGET_ID);
                        handleTouch(context, appWidgetId, action);
                    }
                    break;

                default:
                    super.onReceive(context, intent);
                    break;
            }
        }
    }

    private void updateWidgetsFromCache(Context context) {
        for (int id : new AndroidWidgetRepository(context).getIds()) {
            updateWidgetAsync(context, id, UpdateType.VIEW_NO_UPDATE, Notification.DONT_CHECK);
        }
    }
    private int getCellsForSize(int size){
        return (int)(Math.ceil(size + 30d)/70d);
    }
    @Override
    public void onAppWidgetOptionsChanged (Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        AndroidWidgetRepository widgets = new AndroidWidgetRepository(context);
        Widget widget = widgets.getWidget(appWidgetId);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        minWidth = getCellsForSize(minWidth);
        minHeight = getCellsForSize(minHeight);


        if(widget.getSize() != 4) {
            if (minHeight > 1 && minHeight < 3) {
                if (minWidth > 3) {
                    widget.setSize(3);
                } else {
                    widget.setSize(2);
                }
            } else if (minHeight >= 3) {
                if (minWidth > 3) {
                    widget.setSize(5);
                } else {
                    widget.setSize(6);
                }
            } else {
                if (minWidth > 3) {
                    widget.setSize(1);
                } else {
                    widget.setSize(0);
                }
            }
        }

        new CustomAlarmManager(context).reinitialize();
        updateWidgetsFromCache(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new CustomAlarmManager(context).reinitialize();

        updateWidgetsFromCache(context);

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        new CustomAlarmManager(context).reinitialize();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        for (int appWidgetId : appWidgetIds) {
            widgetRepository.delWidget(appWidgetId);
        }
        if (widgetRepository.isEmpty()) {
            new CustomAlarmManager(context).cancel();
        }

        UserData.cleanupPreferenceFiles(context);
    }

    public enum ViewType {
        VIEW_DAILY_PERCENT,
        VIEW_DAILY_CHANGE,
        VIEW_PORTFOLIO_PERCENT,
        VIEW_PORTFOLIO_CHANGE,
        VIEW_PORTFOLIO_PERCENT_AER,
        VIEW_PL_DAILY_PERCENT,
        VIEW_PL_DAILY_CHANGE,
        VIEW_PL_PERCENT,
        VIEW_PL_CHANGE,
        VIEW_PL_PERCENT_AER
    }

    public enum UpdateType {
        VIEW_UPDATE,
        VIEW_NO_UPDATE,
        VIEW_CHANGE
    }

    public enum Notification {
        DONT_CHECK,
        CHECK_FOR_NOTIFICATIONS
    }

    private static class GetDataTask extends AsyncTask<Object, Void, Void> {
        private Context context;
        private Integer appWidgetId;
        private UpdateType updateType;
        private HashMap<String, StockQuote> quotes;
        private String currencies;
        private String timeStamp;
        private Notification notification;


        public GetDataTask build(Context context, Integer appWidgetId, UpdateType updateType, Notification notification) {
            this.context = context;
            this.appWidgetId = appWidgetId;
            this.updateType = updateType;
            this.notification = notification;


            WidgetRepository repository = new AndroidWidgetRepository(context);



            return this;
        }

        @Override
        protected Void doInBackground(Object... params) {
            WidgetRepository widgetRepository = new AndroidWidgetRepository(this.context);
            Storage storage = PreferenceStorage.getInstance(this.context);
            StorageCache cache = new StorageCache(storage);

            StockQuoteRepository quoteRepository = new StockQuoteRepository(
                    PreferenceStorage.getInstance(this.context), cache,
                    widgetRepository);


            List<String> spListInWidget = new ArrayList<String>();

            CurrencyRepository currencyRepo = new CurrencyRepository(PreferenceStorage.getInstance(this.context), cache);

            Widget widget = widgetRepository.getWidget(this.appWidgetId);
            //Only update on wifi if option is set
            if (widget.updateOnWifi() && !(widget.isUsingWifi())) {
                updateType = UpdateType.VIEW_NO_UPDATE;
            }

            try {
                spListInWidget = widget.checkSPStock();
            } catch (IOException ignored){

            }


            this.quotes = quoteRepository.getQuotes(
                    widgetRepository.getWidget(this.appWidgetId).getSymbols(),
                    updateType == UpdateType.VIEW_UPDATE);

            this.currencies = currencyRepo.getCurrencies(widget.updateOnCurrency());
            this.timeStamp = quoteRepository.getTimeStamp();

            if (this.notification == Notification.CHECK_FOR_NOTIFICATIONS)
                check5PercentDrop(context, widget.getId(), this.quotes);

            if (widget.getSize() == 4) {
                List<String> symbols = widget.getSymbols();
                String symbol = symbols.get(0);
                GraphTools.drawGraph(context, symbol, appWidgetId, widget.historicalData());
            }

                return null;


        }
            @Override
            protected void onPostExecute (Void result){
                applyUpdate(this.context, this.appWidgetId, this.updateType, this.currencies,this.quotes,
                        this.timeStamp);
            }

        public  void check5PercentDrop(Context context, int appWidgetId, HashMap<String, StockQuote> quotes){

            WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
            Widget widget = widgetRepository.getWidget(appWidgetId);
            List<String> spListInWidget = new ArrayList<>();


            try {
                spListInWidget = widget.checkSPStock();
            } catch (IOException ignored){

            }

            if ((widget.getSize() != 4) && (!spListInWidget.isEmpty())) {
                int i = 1;
                List<StockQuote> quoteList = new ArrayList<>();
                for (String symbol : spListInWidget) {
                    if (quotes.containsKey(symbol)) {
                        quoteList.add(quotes.get(symbol));
                    }
                }
                for (StockQuote stockQuote : quoteList) {
                    if (tryParseDouble(stockQuote.getPercent()) <= -5)
                        widget.sendNotification(context, stockQuote.getSymbol() + " " + stockQuote.getPercent(),
                                stockQuote.getName()+" has dropped! ", i);
                    i++;
                }

            }
        }

        }
    }
