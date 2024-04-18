package ar.edu.itba.pod.tpe1.data.utils;

import java.util.ArrayList;
import java.util.List;

public class Sector {
    private final String name;
    private final List<RangeCounter> assignedCounters = new ArrayList<RangeCounter>(1);

    public Sector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<RangeCounter> getAssignedCounters() {
        return assignedCounters;
    }

    public void addCounter(RangeCounter counter) {
        this.assignedCounters.add(counter);
    }
}
