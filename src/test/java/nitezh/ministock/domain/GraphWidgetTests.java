package nitezh.ministock.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import nitezh.ministock.Storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by Giovanni on 2018-03-02.
 */
@RunWith(RobolectricTestRunner.class)
public class GraphWidgetTests {


    private Widget widget;

    @Before
    public void setUp() {

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
}
