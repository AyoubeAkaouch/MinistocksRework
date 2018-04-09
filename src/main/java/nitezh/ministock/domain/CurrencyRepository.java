package nitezh.ministock.domain;


import android.util.Log;

import nitezh.ministock.Storage;
import nitezh.ministock.dataaccess.FixerIORepository;
import nitezh.ministock.utils.Cache;

/**
 * Created by Ayoube on 3/30/2018.
 */

public class CurrencyRepository {

    private Storage appStorage;
    private Cache appCache;
    private FixerIORepository fixerIOrepo;
    private String mCachedCurrencies;
    //private String mCachedTimeStamp;

    public CurrencyRepository(Storage appStorage, Cache appCache){
        this.appStorage=appStorage;
        this.appCache=appCache;
        this.fixerIOrepo= new FixerIORepository();
    }

    String getLiveCurrencies(){
        return fixerIOrepo.getCurrencies();
    }

    public String getCurrencies(boolean noCache){
        String currencies=null;
        if(noCache){
            currencies=getLiveCurrencies();
        }

        if(currencies==null){
            currencies=loadCurrencies();
        }
        else{
            saveCurrencies(currencies);
        }
        return currencies;
    }

    public void saveCurrencies(String currencies){
        mCachedCurrencies=currencies;
        this.appStorage.putString("savedCurrencies", currencies);
        //this.appStorage.putString("savedQuotesTime", timeStamp);
        this.appStorage.apply();
    }

    private String loadCurrencies() {
        if (mCachedCurrencies!=null) {
            return mCachedCurrencies;
        }
        String savedCurrenciesString = this.appStorage.getString("savedCurrencies", "");


        //If not updating and cache is empty this is the values used to convert prices.
        if(savedCurrenciesString.equalsIgnoreCase(""))
            return "{\"success\":true,\"timestamp\":1522454943,\"base\":\"EUR\",\"date\":\"2018-03-31\",\"rates\":{\"AED\":4.526979,\"AFN\":85.268304,\"ALL\":129.745896,\"AMD\":591.170576,\"ANG\":2.194766,\"AOA\":263.669769,\"ARS\":24.79031,\"AUD\":1.604534,\"AWG\":2.194269,\"AZN\":2.095039,\"BAM\":1.958945,\"BBD\":2.465471,\"BDT\":102.193765,\"BGN\":1.956849,\"BHD\":0.4645,\"BIF\":2158.494969,\"BMD\":1.232735,\"BND\":1.622901,\"BOB\":8.457057,\"BRL\":4.073456,\"BSD\":1.232735,\"BTC\":0.000179,\"BTN\":80.312712,\"BWP\":11.745385,\"BYN\":2.404324,\"BYR\":24161.613422,\"BZD\":2.462764,\"CAD\":1.589494,\"CDF\":1929.852068,\"CHF\":1.175048,\"CLF\":0.027293,\"CLP\":743.832557,\"CNY\":7.732708,\"COP\":3438.098971,\"CRC\":693.290407,\"CUC\":1.232735,\"CUP\":32.667488,\"CVE\":110.317487,\"CZK\":25.340158,\"DJF\":217.984599,\"DKK\":7.454233,\"DOP\":60.89713,\"DZD\":140.298842,\"EGP\":21.671972,\"ERN\":18.479187,\"ETB\":33.555056,\"EUR\":1,\"FJD\":2.490608,\"FKP\":0.877959,\"GBP\":0.87952,\"GEL\":2.978664,\"GGP\":0.879051,\"GHS\":5.437645,\"GIP\":0.878206,\"GMD\":58.037184,\"GNF\":11093.386111,\"GTQ\":9.043396,\"GYD\":251.761541,\"HKD\":9.673649,\"HNL\":29.080707,\"HRK\":7.427729,\"HTG\":79.290018,\"HUF\":312.473759,\"IDR\":16962.438811,\"ILS\":4.299046,\"IMP\":0.879051,\"INR\":80.262169,\"IQD\":1459.558688,\"IRR\":46527.131832,\"ISK\":121.362794,\"JEP\":0.879051,\"JMD\":154.363125,\"JOD\":0.873398,\"JPY\":130.97567,\"KES\":124.198564,\"KGS\":84.328347,\"KHR\":4918.614593,\"KMF\":490.850564,\"KPW\":1109.462273,\"KRW\":1306.39179,\"KWD\":0.368839,\"KYD\":1.011315,\"KZT\":393.193265,\"LAK\":10207.049366,\"LBP\":1855.887772,\"LKR\":191.715011,\"LRD\":162.413357,\"LSL\":14.571403,\"LTL\":3.758245,\"LVL\":0.764974,\"LYD\":1.634735,\"MAD\":11.328227,\"MDL\":20.263752,\"MGA\":3920.098932,\"MKD\":61.25462,\"MMK\":1639.538481,\"MNT\":2941.30704,\"MOP\":9.956932,\"MRO\":432.690544,\"MUR\":41.30125,\"MVR\":19.194156,\"MWK\":879.482731,\"MXN\":22.372962,\"MYR\":4.760872,\"MZN\":75.505507,\"NAD\":14.574678,\"NGN\":437.621484,\"NIO\":38.153161,\"NOK\":9.660582,\"NPR\":128.408747,\"NZD\":1.701303,\"OMR\":0.474361,\"PAB\":1.232735,\"PEN\":3.976193,\"PGK\":3.92631,\"PHP\":64.250167,\"PKR\":142.220684,\"PLN\":4.217316,\"PYG\":6845.379979,\"QAR\":4.488764,\"RON\":4.647047,\"RSD\":118.090993,\"RUB\":70.409776,\"RWF\":1039.109624,\"SAR\":4.622269,\"SBD\":9.590193,\"SCR\":16.593077,\"SDG\":22.252354,\"SEK\":10.278059,\"SGD\":1.616252,\"SHP\":0.878205,\"SLL\":9590.681664,\"SOS\":694.030435,\"SRD\":9.126759,\"STD\":24512.572222,\"SVC\":10.786891,\"SYP\":634.834041,\"SZL\":14.559596,\"THB\":38.41249,\"TJS\":10.879881,\"TMT\":4.203628,\"TND\":2.94875,\"TOP\":2.775878,\"TRY\":4.874487,\"TTD\":8.172424,\"TWD\":35.713578,\"TZS\":2774.887751,\"UAH\":32.347429,\"UGX\":4541.397548,\"USD\":1.232735,\"UYU\":34.923845,\"UZS\":10009.811687,\"VEF\":60839.189556,\"VND\":28096.504751,\"VUV\":129.905651,\"WST\":3.132878,\"XAF\":655.494698,\"XAG\":0.075334,\"XAU\":0.00093,\"XCD\":3.332831,\"XDR\":0.847903,\"XOF\":655.494698,\"XPF\":119.398952,\"YER\":307.998942,\"ZAR\":14.58425,\"ZMK\":11096.10212,\"ZMW\":11.625142,\"ZWL\":397.378427}}";
        return savedCurrenciesString;
    }

}