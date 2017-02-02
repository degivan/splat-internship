package ru.splat.messages.conventions;

public enum TaskTypesEnum
{
    //BillingService
    WITHDRAW, // 0
    CANCEL_RESERVE, // 1

    //PunterService
    ADD_PUNTER_LIMITS, // 2
    CANCEL_PUNTER_LIMITS,  // 3

    //EventService
    ADD_SELECTION_LIMIT, // 4
    CANCEL_SELECTION_LIMIT, // 5

    //BetService
    ADD_BET, // 6
    FIX_BET, // 7
    CANCEL_BET // 8
}