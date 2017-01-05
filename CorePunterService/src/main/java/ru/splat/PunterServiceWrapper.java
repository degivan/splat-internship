package ru.splat;


import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;


/**
 * @author nkalugin on 26.12.16.
 */
public class PunterServiceWrapper implements Runnable
{
    @Autowired
    private PunterService punterService;

    @Override
    public void run()
    {
        while (true) {
            try {
                punterService.mainProcess(punterService.getPunterKafka(), punterService.TOPIC_REQUEST);
            } catch (Exception e){

            }
        }
    }
}
