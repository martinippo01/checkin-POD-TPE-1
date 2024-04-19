package ar.edu.itba.pod.tpe1.data.utils;

import java.util.*;

public class RangeCounter implements Comparable<RangeCounter> {

    private final int counterFrom;
    private final int counterTo;
    // Metadata
    //private final List<AssignedRangeCounter> assignedRangeCounters = new ArrayList<>(); // TODO check thread safety
    private final Set<AssignedRangeCounter> assignedRangeCounters = new TreeSet<>(); // TODO check thread safety

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

    public int getSize(){
        return counterTo - counterFrom + 1;
    }

    public AssignedRangeCounter assignRange(final int count, List<Flight> flights, Airline airline) {
        // Cannot assign due to invalid argument or size greater than the whole range
        if (count < 0 || count > getSize()) {
            return null;
        }

        int start = counterFrom;
        for(AssignedRangeCounter assignedRangeCounter : assignedRangeCounters) {
            int end = assignedRangeCounter.getCounterFrom() - 1;
            if(end - start + 1 >= count){ // In case the gap is big enough, create the assigned range of counters
                AssignedRangeCounter newAssignedRangeCounter = new AssignedRangeCounter(start, start + count - 1, flights, airline);
                assignedRangeCounters.add(newAssignedRangeCounter);
                return newAssignedRangeCounter;
            }
            start = assignedRangeCounter.getCounterTo() + 1;
        }

        // Check the remaining space from the last registered to the end of the sector
        if(counterTo - start + 1 >= count){
            AssignedRangeCounter newAssignedRangeCounter = new AssignedRangeCounter(start, start + count, flights, airline);
            assignedRangeCounters.add(newAssignedRangeCounter);
            return newAssignedRangeCounter;
        }
        // There's no space
        return null;
    }

    public List<AssignedRangeCounter> getAssignedRangeCounters() {
        //return assignedRangeCounters; // TODO check thread safety
        return null;
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
