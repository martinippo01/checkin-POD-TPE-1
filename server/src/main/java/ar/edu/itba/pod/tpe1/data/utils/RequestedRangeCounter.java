package ar.edu.itba.pod.tpe1.data.utils;

import java.awt.print.Book;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestedRangeCounter implements Comparable<RequestedRangeCounter> {

    private final int counterFrom;
    private final int counterTo;
    private final List<Flight> flights;
    private final Airline airline;
    private final boolean pending;
    private final int requestedRange;

    private final ConcurrentLinkedQueue<Booking> waitingQueue = new ConcurrentLinkedQueue<Booking>();

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

    public int getSize() {
        return counterTo - counterFrom + 1;
    }

    public boolean isInRange(int counter) {
        return counter >= counterFrom && counter <= counterTo;
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

    public void addBookingToWaitingQueue(Booking booking) {
        Objects.requireNonNull(booking, "Received NULL booking");
        waitingQueue.add(booking);
    }

    /* Returns first Booking in queue, or null if empty */
    public Booking getFromWaitingQueue() {
        return waitingQueue.poll();
    }

    public int getWaitingQueueLength () {
        return waitingQueue.size();
    }

    public boolean isWaitingInQueue(Booking booking) {
        Objects.requireNonNull(booking, "Received NULL booking");
        return waitingQueue.contains(booking);
    }

    // CounterFrom is unique among assigned counters
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedRangeCounter that = (RequestedRangeCounter) o;
        // If it is pending, it is equal by counterFrom, if not it has to match the rest
        if(!pending)
            return counterFrom == that.counterFrom;
        else
            return requestedRange == that.requestedRange && airline.equals(that.airline) && flights.equals(that.flights);
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
