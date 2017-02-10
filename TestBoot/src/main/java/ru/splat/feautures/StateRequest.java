package ru.splat.feautures;

/**
 * Created by Дмитрий on 10.02.2017.
 */
public class StateRequest {
    private int id;

    public StateRequest(int id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "StateRequest{id="+id+'}';
    }
}
