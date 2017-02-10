package ru.splat.service;

import com.google.gson.Gson;
import ru.splat.messages.bet.BetStateRequest;
import ru.splat.messages.bet.BetState;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateCheckService {
    private static final String URL_ADRESS = "http://localhost:8080/getbetstate";   //заглушка, узнать и Ивана форму запроса стейта

    public BetState makeRequest(long trId) throws Exception
    {
        Gson g = new Gson();
        URL url = new URL(URL_ADRESS);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        String json = g.toJson(new BetStateRequest(trId));
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
        String betState = g.fromJson(response.toString(), String.class);
        System.out.println(betState);
        return BetState.valueOf(betState);
    }

    private BetStateRequest generateStateRequest(long trId) {
        return new BetStateRequest(trId);
    }
}
