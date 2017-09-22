package main.java.bksport.crawler.threads;

import main.java.bksport.crawler.footbal.Crawler;
import main.java.bksport.crawler.models.Club;
import main.java.bksport.crawler.models.Player;
import main.java.bksport.crawler.utilities.Constant;
import org.javalite.activejdbc.Base;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by levinhthien.bka@gmail.com on 3/26/2017.
 */
public class RunnableClubDeep implements Runnable {
    static Logger Logger = LoggerFactory.getLogger(RunnableClubDeep.class);
    private Club club = null;
    private Thread thread;

    public RunnableClubDeep(Club club){
        this.club = club;
    }

    public void start(){
        Logger.info(String.format("CLUB CRAWLER DEEP: %s -> START", club.get("name")));
        if (thread == null) {
            thread = new Thread (this);
            thread.start ();
        }
    }

    @Override
    public void run() {
        try {
            Document docs = Jsoup.connect(String.format("%s", club.get("link"))).get();
            Base.open(Crawler.dataSourcePooled);
            Element clubInfo = docs.select(".dataContent").first();
            String stadium = clubInfo.select("div.dataDaten:nth-child(2) > p:nth-child(2) > span:nth-child(2) > a:nth-child(1)").text();
            String link_stadium = clubInfo.select("div.dataDaten:nth-child(2) > p:nth-child(2) > span:nth-child(2) > a:nth-child(1)").attr("href");
            String current_a_national_players = clubInfo.select("div.dataDaten:nth-child(2) > p:nth-child(1) > span:nth-child(2)").text();
            String current_transfer_record = clubInfo.select("div.dataDaten:nth-child(2) > p:nth-child(3) > span:nth-child(2)").text();
            String manager = docs.select(".mitarbeiterVereinSlider > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(1)").text();

            club.set("manager", manager);
            club.set("stadium", stadium);
            club.set("link_stadium", Constant.BASE_URL + link_stadium);
            club.set("current_a_national_players", current_a_national_players);
            club.set("current_transfer_record", current_transfer_record);
            club.saveIt();
            Logger.info(String.format("CLUB CRAWLER: (%s) -> COMPLETED", club.get("name")));

            Elements players =  docs.select("#yw1 > table > tbody > tr");
            for(Element player: players){
                String name = player.select("table > tbody > tr:nth-child(1) > td:nth-child(2) > div:nth-child(1) > span:nth-child(1) > a:nth-child(1)").text();
                String link_player = player.select("table > tbody > tr:nth-child(1) > td:nth-child(2) > div:nth-child(1) > span:nth-child(1) > a:nth-child(1)").attr("href");
                String position = player.select(".posrela table tr:nth-child(2)").text();
                Player modelPlayer = new Player();
                modelPlayer.set("name", name);
                modelPlayer.set("common_name", name);
                modelPlayer.set("link_player", Constant.BASE_URL + link_player);
                modelPlayer.set("club_id", club.get("id"));
                modelPlayer.set("current_club", club.get("name"));
                modelPlayer.set("position", position);
                modelPlayer.saveIt();
                Logger.info(String.format("PLAYER CRAWLER: (%s) -> START", modelPlayer.get("name")));

//                RunnablePlayer runnablePlayer = new RunnablePlayer(modelPlayer);
//                runnablePlayer.start();
            }
            Base.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
