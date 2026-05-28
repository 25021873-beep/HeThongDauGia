package org.example.dao;

import org.example.entity.BidTransaction;
import org.example.testutil.SqlTestSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionDAOTest {

    @Test
    void addBidUsesProvidedConnectionAndBindsParameters() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(1);
        LocalDateTime bidTime = LocalDateTime.of(2026, 5, 18, 12, 0);
        BidTransaction bid = new BidTransaction(0, 2, 3, new BigDecimal("700"), bidTime);

        boolean inserted = new BidTransactionDAO().addBid(recording.connection(), bid);

        assertTrue(inserted);
        assertEquals("INSERT INTO bid_transactions (auction_id, bidder_id, bid_price, bid_time) VALUES (?, ?, ?, ?)",
                recording.sql());
        assertEquals(2, recording.parameter(1));
        assertEquals(3, recording.parameter(2));
        assertEquals(new BigDecimal("700"), recording.parameter(3));
        assertEquals(bidTime, recording.parameter(4));
        assertTrue(recording.statementClosed());
    }

    @Test
    void addBidReturnsFalseWhenNoRowsInserted() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(0);
        BidTransaction bid = new BidTransaction();

        assertFalse(new BidTransactionDAO().addBid(recording.connection(), bid));
    }
}
