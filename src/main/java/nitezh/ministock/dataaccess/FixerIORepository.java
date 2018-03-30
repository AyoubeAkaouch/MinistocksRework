package nitezh.ministock.dataaccess;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Ayoube on 3/30/2018.
 */

public class FixerIORepository {
    private static final String BASE_URL = "http://data.fixer.io/api/latest?access_key=";
    private static final String accessKey = "592580e956e8af7f22b239c236e45f3f";
    private static final String url = BASE_URL + accessKey;

    public FixerIORepository(){

    }

    private String inputStreamToString(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

    //Return a basic value to keep our API calls for real demo.
    public String getCurrencies() {
//        try {
//            URLConnection connection = new URL(url).openConnection();
//            connection.setConnectTimeout(30000);
//            connection.setReadTimeout(60000);
//            return inputStreamToString(connection.getInputStream());
//        } catch (IOException ignored) {
//        }
//        return null;
        return "{\"success\":true,\"timestamp\":1522438448,\"base\":\"EUR\",\"date\":\"2018-03-30\",\"rates\":{\"AED\":4.525864,\"AFN\":85.161016,\"ALL\":129.713915,\"AMD\":591.02486,\"ANG\":2.194225,\"AOA\":263.604778,\"ARS\":24.782964,\"AUD\":1.603522,\"AWG\":2.193728,\"AZN\":2.094523,\"BAM\":1.959078,\"BBD\":2.464863,\"BDT\":102.168576,\"BGN\":1.956367,\"BHD\":0.464385,\"BIF\":2157.962929,\"BMD\":1.232432,\"BND\":1.622501,\"BOB\":8.454972,\"BRL\":4.072329,\"BSD\":1.232432,\"BTC\":0.000181,\"BTN\":80.292916,\"BWP\":11.742489,\"BYN\":2.403731,\"BYR\":24155.657915,\"BZD\":2.462157,\"CAD\":1.588901,\"CDF\":1929.376385,\"CHF\":1.174759,\"CLF\":0.027287,\"CLP\":743.649213,\"CNY\":7.730802,\"COP\":3437.005101,\"CRC\":692.811412,\"CUC\":1.232432,\"CUP\":32.659435,\"CVE\":110.314948,\"CZK\":25.337682,\"DJF\":217.930869,\"DKK\":7.455792,\"DOP\":60.88212,\"DZD\":140.270429,\"EGP\":21.66663,\"ERN\":18.474632,\"ETB\":33.546785,\"EUR\":1,\"FJD\":2.489994,\"FKP\":0.876387,\"GBP\":0.878367,\"GEL\":2.977929,\"GGP\":0.878427,\"GHS\":5.436305,\"GIP\":0.876757,\"GMD\":58.022879,\"GNF\":11085.722017,\"GTQ\":9.041167,\"GYD\":251.699486,\"HKD\":9.672744,\"HNL\":29.022579,\"HRK\":7.425898,\"HTG\":79.270474,\"HUF\":312.396738,\"IDR\":16958.257802,\"ILS\":4.30267,\"IMP\":0.878427,\"INR\":80.242385,\"IQD\":1459.198927,\"IRR\":46515.663531,\"ISK\":121.33288,\"JEP\":0.878427,\"JMD\":154.325077,\"JOD\":0.873183,\"JPY\":130.921205,\"KES\":124.16795,\"KGS\":84.307561,\"KHR\":4914.197347,\"KMF\":490.729577,\"KPW\":1109.188805,\"KRW\":1306.217196,\"KWD\":0.368748,\"KYD\":1.011066,\"KZT\":393.096349,\"LAK\":10204.533468,\"LBP\":1860.972035,\"LKR\":191.667756,\"LRD\":162.373324,\"LSL\":14.567811,\"LTL\":3.757319,\"LVL\":0.764786,\"LYD\":1.634332,\"MAD\":11.330857,\"MDL\":20.258758,\"MGA\":3919.132681,\"MKD\":61.251848,\"MMK\":1639.134357,\"MNT\":2940.582048,\"MOP\":9.954478,\"MRO\":432.583892,\"MUR\":41.29107,\"MVR\":19.189425,\"MWK\":879.265951,\"MXN\":22.379772,\"MYR\":4.759698,\"MZN\":75.486896,\"NAD\":14.553832,\"NGN\":438.746047,\"NIO\":38.143757,\"NOK\":9.665842,\"NPR\":128.377096,\"NZD\":1.702979,\"OMR\":0.474245,\"PAB\":1.232432,\"PEN\":3.975213,\"PGK\":3.925342,\"PHP\":64.28409,\"PKR\":142.185629,\"PLN\":4.210033,\"PYG\":6843.692686,\"QAR\":4.487658,\"RON\":4.655145,\"RSD\":118.061885,\"RUB\":70.335482,\"RWF\":1038.853498,\"SAR\":4.62113,\"SBD\":9.587829,\"SCR\":16.588987,\"SDG\":22.246869,\"SEK\":10.278935,\"SGD\":1.615324,\"SHP\":0.876757,\"SLL\":9588.317692,\"SOS\":693.859366,\"SRD\":9.145099,\"STD\":24512.446842,\"SVC\":10.784232,\"SYP\":634.677563,\"SZL\":14.552187,\"THB\":38.403022,\"TJS\":10.877199,\"TMT\":4.202592,\"TND\":2.98138,\"TOP\":2.726513,\"TRY\":4.874764,\"TTD\":8.17041,\"TWD\":35.851887,\"TZS\":2774.203779,\"UAH\":32.339456,\"UGX\":4541.510587,\"USD\":1.232432,\"UYU\":34.915237,\"UZS\":10007.344406,\"VEF\":60824.193529,\"VND\":28089.579347,\"VUV\":129.873631,\"WST\":3.158603,\"XAF\":655.530358,\"XAG\":0.075316,\"XAU\":0.00093,\"XCD\":3.33201,\"XDR\":0.847694,\"XOF\":655.530358,\"XPF\":119.368042,\"YER\":307.923024,\"ZAR\":14.559828,\"ZMK\":11093.367083,\"ZMW\":11.622277,\"ZWL\":397.280478}}";
    }

}
