package main.java.bksport.crawler.threads;

import main.java.bksport.crawler.footbal.Crawler;
import main.java.bksport.crawler.models.Club;
import main.java.bksport.crawler.models.Competition;
import main.java.bksport.crawler.utilities.Constant;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by levinhthien.bka@gmail.com on 3/24/2017.
 */
public class RunnableClub implements Runnable {
    static Logger Logger = LoggerFactory.getLogger(RunnableClub.class);
    private Competition competition = null;
    private Thread thread;
    public RunnableClub(Competition competition){
        this.competition = competition;
    }

    public void start(){
        Logger.info(String.format("COMPETITON CRAWLER: %s -> START", competition.get("common_name")));
        if (thread == null) {
            thread = new Thread (this);
            thread.start ();
        }
    }

    @Override
    public void run() {
        try {
            Document docs = Jsoup.connect(String.format("%s", competition.get("link"))).get();
            Elements clubs = docs.select("#yw1 > table:nth-child(2) > tbody > tr");
            Base.open(Crawler.dataSourcePooled);
            for(Element e: clubs){
                String name = e.select("td:nth-child(2)").first().text();
                String image = e.select("img").first().attr("src");
                String squad_size = e.select("td:nth-child(4)").first().text();
                String average_age = e.select("td:nth-child(5)").first().text();
                String foreign_players = e.select("td:nth-child(6)").first().text();
                String market_value = e.select("td:nth-child(8)").first().text();
                String total_market_value = e.select("td:nth-child(7)").first().text();
                String link = e.select("td:nth-child(2) a").first().attr("href");

                Club club = new Club();
                club.set("name", name);
                club.set("common_name", name);
                club.set("competition", competition.get("common_name"));
                club.set("image", image);
                club.set("squad_size", squad_size);
                club.set("average_age", average_age);
                club.set("foreign_players", foreign_players);
                club.set("market_value", market_value);
                club.set("total_market_value", total_market_value);
                club.set("image", image);
                club.set("link", Constant.BASE_URL + link);
                club.saveIt();

                Logger.info(String.format("COMPETITION %s CRAWLER CLUP: %s -> COMPLETED", competition.get("common_name"), club.get("name")));

                RunnableClubDeep clubDeep = new RunnableClubDeep(club);
                clubDeep.start();
            }
            Base.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
