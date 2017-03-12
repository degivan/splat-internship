package ru.splat.service;

import com.google.gson.Gson;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.bet.NewResponseClone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateCheckService{

    private static final String URL_ADRESS = "http://172.17.51.54:8080/SpringMVC/checkbet?transactionId=";   //заглушка, узнать и Ивана форму запроса стейта
    private NewResponseClone trdata;

    public StateCheckService(NewResponseClone trdata)
    {
        this.trdata = trdata;
    }


    public Integer doRequest() throws Exception
    {
        Gson g = new Gson();
        URL url = null;
        StringBuilder response = new StringBuilder();
        url = new URL(URL_ADRESS + trdata.getTransactionId() + "&userId=" + trdata.getUserId());
        HttpURLConnection connection = null;
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        // connection.setRequestProperty("Content-Type", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        response = new StringBuilder();
        while((null != (inputLine = in.readLine())))
        {
            response.append(inputLine);
        }
        in.close();

        int betState = g.fromJson(response.toString(), Integer.class);
        return betState;
    }
}
