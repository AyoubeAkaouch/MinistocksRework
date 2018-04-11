package nitezh.ministock;


import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.SearchCondition;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class UITests {

    private UiDevice mDevice;
    //*************************
    //UI tests have to be run on a 1080p screen
    //If not the widget will be stuck on loading data...
    //*************************
    @Before
    public void setUp() throws UiObjectNotFoundException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        mDevice.pressHome();
    }

    //This test assumes that footer is visible and widget is on the home screen
    @Test
    public void testRemoveFooterOption() throws UiObjectNotFoundException, InterruptedException{

        UiObject footer = mDevice.findObject(new UiSelector().className("android.widget.TextView")
                .resourceId("nitezh.ministock:id/text5"));
        //assert footer is present
        assertTrue(footer.exists());

        //Remove footer
        UiObject widgetLeft = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout")
                .resourceIdMatches("nitezh.ministock:id/widget_left"));
        widgetLeft.clickAndWaitForNewWindow();

        UiObject advanced = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(2));
        advanced.clickAndWaitForNewWindow();

        UiObject appearance = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
        appearance.clickAndWaitForNewWindow();

        UiObject footerDisplay = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(4));
        footerDisplay.clickAndWaitForNewWindow();

        //Remove footer
        UiObject removeFooter = mDevice.findObject(new UiSelector().className("android.widget.CheckedTextView")
                .text("Remove"));
        removeFooter.click();

        mDevice.pressHome();

        //footer should be gone
        Thread.sleep(3000);
        assertFalse(footer.exists());

        //Set footer back to visible
        widgetLeft.clickAndWaitForNewWindow();
        advanced.clickAndWaitForNewWindow();
        appearance.clickAndWaitForNewWindow();
        footerDisplay.clickAndWaitForNewWindow();

        UiObject visibleFooter = mDevice.findObject(new UiSelector().className("android.widget.CheckedTextView")
                .text("Visible"));
        visibleFooter.click();
        mDevice.pressHome();
    }

    //The widget has to be setup with the default values
    @Test
    public void addStockToWidget() throws UiObjectNotFoundException,InterruptedException {

        //Verify that no stock has been setup in that section
        UiObject stockRow = mDevice.findObject(new UiSelector().className("android.widget.TextView")
                .resourceId("nitezh.ministock:id/text21"));
        assertTrue(stockRow.getText().equals(""));

        //Setup the left click on options
        UiObject widgetLeft = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout")
                .resourceIdMatches("nitezh.ministock:id/widget_left"));
        widgetLeft.clickAndWaitForNewWindow();

        UiObject advanced = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
        advanced.clickAndWaitForNewWindow();

        UiObject stockSlot3 = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
        stockSlot3.clickAndWaitForNewWindow(1000);

        UiObject stockSearch = mDevice.findObject(new UiSelector().className("android.widget.EditText").resourceId("android:id/search_src_text"));
        Thread.sleep(2000);

        stockSearch.setText("amzn");
        Thread.sleep(5000);

        mDevice.click(514,271);
        Thread.sleep(2000);

        mDevice.pressHome();
        widgetLeft.clickAndWaitForNewWindow();
        mDevice.pressHome();

        Thread.sleep(10000);

        //Check if row has been set with a stock
        assertTrue(!stockRow.getText().equals(""));
    }

}

