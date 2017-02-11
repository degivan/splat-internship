package ru.splat.task;

import org.springframework.stereotype.Component;
import ru.splat.fx.Controller;
import ru.splat.service.BootService;

import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class RequestTask implements Runnable {

    private int requestCount;
    private long requestTimeout;
    private int punterCount;
    private ConcurrentSkipListSet<Long> trIdSet;


    public RequestTask(int requestCount, long requestTimeout, int punterCount, ConcurrentSkipListSet<Long> trIdSet) {
        this.requestCount = requestCount;
        this.requestTimeout = requestTimeout;
        this.punterCount = punterCount;
        this.trIdSet = trIdSet;
    }

    @Override
    public void run() {

        BootService bootService = new BootService();

        while (!Thread.currentThread().interrupted() && !Thread.interrupted())
        {
            int i=0;
            long timeStart = System.currentTimeMillis();
            long residual = 0;
            while (i < requestCount && residual < requestTimeout)
            {
                try {
                    trIdSet.add(bootService.makeRequest(punterCount));  //добавление нового id в сет
                }catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    i = requestCount;
                    residual = requestTimeout;
                }
                residual = System.currentTimeMillis() - timeStart;
                i++;
            }

            if (residual < requestTimeout)
            {
                try {
                    Thread.currentThread().sleep(requestTimeout - residual);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
