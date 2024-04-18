package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public final class Booking {
    private final String code;
    private Airline airline;

    public Booking(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;
        return Objects.equals(getCode(), booking.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCode());
    }
}
