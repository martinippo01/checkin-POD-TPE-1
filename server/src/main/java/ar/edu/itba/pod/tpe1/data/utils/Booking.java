package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class Booking {

    private final String bookingCode;
    private final Flight flight;
    private final Airline airline;

    public Booking(String bookingCode, Flight flight, Airline airline) {
        this.bookingCode = bookingCode;
        this.flight = flight;
        this.airline = airline;
    }

    public String getBookingCode() {
        return bookingCode;
    }
    public Flight getFlight() {
        return flight;
    }
    public Airline getAirline() {
        return airline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingCode, booking.bookingCode) && Objects.equals(flight, booking.flight) && Objects.equals(airline, booking.airline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingCode, flight, airline);
    }
}
