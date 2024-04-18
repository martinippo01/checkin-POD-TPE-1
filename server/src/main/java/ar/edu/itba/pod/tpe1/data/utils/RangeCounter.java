package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class RangeCounter implements Comparable<RangeCounter> {

    private final int counterFrom;
    private final int counterTo;
    // Metadata

    public RangeCounter(final int counterFrom, final int counterTo) {
        this.counterFrom = counterFrom;
        this.counterTo = counterTo;
    }

    public int getCounterFrom() {
        return counterFrom;
    }

    public int getCounterTo() {
        return counterTo;
    }

    @Override
    public int compareTo(RangeCounter rangeCounter) {
        // Check which one starts first
        return Integer.compare(counterFrom, rangeCounter.counterFrom);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeCounter that = (RangeCounter) o;
        return counterFrom == that.counterFrom &&
                counterTo == that.counterTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterFrom, counterTo);
    }

}