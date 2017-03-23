package ru.splat.service;


import com.google.gson.Gson;
import org.apache.log4j.Logger;
import ru.splat.Constant;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.bet.NewResponseClone;
import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class BootService
{
    private static final String URL_ADRESS = "http://172.17.51.54:8080/SpringMVC/dobet";
    private Logger LOGGER = Logger.getLogger(BootService.class);
    private int punterId;


    public BootService(int punterId)
    {
        this.punterId = punterId;
    }

    private BetInfo generateBet()
    {

        int eventId1 = ThreadLocalRandom.current().nextInt(Constant.EVENT_COUNT - 1);
        int eventId2 = ThreadLocalRandom.current().nextInt(Constant.EVENT_COUNT - 1);

        while (eventId1 == eventId2)
        {
            eventId2 = ThreadLocalRandom.current().nextInt(Constant.EVENT_COUNT);
        }

        int outcomeId1 = ThreadLocalRandom.current().nextInt(Constant.OUTCOME_COUNT-1) + eventId1*Constant.MARKET_COUNT*Constant.OUTCOME_COUNT;
        int outcomeId2 = ThreadLocalRandom.current().nextInt(Constant.OUTCOME_COUNT-1) + eventId2*Constant.MARKET_COUNT*Constant.OUTCOME_COUNT;

        BetOutcome betOutcome1 = new BetOutcome(null,eventId1,outcomeId1,Math.random() + 1);
        BetOutcome betOutcome2 = new BetOutcome(null,eventId2,outcomeId2,Math.random() + 1);
        Set<BetOutcome> set = new HashSet<>(2);
        set.add(betOutcome1);
        set.add(betOutcome2);
        BetInfo betInfo = new BetInfo(-1L,punterId, ThreadLocalRandom.current().nextInt(Constant.BET_SUM) + 50 ,set);
        return betInfo;
    }


    public NewResponseClone doRequest() throws Exception
    {
        Gson g = new Gson();

        NewResponseClone newResponse;
        URL url = new URL(URL_ADRESS);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        BetInfo betInfo = generateBet();
        String json = g.toJson(betInfo);
        LOGGER.info("JSON for Server: " + json);


        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((null != (inputLine = in.readLine())) && !(Thread.currentThread().isInterrupted())) {
            response.append(inputLine);
        }
        in.close();

        newResponse = g.fromJson(response.toString(), NewResponseClone.class);


        return newResponse;
    }
}
