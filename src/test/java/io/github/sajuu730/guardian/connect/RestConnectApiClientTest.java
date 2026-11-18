package io.github.sajuu730.guardian.connect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.sajuu730.guardian.config.GuardianProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestConnectApiClientTest {
    @Test
    void listsConnectorsFromDistributedRestApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GuardianProperties properties = new GuardianProperties();
        properties.getConnect().setBaseUrl("http://connect.test:8083");
        RestConnectApiClient client = new RestConnectApiClient(builder, properties);

        server.expect(requestTo("http://connect.test:8083/connectors"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[\"orders\",\"payments\"]", MediaType.APPLICATION_JSON));

        assertEquals(java.util.List.of("orders", "payments"), client.listConnectors());
        server.verify();
    }

    @Test
    void requestsTaskRestartUsingConnectRestEndpoint() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GuardianProperties properties = new GuardianProperties();
        properties.getConnect().setBaseUrl("http://connect.test:8083");
        RestConnectApiClient client = new RestConnectApiClient(builder, properties);

        server.expect(requestTo("http://connect.test:8083/connectors/orders/tasks/2/restart"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.restartTask("orders", 2);
        server.verify();
    }
}
