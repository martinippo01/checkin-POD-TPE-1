package ar.edu.itba.pod.tpe1.client.checkin;

import ar.edu.itba.pod.tpe1.client.Action;

import java.util.List;

public abstract class CheckInAction extends Action {
    public CheckInAction(List<String> actionArguments) {
        super(actionArguments);
    }
}
