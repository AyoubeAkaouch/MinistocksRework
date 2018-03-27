package nitezh.ministock.dataaccess;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SPList {

    private static final String BASE_URL = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies";
    private static  List<String> spSymbols = new ArrayList<String>();

    public List<String> getSPList() throws IOException{
        if (spSymbols.isEmpty()) {
            Document doc = Jsoup.connect(BASE_URL).get();

            //Get the first table from the page
            Element table = doc.select("table").first();
            //Get all table rows
            Elements tableRows = table.select("tr");
            List<Element> tableData = new ArrayList<Element>();


            //Get first table data from each table row
            for (Element tr : tableRows) {
                tableData.add(tr.select("td").first());
            }

            //Retrieve the text inside and add to list
            for (Element td : tableData) {
                if (td != null)
                    spSymbols.add(td.text());
            }

            //Get first 100 only
            spSymbols = spSymbols.subList(0,100);
        }
        return spSymbols;
    }



}
