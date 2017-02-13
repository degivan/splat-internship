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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bounds bounds = (Bounds) o;

        if (lowerBound != null ? !lowerBound.equals(bounds.lowerBound) : bounds.lowerBound != null) return false;
        return upperBound != null ? upperBound.equals(bounds.upperBound) : bounds.upperBound == null;
    }

    @Override
    public int hashCode() {
        int result = lowerBound != null ? lowerBound.hashCode() : 0;
        result = 31 * result + (upperBound != null ? upperBound.hashCode() : 0);
        return result;
    }
}
