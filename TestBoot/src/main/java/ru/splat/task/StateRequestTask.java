package ru.splat.task;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.fx.Controller;
import ru.splat.messages.bet.BetState;
import ru.splat.service.StateCheckService;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateRequestTask implements Runnable{
    //@Autowired
    Controller controller;


    @Override
    public void run() {

        StateCheckService stateCheckService = new StateCheckService();
        while (!Thread.currentThread().interrupted() && !Thread.interrupted()) {    //настроить частоту обращений
            controller.getTrIdSet().forEach(trId -> {
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
            });

        }
    }
}
