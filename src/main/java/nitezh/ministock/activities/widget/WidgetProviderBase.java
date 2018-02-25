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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import nitezh.ministock.CustomAlarmManager;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.utils.StorageCache;
import nitezh.ministock.UserData;
import nitezh.ministock.activities.PreferencesActivity;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.utils.DateTools;


public class WidgetProviderBase extends AppWidgetProvider {

    private static void applyUpdate(Context context, int appWidgetId, UpdateType updateMode,
                                    HashMap<String, StockQuote> quotes, String quotesTimeStamp) {
        WidgetView widgetView = new WidgetView(context, appWidgetId, updateMode,
                quotes, quotesTimeStamp);
        widgetView.setOnClickPendingIntents();
        if (widgetView.hasPendingChanges()) {
            widgetView.applyPendingChanges();
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, widgetView.getRemoteViews());
        }
    }

    public static void updateWidgetAsync(Context context, int appWidgetId, UpdateType updateType) {
        try {
            new GetDataTask().build(context, appWidgetId, updateType).execute();
        }
        // usually occurs when queued tasks = 128
        catch (RejectedExecutionException ignored) {
        }
    }

    public static void updateWidgets(Context context, UpdateType updateType) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        for (int appWidgetId : widgetRepository.getIds()) {
            WidgetProviderBase.updateWidgetAsync(context, appWidgetId, updateType);
        }

        CustomAlarmManager alarmManager = new CustomAlarmManager(context);
        alarmManager.setUpdateTimestamp();
        alarmManager.reinitialize();
    }

    private static void doScheduledUpdates(Context context) {
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

        updateWidgets(context, doUpdates ? UpdateType.VIEW_UPDATE : UpdateType.VIEW_NO_UPDATE);
    }

    private void handleTouch(Context context, int appWidgetId, String action) {
        if (action.equals("LEFT")) {
            startPreferencesActivity(context, appWidgetId);
        } else if (action.equals("RIGHT")) {
            UpdateType updateType = getUpdateTypeForTouchRight(context, appWidgetId);
            updateWidgetAsync(context, appWidgetId, updateType);
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
                    doScheduledUpdates(context);
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
            updateWidgetAsync(context, id, UpdateType.VIEW_NO_UPDATE);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new CustomAlarmManager(context).reinitialize();
        updateWidgetsFromCache(context);

        WidgetRepository repository = new AndroidWidgetRepository(context);

        for (int widgetId : appWidgetIds) {
            Widget widget = repository.getWidget(widgetId);
            System.out.println("(From onUpdate) Widget size is: "+ widget.getSize());
            if (widget.getSize() == 4){
                XYPlot plot = new XYPlot(context, "Widget Example");
                final int h = (int) context.getResources().getDimension(R.dimen.sample_widget_height);
                final int w = (int) context.getResources().getDimension(R.dimen.sample_widget_width);

                plot.getGraph().setMargins(0, 0, 0 , 0);
                plot.getGraph().setPadding(0, 0, 0, 0);

                plot.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT, 0,
                        VerticalPositioning.ABSOLUTE_FROM_TOP, Anchor.LEFT_TOP);

                plot.getGraph().setSize(Size.FILL);

                plot.getLayoutManager().moveToTop(plot.getTitle());

                plot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);
                plot.getGraph().getLineLabelInsets().setLeft(PixelUtils.dpToPix(16));
                plot.getGraph().getLineLabelInsets().setBottom(PixelUtils.dpToPix(4));
                plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.RED);
                plot.getGraph().getGridInsets().setTop(PixelUtils.dpToPix(12));
                plot.getGraph().getGridInsets().setRight(PixelUtils.dpToPix(12));
                plot.getGraph().getGridInsets().setLeft(PixelUtils.dpToPix(36));
                plot.getGraph().getGridInsets().setBottom(PixelUtils.dpToPix(16));

                plot.measure(w, h);
                plot.layout(0, 0, w, h);

                Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
                Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

                // Turn the above arrays into XYSeries':
                XYSeries series1 = new SimpleXYSeries(
                        Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                        "Series1");                             // Set the display title of the series

                // same as above
                XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

                // Create a formatter to use for drawing a series using LineAndPointRenderer:
                LineAndPointFormatter series1Format = new LineAndPointFormatter(
                        Color.rgb(0, 200, 0),                   // line color
                        Color.rgb(0, 100, 0),                   // point color
                        null, null);                            // fill color (none)

                // add a new series' to the xyplot:
                plot.addSeries(series1, series1Format);

                // same as above:
                plot.addSeries(series2,
                        new LineAndPointFormatter(
                                Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null, null));


                // reduce the number of range labels
                plot.setLinesPerRangeLabel(3);
                plot.setLinesPerDomainLabel(2);

                // hide the legend:
                plot.getLegend().setVisible(false);

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_4x4_graph);

                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                plot.draw(new Canvas(bitmap));
                rv.setImageViewBitmap(R.id.imgView, bitmap);
                appWidgetManager.updateAppWidget(widgetId, rv);
            }
        }

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
            Widget widget = widgetRepository.getWidget(appWidgetId);
            System.out.println("(From onDeleted) Widget size is: "+widget.getSize());
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

    private static class GetDataTask extends AsyncTask<Object, Void, Void> {
        private Context context;
        private Integer appWidgetId;
        private UpdateType updateType;
        private HashMap<String, StockQuote> quotes;
        private String timeStamp;

        public GetDataTask build(Context context, Integer appWidgetId, UpdateType updateType) {
            this.context = context;
            this.appWidgetId = appWidgetId;
            this.updateType = updateType;

            WidgetRepository repository = new AndroidWidgetRepository(context);

            // for (int widgetId : appWidgetIds) {
            Widget widget = repository.getWidget(appWidgetId);
            System.out.println("(From GetDataTask.build()) Widget size is: "+ widget.getSize());
            if (widget.getSize() == 4){
                XYPlot plot = new XYPlot(context, "Widget Example");
                final int h = (int) context.getResources().getDimension(R.dimen.sample_widget_height);
                final int w = (int) context.getResources().getDimension(R.dimen.sample_widget_width);

                plot.getGraph().setMargins(0, 0, 0 , 0);
                plot.getGraph().setPadding(0, 0, 0, 0);

                plot.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT, 0,
                        VerticalPositioning.ABSOLUTE_FROM_TOP, Anchor.LEFT_TOP);

                plot.getGraph().setSize(Size.FILL);

                plot.getLayoutManager().moveToTop(plot.getTitle());

                plot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);
                plot.getGraph().getLineLabelInsets().setLeft(PixelUtils.dpToPix(16));
                plot.getGraph().getLineLabelInsets().setBottom(PixelUtils.dpToPix(4));
                plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.RED);
                plot.getGraph().getGridInsets().setTop(PixelUtils.dpToPix(12));
                plot.getGraph().getGridInsets().setRight(PixelUtils.dpToPix(12));
                plot.getGraph().getGridInsets().setLeft(PixelUtils.dpToPix(36));
                plot.getGraph().getGridInsets().setBottom(PixelUtils.dpToPix(16));

                plot.measure(w, h);
                plot.layout(0, 0, w, h);

                Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
                Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

                // Turn the above arrays into XYSeries':
                XYSeries series1 = new SimpleXYSeries(
                        Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                        "Series1");                             // Set the display title of the series

                // same as above
                XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

                // Create a formatter to use for drawing a series using LineAndPointRenderer:
                LineAndPointFormatter series1Format = new LineAndPointFormatter(
                        Color.rgb(0, 200, 0),                   // line color
                        Color.rgb(0, 100, 0),                   // point color
                        null, null);                            // fill color (none)

                // add a new series' to the xyplot:
                plot.addSeries(series1, series1Format);

                // same as above:
                plot.addSeries(series2,
                        new LineAndPointFormatter(
                                Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null, null));


                // reduce the number of range labels
                plot.setLinesPerRangeLabel(3);
                plot.setLinesPerDomainLabel(2);

                // hide the legend:
                plot.getLegend().setVisible(false);

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_4x4_graph);

                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                plot.draw(new Canvas(bitmap));
                rv.setImageViewBitmap(R.id.imgView, bitmap);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetId, rv);
            }

            return this;
        }

        @Override
        protected Void doInBackground(Object... params) {
            WidgetRepository widgetRepository = new AndroidWidgetRepository(this.context);
            Storage storage = PreferenceStorage.getInstance(this.context);
            StockQuoteRepository quoteRepository = new StockQuoteRepository(
                    PreferenceStorage.getInstance(this.context), new StorageCache(storage),
                    widgetRepository);

            this.quotes = quoteRepository.getQuotes(
                    widgetRepository.getWidget(this.appWidgetId).getSymbols(),
                    updateType == UpdateType.VIEW_UPDATE);
            System.out.println("this.quotes from doInBackground boi: "+ this.quotes);
            this.timeStamp = quoteRepository.getTimeStamp();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            applyUpdate(this.context, this.appWidgetId, this.updateType, this.quotes,
                    this.timeStamp);
        }
    }
}
