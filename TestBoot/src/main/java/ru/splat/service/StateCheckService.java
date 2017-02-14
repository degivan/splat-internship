package ru.splat.service;

import com.google.gson.Gson;
import ru.splat.messages.proxyup.bet.NewResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateCheckService {
    private static final String URL_ADRESS = "http://localhost:8080/checkbet?transactionId=";   //заглушка, узнать и Ивана форму запроса стейта

    public int makeRequest(NewResponse trdata) throws Exception
    {
        Gson g = new Gson();
        URL url = new URL(URL_ADRESS + trdata.getTransactionId() + "&userId=" + trdata.getUserId());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
       // connection.setRequestProperty("Content-Type", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while((null != (inputLine = in.readLine())))
        {
            response.append(inputLine);
        }
        in.close();
        int betState = g.fromJson(response.toString(), Integer.class);
        return betState;
    }

}
