package ru.splat.repository;

import java.util.List;

public interface IdempRepositoryInterface<T,V> {
    void insertFilterTable(List<V> transactionResults);
    List<V> filterByTable(List<T> punterIdList);
}
