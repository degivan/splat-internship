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
import java.util.function.Supplier;

public class BootService implements Supplier<NewResponseClone>
{
    private static final String URL_ADRESS = "http://172.17.51.54:8080/SpringMVC/dobet";
    private Logger LOGGER = Logger.getLogger(BootService.class);

    private int punterCount;

    public BootService(int punterCount)
    {
        this.punterCount = punterCount;
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
        BetInfo betInfo = new BetInfo(-1L,random.nextInt(punterCount), random.nextInt(Constant.PUNTER_COUNT),set);
        return betInfo;
    }


    @Override
    public NewResponseClone get()
    {
        Gson g = new Gson();

        NewResponseClone newResponse = new NewResponseClone();
        try
        {

            URL url = new URL(URL_ADRESS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            BetInfo betInfo = generateBet(punterCount);
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

            while ((null != (inputLine = in.readLine()))) {
                response.append(inputLine);
            }
            in.close();

             newResponse = g.fromJson(response.toString(), NewResponseClone.class);
        }catch (Exception ex)
        {

        }

        return newResponse;
    }
}
