package nitezh.ministock;



import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;




@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class UITests {

    private UiDevice mDevice;

    @Before
    public void setUp() throws UiObjectNotFoundException {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        mDevice.pressHome();
    }

    //This test assumes that footer is visible and widget is on the home screen
    @Test
    public void testRemoveFooterOption() throws UiObjectNotFoundException{

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

        UiObject footerDisplay = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(3));
        footerDisplay.clickAndWaitForNewWindow();

        //Remove footer
        UiObject removeFooter = mDevice.findObject(new UiSelector().className("android.widget.CheckedTextView")
                .text("Remove"));
        removeFooter.click();

        mDevice.pressHome();

        //footer should be gone
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

    @Test
    public void WidgetMonkeyTest() throws UiObjectNotFoundException, InterruptedException, RemoteException{


        mDevice.pressHome();

        //open menu
        UiObject widgetLeft = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").resourceIdMatches("nitezh.ministock:id/widget_left"));
        widgetLeft.clickAndWaitForNewWindow();


        //get widget list
       // UiObject swipeWidget = mDevice.findObject(new UiSelector().className("android.widget.ListView").resourceId("android:id/list"));

        //get os bar at top of screen
        UiObject swipeOS = mDevice.findObject(new UiSelector().className("android.widget.FrameLayout").index(0));


        //swipe Widget on widget
      //  swipeWidget.swipeUp(4);

        //swipe down in OS
        swipeOS.swipeDown(4);

        //swipe up in OS
        swipeOS.swipeUp(4);

        //wait for os notification pane to be hidden
//        mDevice.wait(5000);

        //get the 2 the list items in preferences home
        UiObject stockSetup = mDevice.findObject(new UiSelector().className("android.widget.RelativeLayout").index(0));
        UiObject portfolioSetup = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(3));

        //select stocks setup and go back
        stockSetup.click();
        mDevice.pressBack();

        portfolioSetup.click();
        mDevice.pressBack();

        stockSetup.click();
        mDevice.pressBack();

        portfolioSetup.click();
        mDevice.pressBack();

        stockSetup.click();
        mDevice.pressBack();

        //select portfolio and go back to home
        portfolioSetup.click();
        mDevice.pressHome();

        //open widget again
        widgetLeft.clickAndWaitForNewWindow();
        UiObject settings = mDevice.findObject(new UiSelector().className("android.widget.LinearLayout").index(2));

        //to check if app has crashed, the widget settings wouldnt have opened and these aaserts
        //should return false
        assertTrue(settings.isClickable());
        assertTrue(portfolioSetup.isClickable());
        assertTrue(stockSetup.exists());




    }

}

