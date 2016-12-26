package ru.splat;


import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author nkalugin on 26.12.16.
 */
public class PunterServiceWrapper
{
    @Autowired
    private PunterService punterService;


    public void process()
    {
        punterService.mainProcess();
    }
}
