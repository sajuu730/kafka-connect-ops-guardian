package io.github.sajuu730.guardian.connect;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface ConnectApiClient {
    List<String> listConnectors();
    JsonNode connectorStatus(String connector);
    void restartTask(String connector, int taskId);
}
