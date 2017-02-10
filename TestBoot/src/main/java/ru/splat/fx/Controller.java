package ru.splat.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.Constant;
import ru.splat.service.EventDefaultDataService;
import ru.splat.task.RequestTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private EventDefaultDataService eventDefaultDataService;

    private int punterCount;
    private long requestTimeout;
    private int requestCount;

    private Alert alert;

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
        alert.setContentText("Начало работы тестового бота");

        alert.showAndWait();

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

        alert.setContentText("Конец работы тестового бота");

        alert.showAndWait();
    }

    @FXML
    public void initialize(){
        tfRequestTimeout.setText(String.valueOf(Constant.REQUEST_TIMEOUT));
        tfPunterCount.setText(String.valueOf(Constant.PUNTER_COUNT));
        tfRequestCount.setText(String.valueOf(Constant.REQUEST_COUNT));
        timeCombo.setValue("Сек");

        ApplicationContext appContext = new ClassPathXmlApplicationContext("spring-core.xml");
        eventDefaultDataService = (EventDefaultDataService) appContext.getBean("eventDefaultDataService");

        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
    }

    //TODO подумать про зависимости лимитов в бд и во входных данных.
    @FXML
    public void onClickCreate()
    {
        if (eventDefaultDataService.isEmptyEvent())
        {
            eventDefaultDataService.insertDefaultData();

            alert.setContentText("Данные в БД успешно созданы");

            alert.showAndWait();
        } else alert.setContentText("Данные в БД уже существуют");

        alert.showAndWait();
    }

    @FXML
    public void onClickDelete()
    {
        eventDefaultDataService.deleteData();
        alert.setContentText("Данные в БД успешно удалены");
        alert.showAndWait();
    }
}
