package ar.edu.itba.pod.tpe1.client.checkin;

import ar.edu.itba.pod.tpe1.client.Action;
import checkin.CheckinServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CheckInAction extends Action {
    public CheckInAction(List<String> actionArguments) {
        super(actionArguments);
    }
}
