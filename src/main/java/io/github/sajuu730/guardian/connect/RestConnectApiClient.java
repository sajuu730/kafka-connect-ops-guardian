package io.github.sajuu730.guardian.connect;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import io.github.sajuu730.guardian.config.GuardianProperties;

@Component
public class RestConnectApiClient implements ConnectApiClient {
    private final RestClient client;

    public RestConnectApiClient(RestClient.Builder builder, GuardianProperties properties) {
        Duration timeout = properties.getConnect().getRequestTimeout();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.client = builder.baseUrl(properties.getConnect().getBaseUrl())
                .requestFactory(factory).build();
    }

    @Override
    public List<String> listConnectors() {
        String[] names = client.get().uri("/connectors").retrieve().body(String[].class);
        return names == null ? List.of() : List.of(names);
    }

    @Override
    public JsonNode connectorStatus(String connector) {
        return client.get().uri("/connectors/{name}/status", connector)
                .retrieve().body(JsonNode.class);
    }

    @Override
    public void restartTask(String connector, int taskId) {
        client.post().uri("/connectors/{name}/tasks/{task}/restart", connector, taskId)
                .retrieve().toBodilessEntity();
    }
}
