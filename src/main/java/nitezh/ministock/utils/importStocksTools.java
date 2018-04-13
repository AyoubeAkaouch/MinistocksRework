package nitezh.ministock.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;

/**
 * Created by Ayoube on 2018-04-13.
 */

public class importStocksTools {

    static public List<String> startImportFromCSV(Uri uri, Context context, int widgetId){
        List<String> stockSymbols= new ArrayList<String>();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] list=line.split(",");
                stockSymbols= Arrays.asList(list);
            }
            reader.close();
        }
        catch (IOException e) {
            Log.d("Exception", "startImportFromCSV: Error opening file");
        }

        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        Widget widget = widgetRepository.getWidget(widgetId);
        for (int i = 0;i<stockSymbols.size();i++)
            widget.setStock(i,stockSymbols.get(i));


        return stockSymbols;

    }
}
