package ar.edu.itba.pod.tpe1.data.utils;

import java.util.Objects;

public class Airline {

    private final String name;
    private Flight flight;

    public Airline(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return Objects.equals(name, airline.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
