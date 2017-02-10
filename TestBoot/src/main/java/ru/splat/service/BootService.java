package ru.splat.service;


import com.google.gson.Gson;
import ru.splat.Constant;
import ru.splat.feautures.BetRequest;
import ru.splat.feautures.BetRequestFull;
import ru.splat.feautures.StateRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BootService
{
    private static final String URL_ADRESS = "http://localhost:8080/dobet";

    public long makeRequest(int punterCount) throws Exception
    {
        Gson g = new Gson();

        URL url = new URL(URL_ADRESS);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        String json = g.toJson(generateBet(punterCount));

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

        Long transactionId = g.fromJson(response.toString(), Long.class);
        System.out.println(transactionId);

        return transactionId;
    }

    private BetRequestFull generateBet(int punterCount)
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

        BetRequest betRequest = new BetRequest(Math.random() + 1,outcomeId1, eventId1);
        BetRequest betRequest2 = new BetRequest(Math.random() + 1,outcomeId2 ,eventId2);
        Map<Integer,BetRequest> map = new HashMap<>();
        map.put(1,betRequest);
        map.put(2,betRequest2);
        BetRequestFull betRequestFull = new BetRequestFull(map,random.nextInt(50), random.nextInt(Constant.PUNTER_COUNT));
        return betRequestFull;
    }

    
}
