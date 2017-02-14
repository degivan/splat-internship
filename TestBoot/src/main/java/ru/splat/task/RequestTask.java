package ru.splat.task;


import org.apache.log4j.Logger;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.service.BootService;
import java.util.concurrent.ConcurrentSkipListSet;



public class RequestTask implements Runnable {

    private int requestCount;
    private long requestTimeout;
    private int punterCount;
    private ConcurrentSkipListSet<NewResponse> trIdSet;
    private static Logger LOGGER = Logger.getLogger(RequestTask.class);

    public RequestTask(int requestCount, long requestTimeout, int punterCount, ConcurrentSkipListSet<NewResponse> trIdSet) {
        this.requestCount = requestCount;
        this.requestTimeout = requestTimeout;
        this.punterCount = punterCount;
        this.trIdSet = trIdSet;
    }

    @Override
    public void run() {

        BootService bootService = new BootService();

        while (!Thread.currentThread().interrupted())
        {
            int i=0;
            long timeStart = System.currentTimeMillis();
            long residual = 0;
            while (i < requestCount && residual < requestTimeout)
            {
                LOGGER.info(i + "");
                try {
                    NewResponse newResponse = bootService.makeRequest(punterCount);
                    LOGGER.info("Response from server: " + newResponse.toString());
                    trIdSet.add(newResponse);  //добавление нового id в сет
                }catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e) {
                    LOGGER.error("High level",e);
                }
                finally
                {
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
