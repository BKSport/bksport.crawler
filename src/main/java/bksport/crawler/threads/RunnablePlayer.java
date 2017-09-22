package main.java.bksport.crawler.threads;

import main.java.bksport.crawler.footbal.Crawler;
import main.java.bksport.crawler.models.Club;
import main.java.bksport.crawler.models.Player;
import org.javalite.activejdbc.Base;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by levinhthien.bka@gmail.com on 3/24/2017.
 */
public class RunnablePlayer implements Runnable {
    static Logger Logger = LoggerFactory.getLogger(RunnablePlayer.class);
    private Player player = null;
    private Thread thread;

    public RunnablePlayer(Player player){
        this.player = player;
    }

    public void start(){
        Logger.info(String.format("CLUB CRAWLER PALYER: %s -> CONTINUE", player.get("name")));
        if (thread == null) {
            thread = new Thread (this);
            thread.start ();
        }
    }
    @Override
    public void run() {
        try {
            Document docs = Jsoup.connect(String.format("%s", player.get("link_player"))).get();
            Base.open(Crawler.dataSourcePooled);
            Element info = docs.select("table.auflistung").first();
            String name_in_home_country = info.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2)").text();
            String date_of_birth = info.select("tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(2)").text();
            String place_of_birth = info.select("tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(2)").text();
            String age = info.select("tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2)").text();
            String height = info.select("tbody:nth-child(1) > tr:nth-child(5) > td:nth-child(2)").text();
            String nation = "";
            for (Element e: info.select("tbody:nth-child(1) > tr:nth-child(6) > td:nth-child(2) img")){
                nation += String.format("[%s]", e.attr("title"));
            }
            String player_s_agent = info.select("tbody:nth-child(1) > tr:nth-child(8) > td:nth-child(2)").text();
            String in_the_team_since = info.select("tbody:nth-child(1) > tr:nth-child(10) > td:nth-child(2)").text();
            String contract_until = info.select("tbody:nth-child(1) > tr:nth-child(11) > td:nth-child(2)").text();
            String current_market_value = docs.select(".zeile-oben > div:nth-child(2)").text();
            String highest_market_value = docs.select(".zeile-unten > div:nth-child(2)").text();;
            String image = docs.select(".dataBild > img:nth-child(1)").attr("src");

            player.set("name_in_home_country", name_in_home_country);
            player.set("date_of_birth", date_of_birth);
            player.set("place_of_birth", place_of_birth);
            player.set("age", age);
            player.set("height", height);
            player.set("nation", nation);
            player.set("in_the_team_since", in_the_team_since);
            player.set("player_s_agent", player_s_agent );
            player.set("contract_until", contract_until );
            player.set("current_market_value", current_market_value);
            player.set("highest_market_value", highest_market_value);
            player.set("image", image);
            player.saveIt();
            Logger.info(String.format("CLUB CRAWLER PALYER: %s -> COMPLETED", player.get("name")));


            Base.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
