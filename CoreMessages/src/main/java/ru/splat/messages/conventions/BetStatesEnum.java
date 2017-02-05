package ru.splat.messages.conventions;

/**
 * Created by Дмитрий on 22.12.2016.
 */
//возможные "состояния" ставок для записи в БД BetService
public enum BetStatesEnum {
    CANDIDATE, //0
    SUCCESS,    //1
    CANCELED, //2
    TIMEOUT //3
}
