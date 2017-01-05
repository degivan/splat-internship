package ru.splat;


import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;


/**
 * @author nkalugin on 26.12.16.
 */

@ImportResource({ "classpath:spring-core.xml" })
public class Boot
{
    @Autowired
    PunterServiceWrapper punterServiceWrapper;

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(Boot.class, args);
    }

    @PostConstruct
    public void doBet(){
        ExecutorService exService = Executors.newSingleThreadExecutor();
        exService.execute(punterServiceWrapper);
    }
}
