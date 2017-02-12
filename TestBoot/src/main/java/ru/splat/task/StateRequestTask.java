package ru.splat.task;

import ru.splat.messages.bet.BetState;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.service.StateCheckService;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateRequestTask implements Runnable{
    private ConcurrentSkipListSet<NewResponse> trIdSet;

    public StateRequestTask(ConcurrentSkipListSet<NewResponse> trIdSet) {
        this.trIdSet = trIdSet;
    }

    @Override
    public void run() {

        StateCheckService stateCheckService = new StateCheckService();
        while (!Thread.currentThread().interrupted() && !Thread.interrupted()) {    //настроить частоту обращений
            Iterator<NewResponse> iterator = trIdSet.iterator();
            while (iterator.hasNext()) {
                try {
                    NewResponse response = iterator.next();

                    int state = stateCheckService.makeRequest(response);
                    if (state == 1) { System.out.println("TrState for " + response.getTransactionId() + ": ACCEPTED"); iterator.remove();}
                    else if (state == 2) { System.out.println("TrState for " + response.getTransactionId() + ": DENIED"); iterator.remove();}
                    else if (state == 3) System.out.println("TrState for " + response.getTransactionId() + ": PENDING");

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
