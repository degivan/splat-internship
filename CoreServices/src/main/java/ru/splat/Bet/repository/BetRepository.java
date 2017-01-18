package ru.splat.Bet.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.splat.Punter.feautures.PunterInfo;

import java.util.List;

public class BetRepository
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void phase1(List<PunterInfo> punterInfoList)
    {

    }
}
