package nitezh.ministock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.util.Log;

import nitezh.ministock.activities.widget.WidgetProviderBase;

/**
 * Created by Giovanni on 2018-03-10.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Storage prefs = PreferenceStorage.getInstance(context);

        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) && prefs.getBoolean("unlock_after_refresh",false)) {
            //update the widget
            WidgetProviderBase.updateWidgets(context, WidgetProviderBase.UpdateType.VIEW_UPDATE, WidgetProviderBase.Notification.DONT_CHECK);
        }

    }

}

