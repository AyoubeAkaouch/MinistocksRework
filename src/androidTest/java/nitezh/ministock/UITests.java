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
import android.support.test.uiautomator.UiScrollable;
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
    //These tests will run on an empty default configuration of the stock widget!!!
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

        //Select Amazon stock by selecting pixel since dropdown menu not recognized as XML element.
        mDevice.click(514,271);
        Thread.sleep(2000);


        //Need this for widget to update
        mDevice.pressHome();
        widgetLeft.clickAndWaitForNewWindow();
        mDevice.pressHome();

        Thread.sleep(10000);

        //Check if row has been set with a stock
        assertTrue(!stockRow.getText().equals(""));
    }

    @Test
    public void testRemoveHeaderOption() throws UiObjectNotFoundException, InterruptedException{

        UiObject header = mDevice.findObject(new UiSelector().className("android.widget.TextView")
                .resourceId("nitezh.ministock:id/text7"));
        //assert header is present
        assertTrue(header.exists());

        //Remove header
        UiObject widgetLeft = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout")
                .resourceIdMatches("nitezh.ministock:id/widget_left"));
        widgetLeft.clickAndWaitForNewWindow();

        UiObject advanced = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(2));
        advanced.clickAndWaitForNewWindow();

        UiObject appearance = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
        appearance.clickAndWaitForNewWindow();

        UiObject headerDisplay = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(3));
        headerDisplay.clickAndWaitForNewWindow();

        //Remove header
        UiObject removeHeader = mDevice.findObject(new UiSelector().className("android.widget.CheckedTextView")
                .text("Invisible"));
        removeHeader.click();

        //Need this for widget to update
        mDevice.pressHome();
        widgetLeft.clickAndWaitForNewWindow();
        mDevice.pressHome();


        //Header should be gone
        Thread.sleep(5000);
        assertFalse(header.exists());

        //Set footer back to visible
        widgetLeft.clickAndWaitForNewWindow();
        advanced.clickAndWaitForNewWindow();
        appearance.clickAndWaitForNewWindow();
        headerDisplay.clickAndWaitForNewWindow();

        UiObject visibleHeader = mDevice.findObject(new UiSelector().className("android.widget.CheckedTextView")
                .text("Visible"));
        visibleHeader.click();
        mDevice.pressHome();
    }
    @Test
    public void testStockNameToSymbolOption() throws UiObjectNotFoundException, InterruptedException{
        //Verify that a stock has been setup in that section(Should be setup by previous test)
        //If test fail or stock was not setup it is taken care of in next if statement
        UiObject stockRow = mDevice.findObject(new UiSelector().className("android.widget.TextView")
                .resourceId("nitezh.ministock:id/text21"));
        //Setup left click
        UiObject widgetLeft = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout")
                .resourceIdMatches("nitezh.ministock:id/widget_left"));

        if(stockRow.getText().equals("")){
            widgetLeft.clickAndWaitForNewWindow();

            UiObject advanced = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
            advanced.clickAndWaitForNewWindow();

            UiObject stockSlot3 = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
            stockSlot3.clickAndWaitForNewWindow(1000);

            UiObject stockSearch = mDevice.findObject(new UiSelector().className("android.widget.EditText").resourceId("android:id/search_src_text"));
            Thread.sleep(2000);

            stockSearch.setText("amzn");
            Thread.sleep(5000);

            //Select Amazon stock by selecting pixel since dropdown menu not recognized as XML element.
            mDevice.click(514,271);
            Thread.sleep(2000);


            //Need this for widget to update
            mDevice.pressHome();
            widgetLeft.clickAndWaitForNewWindow();
            mDevice.pressHome();

            Thread.sleep(10000);
        }
        //Getting the original length of the string
        int originalStringLength= stockRow.getText().length();

        widgetLeft.clickAndWaitForNewWindow();

        UiObject advanced = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(2));
        advanced.clickAndWaitForNewWindow();

        UiObject appearance = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(1));
        appearance.clickAndWaitForNewWindow();

        //Scroll to end of list
        UiScrollable appearanceScroll = new UiScrollable(new UiSelector().scrollable(true));
        appearanceScroll.scrollToEnd(10);

        UiObject symbolDisplay = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(8));
        symbolDisplay.click();

        //Need this for widget to update
        mDevice.pressHome();
        widgetLeft.clickAndWaitForNewWindow();
        mDevice.pressHome();


        //If symbole is now displayed the length should be shorter than the original stock name.
        Thread.sleep(10000);
        int modifiedStringLength= stockRow.getText().length();
        assertTrue(modifiedStringLength<originalStringLength);

        //reset widget back to full name stocks
        widgetLeft.clickAndWaitForNewWindow();
        advanced.clickAndWaitForNewWindow();
        appearance.clickAndWaitForNewWindow();
        appearanceScroll.scrollToEnd(10);
        symbolDisplay.click();
        mDevice.pressHome();
        widgetLeft.clickAndWaitForNewWindow();
        mDevice.pressHome();


    }


}

