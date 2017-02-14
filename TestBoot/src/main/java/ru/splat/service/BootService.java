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

public class BootService
{
    private static final String URL_ADRESS = "http://localhost:8080/dobet";
    private Logger LOGGER = Logger.getLogger(BootService.class);

    public NewResponseClone makeRequest(int punterCount) throws Exception
    {
        Gson g = new Gson();

        URL url = new URL(URL_ADRESS);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        BetInfo betInfo = generateBet(punterCount);
        String json = g.toJson(betInfo);
        LOGGER.info("JSON for Server: " + json);

//        System.out.println(json);

        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while((null != (inputLine = in.readLine())))
        {
            response.append(inputLine);
        }
        in.close();

        NewResponseClone newResponse = g.fromJson(response.toString(), NewResponseClone.class);
//        System.out.println(transactionId);

        return newResponse;
    }

    private BetInfo generateBet(int punterCount)
    {
        Random random = new Random(System.currentTimeMillis());
        int eventId1 = random.nextInt(Constant.EVENT_COUNT - 1);
        int eventId2 = random.nextInt(Constant.EVENT_COUNT - 1);

        while (eventId1 == eventId2)
        {
            eventId2 = random.nextInt(Constant.EVENT_COUNT);
        }

        int outcomeId1 = random.nextInt(Constant.OUTCOME_COUNT-1) + eventId1*Constant.EVENT_COUNT;
        int outcomeId2 = random.nextInt(Constant.OUTCOME_COUNT-1) + eventId2*Constant.EVENT_COUNT;

        BetOutcome betOutcome1 = new BetOutcome(null,eventId1,outcomeId1,Math.random() + 1);
        BetOutcome betOutcome2 = new BetOutcome(null,eventId2,outcomeId2,Math.random() + 1);
        Set<BetOutcome> set = new HashSet<>(2);
        set.add(betOutcome1);
        set.add(betOutcome2);
        BetInfo betInfo = new BetInfo(-1L,random.nextInt(50), random.nextInt(Constant.PUNTER_COUNT),set);
        return betInfo;
    }

    
}
