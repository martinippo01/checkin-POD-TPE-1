package ar.edu.itba.pod.tpe1.data.utils;

import java.util.List;
import java.util.Objects;

public class RequestedRangeCounter implements Comparable<RequestedRangeCounter> {

    private final int counterFrom;
    private final int counterTo;
    private final List<Flight> flights;
    private final Airline airline;
    private final boolean pending;
    private final int requestedRange;

    public RequestedRangeCounter(final int counterFrom, final int counterTo, List<Flight> flightList, Airline airline, boolean pending) {
        this.counterFrom = counterFrom;
        this.counterTo = counterTo;
        this.flights = flightList; // TODO CHECK THREAD SAFETY!!!
        this.airline = airline;
        this.pending = pending;
        this.requestedRange = counterTo - counterFrom + 1;
    }

    public RequestedRangeCounter(RequestedRangeCounter other) {
        this.counterFrom = other.counterFrom;
        this.counterTo = other.counterTo;
        this.flights = other.flights;
        this.airline = other.airline;
        this.pending = other.pending;
        this.requestedRange = other.requestedRange;
    }
    public RequestedRangeCounter(List<Flight> flightList, Airline airline, boolean pending, int requestedRange) {
        this.counterFrom = -1;
        this.counterTo = -1;
        this.flights = flightList; // TODO CHECK THREAD SAFETY!!!
        this.airline = airline;
        this.pending = pending;
        this.requestedRange = requestedRange;
    }

    public int getCounterFrom() {
        return counterFrom;
    }
    public int getCounterTo() {
        return counterTo;
    }
    public List<Flight> getFlights() {
        return flights; // TODO CHECK THREAD SAFETY!!!
    }
    public Airline getAirline() {
        return airline;
    }
    public int getRequestedRange() {
        return requestedRange;
    }

    // CounterFrom is unique among assigned counters
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedRangeCounter that = (RequestedRangeCounter) o;

        return counterFrom == that.counterFrom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterFrom);
    }

    @Override
    public int compareTo(RequestedRangeCounter assignedRangeCounter) {
        // Check which one starts first
        return Integer.compare(counterFrom, assignedRangeCounter.counterFrom);
    }
}
