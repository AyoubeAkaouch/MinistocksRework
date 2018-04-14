package nitezh.ministock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.Storage;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;

/**
 * Created by Ayoube on 2018-04-13.
 */

public class importStocksTools {

    public static boolean startImportFromCSV(Uri uri, Context context, int widgetId, SharedPreferences sharedPreferences){
        List<String> stockSymbols= new ArrayList<String>();

        String type = getFileExtension(context,uri);

        //If file was not csv return false to tell preferenceActivity we did not do the import.
        if(!type.equalsIgnoreCase(".csv"))
            return false;

        //Read stock symbols from csv file
        try {
           InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
        setNewListOfStocks (stockSymbols, widgetId, sharedPreferences,context);

        return true;

    }

    public static String getFileExtension(Context context, Uri uri){
        //Find file name to make sure it is a csv format
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String displayName="";
        if (cursor != null && cursor.moveToFirst())
            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        int from=displayName.lastIndexOf(".");
        int end=displayName.length();

        return displayName.substring(from,end);
    }

    public static void setNewListOfStocks (List<String> stockSymbols, int widgetId, SharedPreferences sharedPreferences,Context context){

        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        Widget widget = widgetRepository.getWidget(widgetId);
        for (int i = 0;i<stockSymbols.size();i++){
            //Set stock symbols
            widget.setStock(i,stockSymbols.get(i));

            //Update stock summery for stock setup preferences.
            SharedPreferences.Editor editor= sharedPreferences.edit();
            editor.putString("Stock"+(i+1)+"_summary","This stock was added from your csv file.");
            editor.apply();
        }

        //Emptying the rest of stock slots
        for (int i=stockSymbols.size();i<16;i++)
            widget.setStock(i,"");

    }

}
