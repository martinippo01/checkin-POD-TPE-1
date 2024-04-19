package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class Booking {

    private final String bookingCode;
    private final Flight flight;

    public Booking(String bookingCode, Flight flight) {
        this.bookingCode = bookingCode;
        this.flight = flight;
    }

    public String getBookingCode() {
        return bookingCode;
    }
    public Flight getFlight() {
        return flight;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingCode, booking.bookingCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingCode);
    }
}
