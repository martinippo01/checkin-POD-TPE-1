package ar.edu.itba.pod.tpe1.client.notifications;


import ar.edu.itba.pod.tpe1.client.Action;

import java.util.List;


public abstract class NotificationsAction extends Action {
    public NotificationsAction(List<String> actionArguments) {
        super(actionArguments);
    }
}
