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

    boolean useLargeFont();

    boolean useBold();

    boolean useUnderlined();

    boolean useItalic();

    boolean getHideSuffix();

    /*String getTextStyle();*/

    boolean getColorsOnPrices();

    String getFooterVisibility();

    int getFooterColor();

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

    String getFont();
}
