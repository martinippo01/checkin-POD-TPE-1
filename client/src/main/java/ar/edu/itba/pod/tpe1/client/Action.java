package ar.edu.itba.pod.tpe1.client;

import io.grpc.ManagedChannel;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Map;

public interface Action {
    void run(ManagedChannel channel) throws ServiceUnavailableException;

    List<String> getActionArguments();
    void setArgumentsValues(Map<String, String> arguments);
}
