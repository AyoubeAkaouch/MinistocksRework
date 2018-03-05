/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.domain;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import nitezh.ministock.activities.widget.WidgetProviderBase;
import nitezh.ministock.Storage;

@RunWith(RobolectricTestRunner.class)
public class AndroidWidgetTests {

    private Widget widget;

    @Before
    public void setUp() {

        int WIDGET_ID = 1;
        int WIDGET_SIZE = 0; //1x2 widget used

        widget = new AndroidWidgetRepository(RuntimeEnvironment.application)
                .addWidget(WIDGET_ID, WIDGET_SIZE);
    }

    @Test
    public void testNumberOfRowsWhenSizeChange() {
        //No need to setSize for first assertEquals, it is already set to 0 by default
        assertEquals(4,widget.getSymbolCount());
        widget.setSize(1);
        assertEquals(4,widget.getSymbolCount());
        widget.setSize(2);
        assertEquals(10,widget.getSymbolCount());
        widget.setSize(3);
        //We skip 4 because it is already used as the graph widget and does not matter here
        assertEquals(10,widget.getSymbolCount());
        widget.setSize(5);
        assertEquals(15,widget.getSymbolCount());
        widget.setSize(6);
        assertEquals(15,widget.getSymbolCount());
    }

    @Test
    public void testShouldUpdateOnRightTouchReturnsFalseByDefault() {
        // Act and Assert
        assertEquals(false, widget.shouldUpdateOnRightTouch());
    }

    @Test
    public void testAllDefaultColorsHaveExpectedValue(){
        //Making sure that these values will always be the ones the apps start with.
        assertEquals(widget.getFooterColor(), 0xFF888888 );
        assertEquals(widget.getStockNameColor(),0xFFFFFFFF);
        assertEquals(widget.getStockPriceColor(),0xFFFFFFFF);
        assertEquals(widget.getPriceIncreaseColor(),0xFFCCFF66);
        assertEquals(widget.getPriceDecreaseColor(), 0xFFff6666);
        assertEquals(widget.getHighAlertColor(), 0xFFFFEE33);
        assertEquals(widget.getLowAlertColor(),0xFFFF66FF);
    }

    @Test
    public void testColorIsChangedToSetColor(){

        Storage storage = widget.getStorage();
        storage.putInt("stock_name_colour",0x00000000);
        storage.putInt("stock_price_colour",0x00000000);
        storage.putInt("updated_footer_colour", 0x00000000);
        storage.putInt("increase_alert_colour", 0x00000000);
        storage.putInt("decrease_alert_colour", 0x00000000);
        storage.putInt("high_alert_colour", 0x00000000);
        storage.putInt("low_alert_colour", 0x00000000);
        storage.apply();
        
        assertEquals(widget.getFooterColor(), 0x00000000 );
        assertEquals(widget.getStockNameColor(),0x00000000);
        assertEquals(widget.getStockPriceColor(),0x00000000);
        assertEquals(widget.getPriceIncreaseColor(),0x00000000);
        assertEquals(widget.getPriceDecreaseColor(), 0x00000000);
        assertEquals(widget.getHighAlertColor(), 0x00000000);
        assertEquals(widget.getLowAlertColor(),0x00000000);
    }

    @Test
    public void testShouldUpdateOnRightTouchReturnsTrueIfSet() {
        // Arrange
        Storage storage = widget.getStorage();
        storage.putBoolean("update_from_widget", true);
        storage.apply();

        // Act and Assert
        assertEquals(true, widget.shouldUpdateOnRightTouch());
    }

      
    @Test
    public void testMediumFontSize(){
        Storage storage = widget.getStorage();
        storage.putString("font_size_custom", "medium");
        storage.apply();

        assertEquals("medium", widget.getFontSize());
    }

    @Test
    public void testLargeFontSize(){
        Storage storage = widget.getStorage();
        storage.putString("font_size_custom", "large");
        storage.apply();

        assertEquals("large", widget.getFontSize());
    }

    @Test
    public void testSmallFontSize(){
        Storage storage = widget.getStorage();
        storage.putString("font_size_custom", "small");
        storage.apply();

        assertEquals("small", widget.getFontSize());
    }

    @Test
    public void testShouldSerif(){
        Storage storage = widget.getStorage();
        storage.putString("font","Serif");
        storage.apply();
        assertEquals("Serif",widget.getFont());
    }

    @Test
    public void testShouldMonospace(){
        Storage storage = widget.getStorage();
        storage.putString("font","Monospace");
        storage.apply();
        assertEquals("Monospace",widget.getFont());
    }

    @Test
    public void testShouldSansserif(){
        Storage storage = widget.getStorage();
        storage.putString("font","Sans-serif");
        storage.apply();
        assertEquals("Sans-serif",widget.getFont());
    }

  @Test
    public void testCheckIfBoldWasSetToFalse(){
        Storage storage = widget.getStorage();
        storage.getBoolean("show_bold",false);
        storage.apply();

        assertEquals(false,widget.useBold());
    }


    @Test
    public void testCheckIfBoldWasSetToTrue(){
        Storage storage = widget.getStorage();
        storage.putBoolean("show_bold",true);
        storage.apply();

        assertEquals(true,widget.useBold());

    }
   @Test
    public void testShouldUnderlined(){
        Storage storage = widget.getStorage();
        storage.putBoolean("show_underlined",true);
        storage.apply();

        assertEquals(true,widget.useUnderlined());

    }

      @Test
       public void testShouldItalic(){
        Storage storage = widget.getStorage();
        storage.putBoolean("show_italic",true);
        storage.apply();

        assertEquals(true,widget.useItalic());
    }

}


