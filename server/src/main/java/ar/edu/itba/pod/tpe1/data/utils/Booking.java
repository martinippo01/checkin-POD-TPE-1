package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class Booking {
    private final String bookingCode;

    public Booking(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getBookingCode() {
        return bookingCode;
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
