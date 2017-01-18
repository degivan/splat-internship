package ru.splat.mvc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.splat.mvc.features.BetRequestFull;
import ru.splat.mvc.features.ReposResult;
import ru.splat.mvc.service.ShowEvents;


@Controller
public class BetController
{

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
    public @ResponseBody long getTransactionId(@RequestBody BetRequestFull betRequest)
    {
        System.out.println();
        //заглушка
        return 2;
    }

    @RequestMapping(value = "/checkbet", method = RequestMethod.GET)
    public @ResponseBody String chekBet(@RequestParam(value="transactionId", defaultValue="false") long transactionId)
    {
        System.out.println(transactionId);
        //заглушка
        String status = "{\"status\": \"accepted\"}";
        return status;
    }
}
