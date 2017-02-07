package ru.splat.tm;

import ru.splat.messages.uptm.trmetadata.TransactionMetadata;

import java.util.List;

/**
 * Created by Дмитрий on 11.12.2016.
 */
public interface TMStarter {

    void processTransaction(TransactionMetadata trMetadata);
    public void sendBatch();
}
