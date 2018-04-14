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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.domain.*;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType;
import static nitezh.ministock.activities.widget.WidgetProviderBase.ViewType;

class WidgetView {

    private final RemoteViews remoteViews;
    private final Widget widget;
    private final boolean hasPortfolioData;
    private final List<String> symbols;
    private final HashMap<String, PortfolioStock> portfolioStocks;
    private final HashMap<String, StockQuote> quotes;
    private final UpdateType updateMode;
    private final String quotesTimeStamp;
    private final Context context;
    private final HashMap<ViewType, Boolean> enabledViews;
    private final String currencies;

    public WidgetView(Context context, int appWidgetId, UpdateType updateMode, String currencies,
                      HashMap<String, StockQuote> quotes, String quotesTimeStamp) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);

        this.context = context;
        this.widget = widgetRepository.getWidget(appWidgetId);
        this.quotes = quotes;
        this.quotesTimeStamp = quotesTimeStamp;
        this.updateMode = updateMode;
        this.symbols = widget.getSymbols();

        this.portfolioStocks = new PortfolioStockRepository(
                PreferenceStorage.getInstance(context), widgetRepository).getStocksForSymbols(symbols);
        this.hasPortfolioData = !portfolioStocks.isEmpty();
        this.currencies = currencies;
        this.remoteViews = this.getBlankRemoteViews(this.widget, context.getPackageName());
        this.enabledViews = this.calculateEnabledViews(this.widget);
    }

    private RemoteViews getBlankRemoteViews(Widget widget, String packageName) {
        String backgroundStyle = widget.getBackgroundStyle();
        String fontSize = widget.getFontSize();

        RemoteViews views;
        if (widget.getSize() == 1) {
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_1x4_large);
            } else if (fontSize.equals("medium")) {
                views = new RemoteViews(packageName, R.layout.widget_1x4);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x4_small);
            }
        } else if (widget.getSize() == 2) {
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_2x2_large);
            } else if (fontSize.equals("small")){
                views = new RemoteViews(packageName, R.layout.widget_2x2_small);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x2);
            }
        } else if (widget.getSize() == 3) {
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_2x4_large);
            } else if (fontSize.equals("small")){
                views = new RemoteViews(packageName, R.layout.widget_2x4_small);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x4);
            }
        } else if(widget.getSize()==0){
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_1x2_large);
            } else if (fontSize.equals("small")){
                views = new RemoteViews(packageName, R.layout.widget_1x2_small);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x2);
            }
        }else if(widget.getSize()==4){
            views = new RemoteViews(packageName, R.layout.widget_4x4_graph);
        }else if (widget.getSize()==5) {
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_3x4_large);
            } else if (fontSize.equals("small")) {
                views = new RemoteViews(packageName, R.layout.widget_3x4_small);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_3x4);
            }
        } else {
            if (fontSize.equals("large")) {
                views = new RemoteViews(packageName, R.layout.widget_3x2_large);
            } else if (fontSize.equals("small")) {
                views = new RemoteViews(packageName, R.layout.widget_3x2_small);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_3x2);
            }
        }
        views.setImageViewResource(R.id.widget_bg,
                getImageViewSrcId(backgroundStyle, fontSize));
        this.hideUnusedRows(views, widget.getSymbolCount());
        return views;
    }

    private int getImageViewSrcId(String backgroundStyle, String fontSize) {
        Integer imageViewSrcId;
        switch (backgroundStyle) {
            case "transparent":
                if (fontSize.equals("large")) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }
                break;
            case "none":
                imageViewSrcId = R.drawable.blank;
                break;
            default:
                if (fontSize.equals("large")) {
                    imageViewSrcId = R.drawable.ministock_bg_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg;
                }
                break;
        }
        return imageViewSrcId;
    }

    // Global formatter so we can perform global text formatting in one place
    private SpannableString applyFormatting(String s) {
        SpannableString span = new SpannableString(s);
        //Code to change update the widgets text style
        String FontTypeValue =this.widget.getFont();
        switch(FontTypeValue){
            case "Monospace":
                span.setSpan(new TypefaceSpan("monospace"), 0,s.length(),0);
                break;
            case "Serif":
                span.setSpan(new TypefaceSpan("serif"), 0,s.length(),0);
                break;
            case "Sans-serif":
                span.setSpan(new TypefaceSpan("sans-serif"), 0,s.length(),0);
                break;
        }
        // Code to change the widgets font weight
        boolean bold = this.widget.useBold();
        boolean italic = this.widget.useItalic();
        boolean underlined = this.widget.useUnderlined();

        if(bold){
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        }
        if(italic){
            span.setSpan(new StyleSpan(Typeface.ITALIC), 0, s.length(), 0);
        }
        if (underlined){
            span.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        }

        return span;
    }

    public void setOnClickPendingIntents() {
        Intent leftTouchIntent = new Intent(this.context, WidgetProvider.class);
        leftTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        leftTouchIntent.setAction("LEFT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_left,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), leftTouchIntent, 0));

        Intent rightTouchIntent = new Intent(this.context, WidgetProvider.class);
        rightTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        rightTouchIntent.setAction("RIGHT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_right,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), rightTouchIntent, 0));
    }

    private HashMap<WidgetProviderBase.ViewType, Boolean> getEnabledViews() {
        return this.enabledViews;
    }

    private HashMap<ViewType, Boolean> calculateEnabledViews(Widget widget) {
        HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews = new HashMap<>();
        enabledViews.put(ViewType.VIEW_DAILY_PERCENT, widget.hasDailyPercentView());
        enabledViews.put(ViewType.VIEW_DAILY_CHANGE, widget.hasDailyChangeView());
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT, widget.hasTotalPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_CHANGE, widget.hasTotalChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, widget.hasTotalChangeAerView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_PERCENT, widget.hasDailyPlPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_CHANGE, widget.hasDailyPlChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_PERCENT, widget.hasTotalPlPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_CHANGE, widget.hasTotalPlChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_PERCENT_AER, widget.hasTotalPlPercentAerView() && this.hasPortfolioData);
        return enabledViews;
    }

    private WidgetRow getRowInfo(String symbol, ViewType widgetView) {
        WidgetRow widgetRow = new WidgetRow(this.widget);
        StockQuote quote = this.quotes.get(symbol);
        widgetRow.setSymbol(symbol);

        if (isQuoteMissingPriceOrChange(quote)) {
            updateWidgetRowWithNoData(widgetRow);
            return widgetRow;
        }

        PortfolioStock portfolioStock = this.portfolioStocks.get(symbol);
        WidgetStock widgetStock = new WidgetStock(quote, portfolioStock);

        updateWidgetRowWithDefaults(widgetRow, widgetStock);

        Boolean plView = false;

        String priceColumn = null;
        String stockInfo = null;
        String stockInfoExtra = null;

        Boolean stockInfoIsCurrency = false;
        Boolean stockInfoExtraIsCurrency = false;

        switch (widgetView) {
            case VIEW_DAILY_PERCENT:
                stockInfo = widgetStock.getDailyPercent();
                break;

            case VIEW_DAILY_CHANGE:
                stockInfoExtra = widgetStock.getDailyPercent();
                stockInfo = widgetStock.getDailyChange();
                break;

            case VIEW_PORTFOLIO_PERCENT:
                stockInfo = widgetStock.getTotalPercent();
                break;

            case VIEW_PORTFOLIO_CHANGE:
                stockInfoExtra = widgetStock.getTotalPercent();
                stockInfo = widgetStock.getTotalChange();
                break;

            case VIEW_PORTFOLIO_PERCENT_AER:
                stockInfoExtra = widgetStock.getTotalChangeAer();
                stockInfo = widgetStock.getTotalPercentAer();
                break;

            case VIEW_PL_DAILY_PERCENT:
                plView = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfo = widgetStock.getDailyPercent();
                break;

            case VIEW_PL_DAILY_CHANGE:
                plView = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfoExtra = widgetStock.getDailyPercent();
                stockInfo = widgetStock.getPlDailyChange();
                stockInfoIsCurrency = true;
                break;

            case VIEW_PL_PERCENT:
                plView = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfo = widgetStock.getTotalPercent();
                break;

            case VIEW_PL_CHANGE:
                plView = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfoExtra = widgetStock.getTotalPercent();
                stockInfo = widgetStock.getPlTotalChange();
                stockInfoIsCurrency = true;
                break;

            case VIEW_PL_PERCENT_AER:
                plView = true;
                priceColumn = widgetStock.getPlHolding();
                stockInfoExtra = widgetStock.getPlTotalChangeAer();
                stockInfoExtraIsCurrency = true;
                stockInfo = widgetStock.getTotalPercentAer();
                break;
        }

        SetPriceColumnColourIfLimitTriggered(widgetRow, widgetStock, plView);
        SetPriceColumnColourIfNoHoldings(widgetRow, plView, priceColumn);
        AddCurrencySymbolToPriceColumnIfHaveHoldings(symbol, widgetRow, priceColumn);

        SetStockInfoExtraTextAndColourForWideWidget(symbol, widgetRow, stockInfoExtra, stockInfoExtraIsCurrency);
        SetStockInfoTextAndColour(symbol, widgetRow, stockInfo, stockInfoIsCurrency);

        return widgetRow;
    }

    private void SetStockInfoExtraTextAndColourForWideWidget(String symbol, WidgetRow widgetRow, String stockInfoExtra, Boolean stockInfoExtraIsCurrency) {
        if (!widget.isNarrow()) {
            if (stockInfoExtra != null) {
                String infoExtraText = stockInfoExtra;
                if (stockInfoExtraIsCurrency) {
                    infoExtraText = CurrencyTools.addCurrencyToSymbol(stockInfoExtra, symbol);
                }

                widgetRow.setStockInfoExtra(infoExtraText);
                widgetRow.setStockInfoExtraColor(getColourForChange(stockInfoExtra));
            }

        }
    }

    private void SetStockInfoTextAndColour(String symbol, WidgetRow widgetRow, String stockInfo, Boolean stockInfoIsCurrency) {
        if (stockInfo != null) {
            String infoText = stockInfo;
            if (stockInfoIsCurrency) {
                infoText = CurrencyTools.addCurrencyToSymbol(stockInfo, symbol);
            }

            widgetRow.setStockInfo(infoText);
            widgetRow.setStockInfoColor(getColourForChange(stockInfo));
        }
    }

    private void AddCurrencySymbolToPriceColumnIfHaveHoldings(String symbol, WidgetRow widgetRow, String priceColumn) {
        if (priceColumn != null) {
            widgetRow.setPrice(CurrencyTools.addCurrencyToSymbol(priceColumn, symbol));
        }
    }

    private void SetPriceColumnColourIfNoHoldings(WidgetRow widgetRow, Boolean plView, String priceColumn) {
        if (plView && priceColumn == null) {
            widgetRow.setPriceColor(WidgetColors.NA);
        }
    }

    private void SetPriceColumnColourIfLimitTriggered(WidgetRow widgetRow, WidgetStock widgetStock, Boolean plView) {
        if (widgetStock.getLimitHighTriggered() && !plView) {
            widgetRow.setPriceColor(this.widget.getHighAlertColor());
        }
        if (widgetStock.getLimitLowTriggered() && !plView) {
            widgetRow.setPriceColor(this.widget.getLowAlertColor());
        }
    }

    private void updateWidgetRowWithDefaults(WidgetRow widgetRow, WidgetStock widgetStock) {
        String currencyChange=widget.getCurrencyChange();
        if(!currencyChange.equals("normal"))
        {
            String price=CurrencyTools.changeCurrency(currencies,widgetStock.getShortName(),currencyChange,widgetStock.getPrice());
            widgetRow.setPrice(price);
        }
        else{
            widgetRow.setPrice(widgetStock.getPrice());
        }

        widgetRow.setStockInfo(widgetStock.getDailyPercent());
        widgetRow.setStockInfoColor(WidgetColors.NA);

        if (widget.isNarrow() || widget.alwaysUseShortName()) {
            widgetRow.setSymbol(widgetStock.getShortName());
        } else {
            widgetRow.setSymbol(widgetStock.getLongName());
        }

        if (!widget.isNarrow()) {
            widgetRow.setVolume(widgetStock.getVolume());
            widgetRow.setVolumeColor(WidgetColors.VOLUME);
            if(!currencyChange.equals("normal"))
            {
                String dailyChange=CurrencyTools.changeCurrency(currencies,widgetStock.getShortName(),currencyChange,widgetStock.getDailyChange());
                widgetRow.setStockInfoExtra(dailyChange);
            }
            else{
                widgetRow.setStockInfoExtra(widgetStock.getDailyChange());
            }
            widgetRow.setStockInfoExtraColor(WidgetColors.NA);
        }
    }

    private void updateWidgetRowWithNoData(WidgetRow widgetRow) {
        if (this.widget.isNarrow()) {
            widgetRow.setPrice("—");
            widgetRow.setPriceColor(Color.GRAY);
            widgetRow.setStockInfo("—");
            widgetRow.setStockInfoColor(Color.GRAY);
        } else {
            widgetRow.setStockInfoExtra("—");
            widgetRow.setStockInfoExtraColor(Color.GRAY);
            widgetRow.setStockInfo("—");
            widgetRow.setStockInfoColor(Color.GRAY);
        }
    }

    private boolean isQuoteMissingPriceOrChange(StockQuote quote) {
        return quote == null || quote.getPrice() == null || quote.getPercent() == null;
    }

    private int getColourForChange(String value) {
        double parsedValue = NumberTools.tryParseDouble(value, 0d);
        int colour;
        if (parsedValue < 0) {
            colour = this.widget.getPriceDecreaseColor();
        } else if (parsedValue == 0) {
            colour = this.widget.getStockNameColor();
        } else {
            colour = this.widget.getPriceIncreaseColor();
        }
        return colour;
    }

    private void clear() {
        int columnCount = (!widget.isNarrow()) ? 6 : 4;
        for (int i = 1; i < this.widget.getSymbolCount() + 1; i++) {
            for (int j = 1; j < columnCount; j++) {
                this.setStockRowItemText(i, j, "");
            }
        }
    }

    private void hideUnusedRows(RemoteViews views, int count) {
        for (int i = 0; i < 16; i++) {
            int viewId = ReflectionTools.getField("line" + i);
            if (viewId > 0) {
                views.setViewVisibility(ReflectionTools.getField("line" + i), View.GONE);
            }
        }
        for (int i = 1; i < count + 1; i++) {
            views.setViewVisibility(ReflectionTools.getField("line" + i), View.VISIBLE);
        }
    }

    public RemoteViews getRemoteViews() {
        return remoteViews;
    }

    private int getNextView(UpdateType updateMode) {
        int currentView = this.widget.getPreviousView();
        if (updateMode == UpdateType.VIEW_CHANGE) {
            currentView += 1;
            currentView = currentView % 10;
        }

        // Skip views as relevant
        int count = 0;
        while (!this.getEnabledViews().get(ViewType.values()[currentView])) {
            count += 1;
            currentView += 1;
            currentView = currentView % 10;
            // Percent change as default view if none selected
            if (count > 10) {
                currentView = 0;
                break;
            }
        }
        widget.setView(currentView);
        return currentView;
    }

    private void setStockRowItemText(int row, int col, Object text) {
        this.remoteViews.setTextViewText(
                ReflectionTools.getField("text" + row + col),
                !text.equals("") ? applyFormatting((String) text) : "");
    }

    private void setStockRowItemColor(int row, int col, int color) {
        this.remoteViews.setTextColor(ReflectionTools.getField("text" + row + col), color);
    }

    public void applyPendingChanges() {
        int widgetDisplay = this.getNextView(this.updateMode);
        this.clear();


        int lineNo = 0;
        for (String symbol : this.symbols) {
            if (symbol.equals("")) {
                continue;
            }

            // Get the info for this quote
            lineNo++;
            WidgetRow rowInfo = getRowInfo(symbol, ViewType.values()[widgetDisplay]);

            // Values
            setStockRowItemText(lineNo, 1, rowInfo.getSymbol());
            setStockRowItemText(lineNo, 2, rowInfo.getPrice());

            if (widget.isNarrow()) {
                setStockRowItemText(lineNo, 3, rowInfo.getStockInfo());
            } else {
                setStockRowItemText(lineNo, 3, rowInfo.getVolume());
                setStockRowItemText(lineNo, 4, rowInfo.getStockInfoExtra());
                setStockRowItemText(lineNo, 5, rowInfo.getStockInfo());
            }

            // Colours
            setStockRowItemColor(lineNo, 1, rowInfo.getSymbolDisplayColor());
            if (!this.widget.getColorsOnPrices()) {
                setStockRowItemColor(lineNo, 2, rowInfo.getPriceColor());

                if (widget.isNarrow()) {
                    setStockRowItemColor(lineNo, 3, rowInfo.getStockInfoColor());
                } else {
                    setStockRowItemColor(lineNo, 3, rowInfo.getVolumeColor());
                    setStockRowItemColor(lineNo, 4, rowInfo.getStockInfoExtraColor());
                    setStockRowItemColor(lineNo, 5, rowInfo.getStockInfoColor());
                }
            } else {
                setStockRowItemColor(lineNo, 2, rowInfo.getStockInfoColor());

                if (widget.isNarrow()) {
                    setStockRowItemColor(lineNo, 3, rowInfo.getPriceColor());
                } else {
                    setStockRowItemColor(lineNo, 3, rowInfo.getVolumeColor());
                    setStockRowItemColor(lineNo, 4, rowInfo.getPriceColor());
                    setStockRowItemColor(lineNo, 5, rowInfo.getPriceColor());
                }
            }
        }

        //set Header display

        switch(this.widget.getHeaderVisibility()){
            case "visible":
                remoteViews.setViewVisibility(R.id.text_header, View.VISIBLE);
                //Setup the columns
                int HeaderColor = this.getHeaderColor();


                // Values
                remoteViews.setTextViewText(R.id.text7, applyFormatting("Stock"));
                remoteViews.setTextColor(R.id.text7, HeaderColor);

                remoteViews.setTextViewText(R.id.text8, applyFormatting("Price"));
                remoteViews.setTextColor(R.id.text8, HeaderColor);

                if (widget.isNarrow()) {
                    remoteViews.setTextViewText(R.id.text9, applyFormatting("DailyChange"));
                    remoteViews.setTextColor(R.id.text9, HeaderColor);
                } else {

                    remoteViews.setTextViewText(R.id.text10, applyFormatting("Volume"));
                    remoteViews.setTextColor(R.id.text10, HeaderColor);

                    remoteViews.setTextViewText(R.id.text110, applyFormatting("DC%  "));
                    remoteViews.setTextColor(R.id.text110, HeaderColor);

                    remoteViews.setTextViewText(R.id.text9, applyFormatting("DailyChange"));
                    remoteViews.setTextColor(R.id.text9, HeaderColor);

                }

                break;
            case "invisible":
                remoteViews.setViewVisibility(R.id.text_header, View.GONE);
                break;


        }




        // Set footer display
        switch (this.widget.getFooterVisibility()) {
            case "remove":
                remoteViews.setViewVisibility(R.id.text_footer, View.GONE);
                break;

            case "invisible":
                remoteViews.setViewVisibility(R.id.text_footer, View.INVISIBLE);
                break;

            default:
                remoteViews.setViewVisibility(R.id.text_footer, View.VISIBLE);

                // Set time stamp
                int footerColor = this.getFooterColor();
                remoteViews.setTextViewText(R.id.text5, applyFormatting(this.getTimeStamp()));
                remoteViews.setTextColor(R.id.text5, footerColor);

                // Set the view label
                remoteViews.setTextViewText(R.id.text6, applyFormatting(this.getLabel(widgetDisplay)));
                remoteViews.setTextColor(R.id.text6, footerColor);
                break;
        }
    }


    private int getHeaderColor(){
        int color = this.widget.getHeaderColor();
        return color;
    }


    private int getFooterColor() {
        int color = this.widget.getFooterColor();
        return color;
    }


    private String getLabel(int widgetDisplay) {
        // Set the widget view text in the footer
        String label = "";
        if (widget.isNarrow()) {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "DA";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T%";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF TA";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D%";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L DA";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T%";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L TA";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        } else {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L D";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L T";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        }

        return label;
    }


    private String getTimeStamp() {
        String timeStamp = this.quotesTimeStamp;
        if (!this.widget.showShortTime()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String date = dateFormat.format(new Date()).toUpperCase();

            // Check if we should use yesterdays date or today's time
            String[] parts = timeStamp.split(" ");
            if (parts.length > 2) {
                String fullDate = parts[0] + " " + parts[1];
                if (fullDate.equals(date)) {
                    timeStamp = parts[2];
                } else {
                    timeStamp = fullDate;
                }
            }
        }

        return timeStamp;
    }

    private boolean canChangeView() {
        HashMap<ViewType, Boolean> enabledViews = this.getEnabledViews();
        boolean hasMultipleDefaultViews = enabledViews.get(ViewType.VIEW_DAILY_PERCENT)
                && enabledViews.get(ViewType.VIEW_DAILY_CHANGE);

        return !(this.updateMode == UpdateType.VIEW_CHANGE
                && !this.hasPortfolioData
                && !hasMultipleDefaultViews);
    }

    public boolean hasPendingChanges() {
        return (!this.quotes.isEmpty() || this.canChangeView());
    }
}
