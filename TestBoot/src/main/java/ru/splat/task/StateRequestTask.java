package ru.splat.task;

import org.apache.log4j.Logger;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.bet.NewResponseClone;
import ru.splat.messages.proxyup.check.CheckResult;
import ru.splat.service.StateCheckService;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateRequestTask implements Runnable{
    private ConcurrentSkipListSet<NewResponseClone> trIdSet;
    private static Logger LOGGER = Logger.getLogger(StateRequestTask.class);

    public StateRequestTask(ConcurrentSkipListSet<NewResponseClone> trIdSet) {
        this.trIdSet = trIdSet;
    }

    //TODO добавить логирование

    @Override
    public void run() {

        StateCheckService stateCheckService = new StateCheckService();
        while (!Thread.currentThread().interrupted()) {    //настроить частоту обращений
            Iterator<NewResponseClone> iterator = trIdSet.iterator();
            while (iterator.hasNext()) {
                try {
                    NewResponseClone response = iterator.next();

                    LOGGER.info(response.toString());

                    int state = stateCheckService.makeRequest(response);
                    LOGGER.info(state + "");
                    if (state == CheckResult.ACCEPTED.ordinal()) { System.out.println("TrState for " + response.getTransactionId() + ": ACCEPTED"); iterator.remove();}
                    else if (state == CheckResult.REJECTED.ordinal()) { System.out.println("TrState for " + response.getTransactionId() + ": REJECTED"); iterator.remove();}
                    else if (state == CheckResult.PENDING.ordinal()) System.out.println("TrState for " + response.getTransactionId() + ": PENDING");

                } catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.currentThread().sleep(100l);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            /*controller.getTrIdSet().forEach(trId -> {
                try {
                    BetState state = stateCheckService.makeRequest(trId);
                    System.out.println("TrState for " + trId + ": " + state.toString());
                    if (!state.equals(BetState.PROCESSING))
                        controller.removeTransactionId(trId);
                } catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });*/

        }
    }
}
