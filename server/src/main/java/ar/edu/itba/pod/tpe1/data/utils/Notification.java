package ar.edu.itba.pod.tpe1.data.utils;

import airport.NotificationsServiceOuterClass;

import java.util.List;

public class Notification {

    private final NotificationsServiceOuterClass.NotificationType notificationType;
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

    public NotificationsServiceOuterClass.NotificationType getNotificationType() {
        return notificationType;
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


    //-------- BUILDER -----------

    public static class Builder {
        private NotificationsServiceOuterClass.NotificationType notificationType;
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
            // set defaults
            this.notificationType = null;
            this.airline = null;
            this.counterFrom = -1;
            this.counterTo = -1;
            this.sector = "";
            this.flights = null;
            this.booking = "";
            this.flight = "";
            this.people_ahead = -1;
            this.counter = -1;
            this.pending_ahead = -1;
        }

        public void setAirline(Airline airline) {
            this.airline = airline;
        }

        public void setCounterFrom(int counterFrom) {
            this.counterFrom = counterFrom;
        }

        public void setCounterTo(int counterTo){
            this.counterTo = counterTo;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }

        public void setFlights(List<String> flights) {
            this.flights = flights;
        }

        public void setBooking(String booking) {
            this.booking = booking;
        }

        public void setFlight(String flight) {
            this.flight = flight;
        }

        public void setPeopleAhead(int peopleAhead) {
            this.people_ahead = peopleAhead;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        public void setPendingAhead(int pendingAhead) {
            this.pending_ahead = pendingAhead;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}
