package ru.splat.task;


import org.apache.log4j.Logger;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.bet.NewResponseClone;
import ru.splat.service.BootService;
import java.util.concurrent.ConcurrentSkipListSet;



public class RequestTask2 implements Runnable {

    private long requestTimeout;
    private int punterCount;
    private ConcurrentSkipListSet<NewResponseClone> trIdSet;
    private static Logger LOGGER = Logger.getLogger(RequestTask.class);

    public RequestTask2(long requestTimeout, int punterCount, ConcurrentSkipListSet<NewResponseClone> trIdSet) {
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

                try {
                    NewResponseClone newResponse = bootService.makeRequest(punterCount);
                    LOGGER.info("Response from server: " + newResponse.toString());
                    //System.out.println(newResponse.getActive());
                    if (!newResponse.getActive()) trIdSet.add(newResponse);  //добавление нового id в сет
                    else LOGGER.info("Another transaction is active for userId = " + newResponse.getUserId());
                }catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();

                }
                catch (Exception e) {
                    LOGGER.error("High level",e);

                }

                residual = System.currentTimeMillis() - timeStart;
                i++;



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
