package ru.splat.task;

import ru.splat.fx.Controller;
import ru.splat.messages.bet.BetState;
import ru.splat.service.StateCheckService;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateRequestTask implements Runnable{
    private ConcurrentSkipListSet<Long> trIdSet;

    public StateRequestTask(ConcurrentSkipListSet<Long> trIdSet) {
        this.trIdSet = trIdSet;
    }

    @Override
    public void run() {

        StateCheckService stateCheckService = new StateCheckService();
        while (!Thread.currentThread().interrupted() && !Thread.interrupted()) {    //настроить частоту обращений
            Iterator<Long> iterator = trIdSet.iterator();
            while (iterator.hasNext()) {
                try {
                    Long trId = iterator.next();
                    BetState state = stateCheckService.makeRequest(trId);
                    System.out.println("TrState for " + trId + ": " + state.toString());
                    if (!state.equals(BetState.PROCESSING))
                        iterator.remove();
                } catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
