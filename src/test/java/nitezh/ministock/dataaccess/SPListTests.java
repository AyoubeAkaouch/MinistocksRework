package nitezh.ministock.dataaccess;


import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class SPListTests {

    @Test
    public void testList() throws IOException {
        List <String> spListActual = new SPList().getSPList();
        List <String> spListExpected = new ArrayList<>();
        spListExpected.add("MMM");
        spListExpected.add("ABT");
        spListExpected.add("ABBV");
        spListExpected.add("ACN");
        spListExpected.add("ATVI");
        spListExpected.add("AYI");
        spListExpected.add("ADBE");
        spListExpected.add("AMD");
        spListExpected.add("AAP");
        spListExpected.add("AES");
        spListExpected.add("AET");
        spListExpected.add("AMG");
        spListExpected.add("AFL");
        spListExpected.add("A");
        spListExpected.add("APD");
        spListExpected.add("AKAM");
        spListExpected.add("ALK");
        spListExpected.add("ALB");
        spListExpected.add("ARE");
        spListExpected.add("ALXN");
        spListExpected.add("ALGN");
        spListExpected.add("ALLE");
        spListExpected.add("AGN");
        spListExpected.add("ADS");
        spListExpected.add("LNT");
        spListExpected.add("ALL");
        spListExpected.add("GOOGL");
        spListExpected.add("GOOG");
        spListExpected.add("MO");
        spListExpected.add("AMZN");
        spListExpected.add("AEE");
        spListExpected.add("AAL");
        spListExpected.add("AEP");
        spListExpected.add("AXP");
        spListExpected.add("AIG");
        spListExpected.add("AMT");
        spListExpected.add("AWK");
        spListExpected.add("AMP");
        spListExpected.add("ABC");
        spListExpected.add("AME");
        spListExpected.add("AMGN");
        spListExpected.add("APH");
        spListExpected.add("APC");
        spListExpected.add("ADI");
        spListExpected.add("ANDV");
        spListExpected.add("ANSS");
        spListExpected.add("ANTM");
        spListExpected.add("AON");
        spListExpected.add("AOS");
        spListExpected.add("APA");
        spListExpected.add("AIV");
        spListExpected.add("AAPL");
        spListExpected.add("AMAT");
        spListExpected.add("APTV");
        spListExpected.add("ADM");
        spListExpected.add("ARNC");
        spListExpected.add("AJG");
        spListExpected.add("AIZ");
        spListExpected.add("T");
        spListExpected.add("ADSK");
        spListExpected.add("ADP");
        spListExpected.add("AZO");
        spListExpected.add("AVB");
        spListExpected.add("AVY");
        spListExpected.add("BHGE");
        spListExpected.add("BLL");
        spListExpected.add("BAC");
        spListExpected.add("BK");
        spListExpected.add("BAX");
        spListExpected.add("BBT");
        spListExpected.add("BDX");
        spListExpected.add("BRK.B");
        spListExpected.add("BBY");
        spListExpected.add("BIIB");
        spListExpected.add("BLK");
        spListExpected.add("HRB");
        spListExpected.add("BA");
        spListExpected.add("BKNG");
        spListExpected.add("BWA");
        spListExpected.add("BXP");
        spListExpected.add("BSX");
        spListExpected.add("BHF");
        spListExpected.add("BMY");
        spListExpected.add("AVGO");
        spListExpected.add("BF.B");
        spListExpected.add("CHRW");
        spListExpected.add("CA");
        spListExpected.add("COG");
        spListExpected.add("CDNS");
        spListExpected.add("CPB");
        spListExpected.add("COF");
        spListExpected.add("CAH");
        spListExpected.add("KMX");
        spListExpected.add("CCL");
        spListExpected.add("CAT");
        spListExpected.add("CBOE");
        spListExpected.add("CBRE");
        spListExpected.add("CBS");
        spListExpected.add("CELG");
        spListExpected.add("CNC");
        for (String actual : spListActual) {
            System.out.print(actual+ " ");
        }
        System.out.println();
        for (String expected : spListExpected) {
            System.out.print(expected+ " ");
        }
        assertTrue(spListActual.containsAll(spListExpected));
    }
}
