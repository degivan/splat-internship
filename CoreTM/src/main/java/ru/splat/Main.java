package ru.splat;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.actors.ActorsStarter;

/**
 * Created by Дмитрий on 07.01.2017.
 */
//заглушка мэйна (согласовать с Иваном)
public class Main {
    public static void main(String[] args) {
        ApplicationContext appContext =
                new ClassPathXmlApplicationContext("beans.xml");
        /*ActorsStarter actorsStarter = (ActorsStarter) appContext.getBean("ActorsStarter");
        actorsStarter.initActors();*/

    }


}
