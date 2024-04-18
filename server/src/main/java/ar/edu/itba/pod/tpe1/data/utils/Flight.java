package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class Flight {

    private final String flightCode;

    public Flight(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightCode, flight.flightCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(flightCode);
    }
}
