package io.github.sajuu730.guardian.monitor;

import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sajuu730.guardian.config.GuardianProperties;
import io.github.sajuu730.guardian.connect.ConnectApiClient;
import io.github.sajuu730.guardian.diagnosis.RootCauseClassifier;
import io.github.sajuu730.guardian.incident.IncidentPublisher;
import io.github.sajuu730.guardian.recovery.RestartCircuitBreaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConnectHealthMonitorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void reportsFailedTaskWithoutRestartWhenRecoveryDisabled() throws Exception {
        ConnectApiClient api = mock(ConnectApiClient.class);
        IncidentPublisher publisher = mock(IncidentPublisher.class);
        GuardianProperties properties = new GuardianProperties();
        when(api.listConnectors()).thenReturn(List.of("orders"));
        when(api.connectorStatus("orders")).thenReturn(mapper.readTree("""
            {"name":"orders","connector":{"state":"RUNNING"},
             "tasks":[{"id":0,"state":"FAILED","trace":"Connection refused"}]}
            """));

        ConnectHealthMonitor monitor = new ConnectHealthMonitor(api, properties,
                new RootCauseClassifier(), new RestartCircuitBreaker(), publisher,
                new SimpleMeterRegistry());
        monitor.poll();

        verify(publisher).publish(any());
        verify(api, never()).restartTask(anyString(), anyInt());
    }

    @Test void restartsFailedTaskWhenExplicitlyEnabled() throws Exception {
        ConnectApiClient api = mock(ConnectApiClient.class);
        IncidentPublisher publisher = mock(IncidentPublisher.class);
        GuardianProperties properties = new GuardianProperties();
        properties.getRecovery().setEnabled(true);
        properties.getRecovery().setCooldown(Duration.ZERO);
        when(api.listConnectors()).thenReturn(List.of("orders"));
        when(api.connectorStatus("orders")).thenReturn(mapper.readTree("""
            {"name":"orders","connector":{"state":"RUNNING"},
             "tasks":[{"id":2,"state":"FAILED","trace":"SQL connection timeout"}]}
            """));

        ConnectHealthMonitor monitor = new ConnectHealthMonitor(api, properties,
                new RootCauseClassifier(), new RestartCircuitBreaker(), publisher,
                new SimpleMeterRegistry());
        monitor.poll();

        verify(api).restartTask("orders", 2);
        verify(publisher).publish(any());
    }

    @Test void continuesInspectingOtherConnectorsWhenOneStatusRequestFails() throws Exception {
        ConnectApiClient api = mock(ConnectApiClient.class);
        IncidentPublisher publisher = mock(IncidentPublisher.class);
        GuardianProperties properties = new GuardianProperties();
        when(api.listConnectors()).thenReturn(List.of("broken", "orders"));
        when(api.connectorStatus("broken")).thenThrow(new IllegalStateException("temporary REST failure"));
        when(api.connectorStatus("orders")).thenReturn(mapper.readTree("""
            {"name":"orders","connector":{"state":"RUNNING"},
             "tasks":[{"id":1,"state":"FAILED","trace":"Connection refused"}]}
            """));

        ConnectHealthMonitor monitor = new ConnectHealthMonitor(api, properties,
                new RootCauseClassifier(), new RestartCircuitBreaker(), publisher,
                new SimpleMeterRegistry());
        monitor.poll();

        verify(publisher).publish(any());
        verify(api).connectorStatus("orders");
    }

    @Test void continuesPollingWhenIncidentPublisherFails() throws Exception {
        ConnectApiClient api = mock(ConnectApiClient.class);
        IncidentPublisher publisher = mock(IncidentPublisher.class);
        doThrow(new IllegalStateException("webhook unavailable")).when(publisher).publish(any());
        GuardianProperties properties = new GuardianProperties();
        when(api.listConnectors()).thenReturn(List.of("orders", "payments"));
        when(api.connectorStatus(anyString())).thenReturn(mapper.readTree("""
            {"connector":{"state":"RUNNING"},
             "tasks":[{"id":0,"state":"FAILED","trace":"Connection refused"}]}
            """));

        ConnectHealthMonitor monitor = new ConnectHealthMonitor(api, properties,
                new RootCauseClassifier(), new RestartCircuitBreaker(), publisher,
                new SimpleMeterRegistry());
        monitor.poll();

        verify(api).connectorStatus("orders");
        verify(api).connectorStatus("payments");
        verify(publisher, times(2)).publish(any());
    }
}
