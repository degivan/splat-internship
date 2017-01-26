package ru.splat.tmkafka;

import java.util.List;

/**
 * Created by Дмитрий on 06.01.2017.
 */
public interface TMProducer {
    void writeToKafka(Object transactionResults);
}
