package nitezh.ministock.domain;

import org.json.JSONObject;

import java.util.List;

import nitezh.ministock.Storage;


public interface Widget {
    Storage getStorage();

    void setWidgetPreferencesFromJson(JSONObject jsonPrefs);

    JSONObject getWidgetPreferencesAsJson();

    void enablePercentChangeView();

    void enableDailyChangeView();

    void setStock1();

    void setStock1Summary();

    void save();

    int getId();

    int getSize();

    void setSize(int size);

    boolean isNarrow();

    String getStock(int i);

    int getPreviousView();

    void setView(int view);

    List<String> getSymbols();

    int getSymbolCount();

    String getBackgroundStyle();



    boolean useBold();

    boolean useUnderlined();

    boolean useItalic();

    boolean getHideSuffix();

    /*String getTextStyle();*/

    String getFontSize();

    boolean getColorsOnPrices();

    String getFooterVisibility();

    String getHeaderVisibility();

    int getFooterColor();

    int getHeaderColor();

    int getStockNameColor();

    int getStockPriceColor();

    int getPriceIncreaseColor();

    int getPriceDecreaseColor();

    int getHighAlertColor();

    int getLowAlertColor();

    boolean showShortTime();

    boolean hasDailyChangeView();

    boolean hasTotalPercentView();

    boolean hasDailyPercentView();

    boolean hasTotalChangeView();

    boolean hasTotalChangeAerView();

    boolean hasDailyPlChangeView();

    boolean hasDailyPlPercentView();

    boolean hasTotalPlPercentView();

    boolean hasTotalPlChangeView();

    boolean hasTotalPlPercentAerView();

    boolean alwaysUseShortName();

    boolean shouldUpdateOnRightTouch();

    boolean updateOnWifi();

    boolean isUsingWifi();

    String getFont();
}
