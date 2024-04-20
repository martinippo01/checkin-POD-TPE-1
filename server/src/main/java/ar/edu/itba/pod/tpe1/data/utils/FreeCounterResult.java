package ar.edu.itba.pod.tpe1.data.utils;

import java.util.List;

public class FreeCounterResult {
    private final String sectorName;
    private final int rangeStart;
    private final int rangeEnd;
    private final String airlineName;
    private final List<String> flights;

    public FreeCounterResult(String sectorName, int rangeStart, int rangeEnd, String airlineName, List<String> flights) {
        this.sectorName = sectorName;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.airlineName = airlineName;
        this.flights = flights;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public List<String> getFlights() {
        return flights;
    }
}

