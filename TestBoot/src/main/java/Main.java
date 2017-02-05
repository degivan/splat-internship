import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main {

    public static void main(String[] args)
    {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("spring-core.xml");
        EventDefaultDataService eventDefaultDataService = (EventDefaultDataService) appContext.getBean("eventDefaultDataService");
        eventDefaultDataService.insertDefaultData();
    }
}
