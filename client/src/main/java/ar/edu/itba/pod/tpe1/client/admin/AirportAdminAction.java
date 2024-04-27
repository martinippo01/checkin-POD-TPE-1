package ar.edu.itba.pod.tpe1.client.admin;

import ar.edu.itba.pod.tpe1.client.Action;

import java.util.List;

public abstract class AirportAdminAction extends Action {
    public AirportAdminAction(List<String> actionArguments) {
        super(actionArguments);
    }
}
