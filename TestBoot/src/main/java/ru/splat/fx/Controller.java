package ru.splat.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ru.splat.Constant;
import ru.splat.task.RequestTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller
{
    @FXML
    private Button button;

    @FXML
    private TextField tfPunterCount;
    @FXML
    private ComboBox<String> timeCombo;
    @FXML
    private TextField tfRequestTimeout;
    @FXML
    private TextField tfRequestCount;

    private int punterCount;
    private long requestTimeout;
    private int requestCount;
    private Set<Long> trIdSet;

    private ExecutorService executorService;


    private void init()
    {
        try
        {
            punterCount  = Integer.valueOf(tfPunterCount.getCharacters().toString());
        }catch (NumberFormatException nfe)
        {
            punterCount = Constant.PUNTER_COUNT;
        }

        try
        {
            requestTimeout = Long.valueOf(tfRequestTimeout.getCharacters().toString());
        }
        catch (NumberFormatException nfe)
        {
            requestTimeout = Constant.REQUEST_TIMEOUT;
        }

        if (timeCombo.getValue() == null || "Мин".equals(timeCombo.getValue()))
        {
            requestTimeout = requestTimeout *60*1000;
        }
        else
        {
            requestTimeout = requestTimeout * 1000;
        }

        trIdSet = new HashSet<>();

        try
        {
            requestCount = Integer.valueOf(tfRequestCount.getCharacters().toString());
        }
        catch (NumberFormatException nfe)
        {
            requestCount = Constant.REQUEST_COUNT;
        }
    }

    @FXML
    public void onClickStart()
    {
        if (executorService == null)
        {
            init();
            executorService = Executors.newFixedThreadPool(9);
            for (int i = 0; i < 8; i++) {
                executorService.submit(new RequestTask(requestCount, requestTimeout, punterCount));
            }
        }
    }

    @FXML
    public void onClickStop()
    {
        if (executorService != null){ executorService.shutdownNow();}
        executorService = null;
    }

    @FXML
    public void initialize(){

    }
}
