package nitezh.ministock;


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

}

