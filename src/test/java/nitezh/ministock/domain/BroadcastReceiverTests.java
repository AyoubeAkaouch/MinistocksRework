package nitezh.ministock.domain;

/**
 * Created by Giovanni on 2018-03-14.
 */

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


import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;


import android.content.BroadcastReceiver;
import android.content.Intent;



import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.robolectric.shadows.ShadowApplication;


import java.util.List;


@RunWith(RobolectricTestRunner.class)
public class BroadcastReceiverTests {


    Intent intent = new Intent("android.intent.action.USER_PRESENT");

    ShadowApplication shadowApplication = ShadowApplication.getInstance();


    @Test
    public void hasReceiversForUnlockIntent() {

        assertTrue(shadowApplication.hasReceiverForIntent(intent));
    }

    @Test
    public void oneReceiverForUnlockIntent() {

        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);

        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());

    }

}


