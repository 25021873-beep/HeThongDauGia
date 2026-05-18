package org.example.dao;

import org.example.testutil.SqlTestSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDAOTest {

    @Test
    void updateCurrentPriceUsesProvidedConnectionAndBindsParameters() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(1);

        boolean updated = new AuctionDAO().updateCurrentPrice(
                recording.connection(), 7, new BigDecimal("1500"));

        assertTrue(updated);
        assertEquals("UPDATE Auctions SET current_price = ? WHERE id = ? AND status IN ('OPEN', 'RUNNING')",
                recording.sql());
        assertEquals(new BigDecimal("1500"), recording.parameter(1));
        assertEquals(7, recording.parameter(2));
        assertTrue(recording.statementClosed());
    }

    @Test
    void updateCurrentPriceReturnsFalseWhenNoRowsUpdated() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(0);

        assertFalse(new AuctionDAO().updateCurrentPrice(recording.connection(), 7, BigDecimal.ONE));
    }
}
