package ru.splat.task;


import org.apache.log4j.Logger;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.bet.NewResponseClone;
import ru.splat.service.BootService;
import ru.splat.service.StateCheckService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RequestTask implements Runnable {

    private int requestCount;
    private long requestTimeout;
    private int punterCount;
    private ConcurrentSkipListSet<NewResponseClone> trIdSet;
    private static Logger LOGGER = Logger.getLogger(RequestTask.class);

    public RequestTask(int requestCount, long requestTimeout, int punterCount, ConcurrentSkipListSet<NewResponseClone> trIdSet) {
        this.requestCount = requestCount;
        this.requestTimeout = requestTimeout;
        this.punterCount = punterCount;
        this.trIdSet = trIdSet;
    }

    @Override
    public void run() {


        ExecutorService executorService = Executors.newFixedThreadPool(2000);

        while (!Thread.currentThread().interrupted())
        {
            int i=0;
            long timeStart = System.currentTimeMillis();
            long residual = 0;

            while (i < requestCount && residual < requestTimeout)
            {

                CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(new BootService(punterCount),executorService)
                        .exceptionally( (ex) ->
                        {
                            if (ex.getClass() == InterruptedException.class)
                            {
                                Thread.currentThread().interrupt();
                                executorService.shutdown();
                            }
                            LOGGER.error("High level",ex);
                            return null;
                        })
                        .thenAccept( (newResponse) ->
                        {
                            LOGGER.info("Response from server: " + newResponse.toString());
                            if (!newResponse.getActive()) trIdSet.add(newResponse);  //добавление нового id в сет
                            else LOGGER.info("Another transaction is active for userId = " + newResponse.getUserId());
                        });

                residual = System.currentTimeMillis() - timeStart;
                i++;
            }


            if (residual < requestTimeout)
            {
                long freeTime = requestTimeout - residual;
                LOGGER.info("Sleep time: " + freeTime);
                try {
                    Thread.currentThread().sleep(freeTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
