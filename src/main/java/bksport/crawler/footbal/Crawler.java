package main.java.bksport.crawler.footbal;

import com.mchange.v2.c3p0.DataSources;
import main.java.bksport.crawler.models.Competition;
import main.java.bksport.crawler.models.Player;
import main.java.bksport.crawler.threads.RunnableClub;
import main.java.bksport.crawler.utilities.Constant;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by levinhthien.bka@gmail.com on 3/24/2017.
 */
public class Crawler {
    static Logger Logger = LoggerFactory.getLogger(Crawler.class);
    public static DataSource dataSourceUnpooled = null;
    public static DataSource dataSourcePooled = null;

    /**
     * Main
     * @param args
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        // Load properties file
        String filename = "database.properties";
        Properties prop = new Properties();
        InputStream input = Crawler.class.getClassLoader().getResourceAsStream(filename);
        if(input==null){
            System.out.println("Sorry, unable to find " + filename);
            return;
        }
        else{
            prop.load(input);
        }

        // Init data pool
        dataSourceUnpooled = DataSources.unpooledDataSource(prop.getProperty("development.url"), prop.getProperty("development.username"), prop.getProperty("development.password"));
        dataSourcePooled = DataSources.pooledDataSource(dataSourceUnpooled);
        // Start
        Base.open(dataSourcePooled);
        crawlerCompetitions();
        Base.close();
    }

    /**
     * Crawler Competition
     * @throws IOException
     */
    public static void crawlerCompetitions() throws IOException {
        Document docs = Jsoup.connect(String.format("%s/wettbewerbe/europa?page=1", Constant.BASE_URL)).get();
        Elements competitions = docs.select(".items tr");
        for (Element competition: competitions){
            Elements elements = competition.select("td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) a");
            String name = elements.text().trim();
            String link = Constant.BASE_URL + elements.attr("href").trim();
            if(name.length() > 4){
                Competition compe  = new Competition();
                compe.set("competition_name", name);
                compe.set("common_name", name);
                compe.set("type", name);
                compe.set("link", link);
                compe.saveIt();
                Logger.info(String.format("COMPETITION SAVED: %s\t%s -> COMPLETED", name, link));
                RunnableClub club = new RunnableClub(compe);
                club.start();
            }
        }
    }
}
