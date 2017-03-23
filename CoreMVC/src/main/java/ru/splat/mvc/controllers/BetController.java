package ru.splat.mvc.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.splat.Proxy;
import ru.splat.UP;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.mvc.features.ReposResult;
import ru.splat.mvc.service.ShowEvents;
import javax.annotation.PostConstruct;


@Controller
public class BetController
{
    private Proxy proxy;
    private ShowEvents showEvents;

    @Autowired
    public BetController(ShowEvents showEvents)
    {
        this.showEvents = showEvents;
        init();
    }

    private static Logger LOGGER = Logger.getLogger(BetController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String printWelcome()
    {
        return "index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public @ResponseBody ReposResult initMain()
    {

        ReposResult reposResult = showEvents.initMainPage();
        LOGGER.info("Init data " + reposResult.toString());
        return reposResult;

    }

    @RequestMapping(value = "/dobet", method = RequestMethod.POST)
    public @ResponseBody
    NewResponse getTransactionId(@RequestBody BetInfo betInfo) throws Exception
    {
        //System.out.println(betInfo.toString());
//        NewResponse newResponse = proxy.sendNewRequest(betInfo);
        LOGGER.info("/dobet request: " + betInfo.toString() + " response: ");
//                + newResponse.toString());
//        return newResponse;
        return new NewResponse(betInfo.getUserId());
    }

    @RequestMapping(value = "/checkbet", method = RequestMethod.GET)
    public @ResponseBody
    int chekBet(@RequestParam(value="transactionId", defaultValue="false") long transactionId,@RequestParam(value="userId", defaultValue="false") int userId) throws Exception
    {

       // int check = proxy.sendCheckRequest(transactionId, userId).ordinal();
        LOGGER.info("/checkbet request: " + transactionId + " " + userId + " response: ");
        //+ check);
        //return check;
        return 1;
    }

    public void init()
    {
        UP up = UP.create();
        proxy = up.start();

    }
}
