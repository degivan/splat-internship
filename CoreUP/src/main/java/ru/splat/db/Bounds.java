package ru.splat.db;

/**
 * Created by Иван on 01.02.2017.
 */
public class Bounds {
    private final Long lowerBound;
    private final Long upperBound;

    public Bounds(Long lowerBound, Long upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Long getLowerBound() {
        return lowerBound;
    }

    public Long getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                '}';
    }
}
