package ar.edu.itba.pod.tpe1.data.utils;

import ar.edu.itba.pod.tpe1.protos.NotificationsService.NotificationType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Notification {
    private final boolean poissonPill;
    private final NotificationType notificationType;
    private final Airline airline;
    private final int counterFrom;
    private final int counterTo;
    private final String sector;
    private final List<String> flights;
    private final String booking;
    private final String flight;
    private final int people_ahead;
    private final int counter;
    private final int pending_ahead;

    public Notification(Builder builder) {
        this.poissonPill = builder.poisonPill;
        this.notificationType = builder.notificationType;
        this.airline = builder.airline;
        this.counterFrom = builder.counterFrom;
        this.counterTo = builder.counterTo;
        this.sector = builder.sector;
        this.flights = builder.flights;
        this.booking = builder.booking;
        this.flight = builder.flight;
        this.people_ahead = builder.people_ahead;
        this.counter = builder.counter;
        this.pending_ahead = builder.pending_ahead;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public boolean isPoissonPill() {
        return poissonPill;
    }

    public Airline getAirline() {
        return airline;
    }

    public int getCounterFrom() {
        return counterFrom;
    }

    public int getCounterTo() {
        return counterTo;
    }

    public String getSector() {
        return sector;
    }

    public List<String> getFlights() {
        return flights;
    }

    public String getBooking() {
        return booking;
    }

    public String getFlight() {
        return flight;
    }

    public int getPeopleAhead() {
        return people_ahead;
    }

    public int getCounter() {
        return counter;
    }

    public int getPendingAhead() {
        return pending_ahead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return counterFrom == that.counterFrom && counterTo == that.counterTo && people_ahead == that.people_ahead && counter == that.counter && pending_ahead == that.pending_ahead && notificationType == that.notificationType && Objects.equals(airline, that.airline) && Objects.equals(sector, that.sector) && Objects.equals(flights, that.flights) && Objects.equals(booking, that.booking) && Objects.equals(flight, that.flight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationType, airline, counterFrom, counterTo, sector, flights, booking, flight, people_ahead, counter, pending_ahead);
    }

    //-------- BUILDER -----------

    public static class Builder {
        private boolean poisonPill;
        private NotificationType notificationType;
        private Airline airline;
        private int counterFrom;
        private int counterTo;
        private String sector;
        private List<String> flights;
        private String booking;
        private String flight;
        private int people_ahead;
        private int counter;
        private int pending_ahead;

        public Builder() {
            this.poisonPill = false;
            // set defaults
            this.notificationType = NotificationType.SUCCESSFUL_REGISTER;
            this.airline = new Airline("");
            this.counterFrom = -1;
            this.counterTo = -1;
            this.sector = "";
            this.flights = Collections.emptyList();
            this.booking = "";
            this.flight = "";
            this.people_ahead = -1;
            this.counter = -1;
            this.pending_ahead = -1;
        }

        public Builder setNotificationType(NotificationType notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public Builder setPoisonPill() {
            poisonPill = true;
            return this;
        }

        public Builder setAirline(Airline airline) {
            this.airline = airline;
            return this;
        }

        public Builder setCounterFrom(int counterFrom) {
            this.counterFrom = counterFrom;
            return this;
        }

        public Builder setCounterTo(int counterTo) {
            this.counterTo = counterTo;
            return this;
        }

        public Builder setSector(String sector) {
            this.sector = sector;
            return this;
        }

        public Builder setFlights(List<String> flights) {
            this.flights = flights;
            return this;
        }

        public Builder setBooking(String booking) {
            this.booking = booking;
            return this;
        }

        public Builder setFlight(String flight) {
            this.flight = flight;
            return this;
        }

        public Builder setPeopleAhead(int peopleAhead) {
            this.people_ahead = peopleAhead;
            return this;
        }

        public Builder setCounter(int counter) {
            this.counter = counter;
            return this;
        }

        public Builder setPendingAhead(int pendingAhead) {
            this.pending_ahead = pendingAhead;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}
