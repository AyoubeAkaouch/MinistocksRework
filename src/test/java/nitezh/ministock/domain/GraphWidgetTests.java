package nitezh.ministock.domain;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import nitezh.ministock.Storage;
import nitezh.ministock.activities.PreferencesActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@RunWith(RobolectricTestRunner.class)
public class GraphWidgetTests {

    private PreferencesActivity preferencesActivity;
    private SharedPreferences sharedPreferences;
    private PreferenceScreen screen;

    private Widget widget;

    @Before
    public void setUp() {

        //Setup activity to set the widget size to 4
        preferencesActivity = Robolectric.setupActivity(PreferencesActivity.class);

        //Set the widget size to 4
        screen = preferencesActivity.getPreferenceScreen();
        sharedPreferences = screen.getSharedPreferences();
        sharedPreferences.edit().putInt("widgetSize",4).commit();

        int WIDGET_ID = 1;
        int WIDGET_SIZE = 4; //select graph widget

        widget = new AndroidWidgetRepository(RuntimeEnvironment.application)
                .addWidget(WIDGET_ID, WIDGET_SIZE);
    }



    @Test
    public void DefaultIsDowJones(){

        assertEquals(widget.getStock(0),"^DJI");

    }

    @Test
    public void TestIfStockChanged(){

        Storage storage = widget.getStorage();

        //display what current stock name is
        System.out.println("Current stock is "+widget.getStock(0));

        //change the stock located in storage
        storage.putString("Stock1","GOOGL");
        storage.apply();

        //display and assert the current stock is equal to the changed one
        System.out.println("Current stock is now: "+widget.getStock(0));
        assertEquals(widget.getStock(0),"GOOGL");
    }

    @Test
    public void TestIfUnneededPreferencesAreGone(){
        //Re-setup the activity with new widget size (4)
        preferencesActivity = Robolectric.setupActivity(PreferencesActivity.class);

        PreferenceScreen advanced = (PreferenceScreen) preferencesActivity.findPreference("advanced");
        Preference portfolio = preferencesActivity.findPreference("portfolio");
        Preference update_now = preferencesActivity.findPreference("update_now");

        //Assert
        assertNull(advanced);
        assertNull(portfolio);
        assertNull(update_now);

    }


}
