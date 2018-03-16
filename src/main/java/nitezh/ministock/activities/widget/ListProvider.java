package nitezh.ministock.activities.widget;

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
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStock;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.domain.WidgetStock;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.StorageCache;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType.VIEW_CHANGE;
import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType.VIEW_NO_UPDATE;
import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType.VIEW_UPDATE;

/**
 * Created by Ayoube on 2018-03-03.
 */

public class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Widget widget = getWidget();
    private int appWidgetId;
    private boolean hasPortfolioData;
    private List<String> symbols;
    private HashMap<String, PortfolioStock> portfolioStocks;
    private HashMap<String, StockQuote> quotes;
    private WidgetProviderBase.UpdateType updateMode;
    private String quotesTimeStamp;
    private Context context;
    private final HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews;

    private List<String[]> stocks;
    private String fontSize;
    private List<int[]> colors;


    public ListProvider(Context context, Intent intent) {
        this.context = context;

        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        Storage storage = PreferenceStorage.getInstance(this.context);
        StockQuoteRepository quoteRepository = new StockQuoteRepository(
                PreferenceStorage.getInstance(this.context), new StorageCache(storage),
                widgetRepository);


        this.widget = widgetRepository.getWidget(appWidgetId);
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        //this.quotes to get from storage
        this.quotes = quoteRepository.getQuotes(
                widgetRepository.getWidget(this.appWidgetId).getSymbols(),
                updateMode == WidgetProviderBase.UpdateType.VIEW_UPDATE);

        //this.quotesTimeStamp from storage
        this.quotesTimeStamp = quoteRepository.getTimeStamp();

        getUpdatTypeFromInt();//Sets our updateMode from storage
        this.symbols = widget.getSymbols();
        this.portfolioStocks = new PortfolioStockRepository(
                PreferenceStorage.getInstance(context), widgetRepository).getStocksForSymbols(symbols);
        this.hasPortfolioData = !portfolioStocks.isEmpty();
        this.enabledViews = this.calculateEnabledViews(this.widget);


        //To populate the list
        this.stocks = new ArrayList<String[]>();
        this.fontSize= this.widget.getFontSize();
        this.colors  = new ArrayList<int[]>();
        Log.d("ss", "getViewAt: CONSTRUCVTOF");

        populateListItem();
    }


    private void populateListItem() {
        int widgetDisplay = getNextView(getUpdateMode());
        Log.d("", "getViewAt: POPULATE");

        for (String symbol : this.symbols) {
            if (symbol.equals("")) {
                continue;
            }
            Log.d("", "getViewAt: POPULATELOOP");

            WidgetRow rowInfo = getRowInfo(symbol, WidgetProviderBase.ViewType.values()[widgetDisplay]);
            String[] stock = new String[5];
            int[] color = new int[5];
            stock[0] = rowInfo.getSymbol();
            stock[1] = rowInfo.getPrice();
            stock[2] = rowInfo.getVolume();
            stock[3] = rowInfo.getStockInfoExtra();
            stock[4] = rowInfo.getStockInfo();

            color[0] = rowInfo.getSymbolDisplayColor();
            color[1] = rowInfo.getPriceColor();
            color[2] = rowInfo.getVolumeColor();
            color[3] = rowInfo.getStockInfoExtraColor();
            color[4] = rowInfo.getStockInfoColor();
            this.colors.add(color);
            this.stocks.add(stock);
        }
    }


    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d("", "getViewAt: getViewAtDABZ");

        String[] stock = stocks.get(position);
        int[] color = colors.get(position);
        final RemoteViews remoteView;
        if (widget.isNarrow()) {
            if (fontSize.equals("large")) {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item2_large);
            } else if (fontSize.equals("small")) {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item2_small);
            } else {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item2);
            }

            //Set stock info
            remoteView.setTextViewText(
                    R.id.text11, !stock[0].equals("") ? applyFormatting(stock[0]) : "");
            remoteView.setTextViewText(
                    R.id.text12, !stock[1].equals("") ? applyFormatting(stock[1]) : "");
            remoteView.setTextViewText(
                    R.id.text13, !stock[4].equals("") ? applyFormatting(stock[4]) : "");
        } else {
            if (fontSize.equals("large")) {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item4_large);
            } else if (fontSize.equals("small")) {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item4_small);
            } else {
                remoteView = new RemoteViews(context.getPackageName(), R.layout.stock_item4);
            }
            remoteView.setTextViewText(
                    R.id.text11, !stock[0].equals("") ? applyFormatting(stock[0]) : "");
            remoteView.setTextViewText(
                    R.id.text12, !stock[1].equals("") ? applyFormatting(stock[1]) : "");
            remoteView.setTextViewText(
                    R.id.text13, !stock[2].equals("") ? applyFormatting(stock[2]) : "");
            remoteView.setTextViewText(
                    R.id.text14, !stock[3].equals("") ? applyFormatting(stock[3]) : "");
            remoteView.setTextViewText(
                    R.id.text15, !stock[4].equals("") ? applyFormatting(stock[4]) : "");

        }
        //Set colors
        remoteView.setTextColor(
                R.id.text11, color[0]);
        if (!this.widget.getColorsOnPrices()) {
            remoteView.setTextColor(
                    R.id.text12, color[1]);
            if (widget.isNarrow()) {
                remoteView.setTextColor(
                        R.id.text13, color[4]);
            } else {
                remoteView.setTextColor(
                        R.id.text13, color[2]);
                remoteView.setTextColor(
                        R.id.text14, color[3]);
                remoteView.setTextColor(
                        R.id.text15, color[4]);
            }
        } else {
            remoteView.setTextColor(
                    R.id.text12, color[4]);
            if (widget.isNarrow()) {
                remoteView.setTextColor(
                        R.id.text13, color[1]);
            } else {
                remoteView.setTextColor(
                        R.id.text13, color[2]);
                remoteView.setTextColor(
                        R.id.text14, color[1]);
                remoteView.setTextColor(
                        R.id.text15, color[1]);
            }
        }

        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }


    private int getNextView(WidgetProviderBase.UpdateType updateMode) {
        int currentView = this.widget.getPreviousView();
        if (updateMode == VIEW_CHANGE) {
            currentView += 1;
            currentView = currentView % 10;
        }
        // Skip views as relevant
        int count = 0;
        while (!this.getEnabledViews().get(WidgetProviderBase.ViewType.values()[currentView])) {
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

    private HashMap<WidgetProviderBase.ViewType, Boolean> calculateEnabledViews(Widget widget) {
        HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews = new HashMap<>();
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_DAILY_PERCENT, widget.hasDailyPercentView());
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_DAILY_CHANGE, widget.hasDailyChangeView());
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PORTFOLIO_PERCENT, widget.hasTotalPercentView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PORTFOLIO_CHANGE, widget.hasTotalChangeView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PORTFOLIO_PERCENT_AER, widget.hasTotalChangeAerView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PL_DAILY_PERCENT, widget.hasDailyPlPercentView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PL_DAILY_CHANGE, widget.hasDailyPlChangeView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PL_PERCENT, widget.hasTotalPlPercentView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PL_CHANGE, widget.hasTotalPlChangeView() && this.hasPortfolioData);
        enabledViews.put(WidgetProviderBase.ViewType.VIEW_PL_PERCENT_AER, widget.hasTotalPlPercentAerView() && this.hasPortfolioData);
        return enabledViews;
    }

    private void getUpdatTypeFromInt() {
        int updateTypeInt = widget.getUpdateTypeInt();

        switch (updateTypeInt) {
            case 0:
                this.updateMode = VIEW_UPDATE;
                break;
            case 1:
                this.updateMode = VIEW_NO_UPDATE;
                break;
            case 2:
                this.updateMode = VIEW_CHANGE;
                break;
        }
    }
    private WidgetRow getRowInfo(String symbol, WidgetProviderBase.ViewType widgetView) {
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

    private HashMap<WidgetProviderBase.ViewType, Boolean> getEnabledViews() {
        return this.enabledViews;
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

    private void updateWidgetRowWithDefaults(WidgetRow widgetRow, WidgetStock widgetStock) {
        widgetRow.setPrice(widgetStock.getPrice());
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
            widgetRow.setStockInfoExtra(widgetStock.getDailyChange());
            widgetRow.setStockInfoExtraColor(WidgetColors.NA);
        }
    }

    private Widget getWidget() {
        return widget;
    }

    private WidgetProviderBase.UpdateType getUpdateMode() {
        return updateMode;
    }

    private List<String> getSymbols() {
        return symbols;
    }

}