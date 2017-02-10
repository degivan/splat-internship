package ru.splat;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.service.EventDefaultDataService;
import ru.splat.task.RequestTask;

public class Main {

    public static void main(String[] args)
    {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("spring-core.xml");
        EventDefaultDataService eventDefaultDataService = (EventDefaultDataService) appContext.getBean("eventDefaultDataService");
        eventDefaultDataService.insertDefaultData();

        new Thread(new RequestTask(Constant.REQUEST_COUNT,Constant.REQUEST_TIMEOUT, Constant.PUNTER_COUNT)).start();

    }
}
