package nitezh.ministock.domain;


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
       // String timeStamp = this.appStorage.getString("savedQuotesTime", "");
        //mTimeStamp = timeStamp;

        return savedCurrenciesString;
    }

}
