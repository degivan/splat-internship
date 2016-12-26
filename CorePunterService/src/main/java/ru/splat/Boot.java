package ru.splat;


import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * @author nkalugin on 26.12.16.
 */
@ImportResource({ "classpath:spring-core.xml" })
public class Boot
{
    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(Boot.class, args);
    }
}
