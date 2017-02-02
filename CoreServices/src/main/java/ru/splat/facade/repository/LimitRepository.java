package ru.splat.facade.repository;


import ru.splat.facade.feautures.Limit;

import java.util.List;

public interface LimitRepository {

    void updateLimit(Limit limit);

    List<Limit> getLimits(List<Integer> idList);

}
