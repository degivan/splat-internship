package ru.splat.facade.repository;

import java.util.List;

public interface ExactlyOnceRepositoryInterface<V>
{
    void insertFilterTable(List<V> transactionResults);
    List<V> filterByTable(List<Long> transactionIds);
}
