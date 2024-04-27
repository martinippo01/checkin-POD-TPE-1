package ar.edu.itba.pod.tpe1.client.counter;

import ar.edu.itba.pod.tpe1.client.Action;

import java.util.List;

public abstract class CounterReservationAction extends Action {
    public CounterReservationAction(List<String> actionArguments) {
        super(actionArguments);
    }
}
