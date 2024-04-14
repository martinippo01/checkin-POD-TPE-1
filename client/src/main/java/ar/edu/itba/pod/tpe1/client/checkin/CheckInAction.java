package ar.edu.itba.pod.tpe1.client.checkin;

import ar.edu.itba.pod.tpe1.client.Action;
import checkin.CheckinServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CheckInAction implements Action {
    protected final List<String> actionArguments;
    protected final HashMap<String, String> arguments = new HashMap<>(1);

    public CheckInAction(List<String> actionArguments) {
        this.actionArguments = actionArguments;
    }

    @Override
    public List<String> getActionArguments() {
        return actionArguments;
    }

    @Override
    public void setArgumentsValues(Map<String, String> arguments) {
        for (String actionArgument : actionArguments) {
            if(!arguments.containsKey(actionArgument)) {
                throw new IllegalArgumentException("Argument with key '" + actionArgument + "' is required for this action.");
            }

            this.arguments.put(actionArgument, arguments.get(actionArgument));
        }
    }
}
