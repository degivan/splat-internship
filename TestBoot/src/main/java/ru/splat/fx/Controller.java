package ru.splat.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.Constant;
import ru.splat.messages.proxyup.bet.NewResponseClone;
import ru.splat.service.EventDefaultDataService;
import ru.splat.task.RequestTask;
import ru.splat.task.StateRequestTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller
{
    public static Stage stage;

    @FXML
    private Button button;

    @FXML
    private TextField tfPunterCount1;
    @FXML
    private ComboBox<String> timeCombo;
    @FXML
    private TextField tfRequestTimeout;
    @FXML
    private TextField tfPunterCount2;

    private EventDefaultDataService eventDefaultDataService;

    private int punterCount1;
    private long requestTimeout;
    private int punterCount2;
    private ConcurrentSkipListSet<NewResponseClone> trIdSet;

    private Alert alert;

    private List<ExecutorService> threads;

    public void interruptThreads()
    {
        if (threads != null){ threads.stream().forEach(p -> p.shutdown());}
        threads = null;
    }

    private void init()
    {
        try
        {
            punterCount1 = Integer.valueOf(tfPunterCount1.getCharacters().toString());
        }catch (NumberFormatException nfe)
        {
            punterCount1 = Constant.PUNTER_COUNT1;
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
            punterCount2 = Integer.valueOf(tfPunterCount2.getCharacters().toString());
        }
        catch (NumberFormatException nfe)
        {
            punterCount2 = Constant.PUNTER_COUNT2;
        }


    }

    @FXML
    public void onClickStart()
    {
        alert.setContentText("Начало работы тестового бота");

        alert.showAndWait();

        if (threads == null)
        {
            init();
            threads = new ArrayList<>(punterCount2 - punterCount1 + 1);

            int j = 0;

            for (int i = punterCount1; i <= punterCount2; i++)
            {
                threads.add(Executors.newSingleThreadExecutor());
                threads.get(j).submit(new RequestTask(requestTimeout, i, this.trIdSet));
                j++;
            }
           threads.get(threads.size() - 1).submit(new StateRequestTask(this.trIdSet));

        }
    }

    @FXML
    public void onClickStop()
    {
        interruptThreads();

        alert.setContentText("Конец работы тестового бота");

        alert.showAndWait();
    }

    @FXML
    public void initialize(){
        trIdSet = new ConcurrentSkipListSet<NewResponseClone>(new Comparator<NewResponseClone>()
        {
            public int compare(NewResponseClone o1, NewResponseClone o2)
            {
                return o1.getTransactionId() == o2.getTransactionId()?0:o1.getTransactionId() > o2.getTransactionId()?1:-1;
            }
        });
        tfRequestTimeout.setText(String.valueOf(Constant.REQUEST_TIMEOUT));
        tfPunterCount1.setText(String.valueOf(Constant.PUNTER_COUNT1));
        tfPunterCount2.setText(String.valueOf(Constant.PUNTER_COUNT2));
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
