package ru.splat.mvc.controllers;

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
    @Autowired
    private ShowEvents showEvents;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String printWelcome()
    {
        return "index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public @ResponseBody ReposResult initMain()
    {
        ReposResult reposResult = showEvents.initMainPage();
        return reposResult;
    }

    @RequestMapping(value = "/dobet", method = RequestMethod.POST)
    public @ResponseBody
    NewResponse getTransactionId(@RequestBody BetInfo betInfo) throws Exception
    {
        System.out.println(betInfo.toString());
        return proxy.sendNewRequest(betInfo);
        //return new NewResponse(betInfo.getUserId());
    }

    @RequestMapping(value = "/checkbet", method = RequestMethod.GET)
    public @ResponseBody
    int chekBet(@RequestParam(value="transactionId", defaultValue="false") long transactionId,@RequestParam(value="userId", defaultValue="false") int userId) throws Exception {

        return proxy.sendCheckRequest(transactionId, userId).ordinal();
        //return 1;
    }

    @PostConstruct
    public void init() {
        UP up = UP.create();
        proxy = up.start();
    }
}
