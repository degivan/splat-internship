package ru.splat.facade;


import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource({ "classpath:spring-core.xml" })
public class Boot
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        SpringApplication.run(Boot.class, args);
    }
}
