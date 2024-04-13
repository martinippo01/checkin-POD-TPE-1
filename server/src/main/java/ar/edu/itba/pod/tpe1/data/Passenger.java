package ar.edu.itba.pod.tpe1.data;

import java.util.Objects;

public class Passenger {
    private final String bookingCode;
    private final String flightCode;
    private final String airlineName;

    public Passenger(String bookingCode, String flightCode, String airlineName) {
        this.bookingCode = bookingCode;
        this.flightCode = flightCode;
        this.airlineName = airlineName;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return bookingCode.equals(passenger.bookingCode) &&
                flightCode.equals(passenger.flightCode) &&
                airlineName.equals(passenger.airlineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingCode, flightCode, airlineName);
    }
}

