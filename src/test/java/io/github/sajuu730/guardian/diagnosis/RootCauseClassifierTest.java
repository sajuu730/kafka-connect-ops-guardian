package io.github.sajuu730.guardian.diagnosis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RootCauseClassifierTest {
    private final RootCauseClassifier classifier = new RootCauseClassifier();

    @Test void classifiesAuthenticationFailures() {
        assertEquals(RootCause.AUTHENTICATION, classifier.classify("SASL authentication failed"));
    }
    @Test void classifiesNetworkFailures() {
        assertEquals(RootCause.NETWORK, classifier.classify("java.net.ConnectException: Connection refused"));
    }
    @Test void classifiesDatabaseFailures() {
        assertEquals(RootCause.DATABASE, classifier.classify("org.postgresql.util.PSQLException"));
    }
    @Test void preservesUnknownAsUnknown() {
        assertEquals(RootCause.UNKNOWN, classifier.classify("unexpected failure"));
    }
}
