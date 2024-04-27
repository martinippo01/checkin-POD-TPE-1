package ar.edu.itba.pod.tpe1.data.utils;

public class CheckIn {
    private CheckInStatus status;
    private Flight flight;
    private int counterWhereCheckInWasDone;
    private RequestedRangeCounter rangeCounter;
    private Sector sector;

    public CheckIn(CheckInStatus status, Flight flight) {
        this.status = status;
        this.flight = flight;
    }

    public CheckIn(CheckInStatus status, Flight flight, RequestedRangeCounter rangeCounter, Sector sector) {
        this.status = status;
        this.flight = flight;
        this.rangeCounter = rangeCounter;
        this.sector = sector;
    }

    public CheckIn(CheckInStatus status, Flight flight, int counterWhereCheckInWasDone, Sector sector) {
        this.status = status;
        this.flight = flight;
        this.counterWhereCheckInWasDone = counterWhereCheckInWasDone;
        this.sector = sector;
    }

    public CheckInStatus getStatus() {
        return status;
    }

    public void setStatus(CheckInStatus status) {
        this.status = status;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public int getCounterWhereCheckInWasDone() {
        return counterWhereCheckInWasDone;
    }

    public void setCounterWhereCheckInWasDone(int counterWhereCheckInWasDone) {
        this.counterWhereCheckInWasDone = counterWhereCheckInWasDone;
    }

    public RequestedRangeCounter getRangeCounter() {
        return rangeCounter;
    }

    public void setRangeCounter(RequestedRangeCounter rangeCounter) {
        this.rangeCounter = rangeCounter;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}
