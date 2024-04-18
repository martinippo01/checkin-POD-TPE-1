package ar.edu.itba.pod.tpe1.data.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Sector implements Comparable<Sector> {

    private final String name;


    public Sector(String name) {
        this.name = name;
    }

    public static Sector fromName(String name) {
        return new Sector(name);
    }

    public String getName() {
        return name;
    }


    @Override
    public int compareTo(Sector sector) {
        return name.compareTo(sector.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sector sector = (Sector) o;
        return Objects.equals(name, sector.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
