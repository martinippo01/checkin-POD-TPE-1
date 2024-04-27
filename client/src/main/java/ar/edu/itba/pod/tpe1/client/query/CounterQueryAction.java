package ar.edu.itba.pod.tpe1.client.query;

import ar.edu.itba.pod.tpe1.client.Action;

import java.util.List;

public abstract class CounterQueryAction extends Action {
    public CounterQueryAction(List<String> actionArguments) {
        super(actionArguments);
    }

    public CounterQueryAction(List<String> actionArguments, List<String> optionalActionArguments) {
        super(actionArguments, optionalActionArguments);
    }
}
