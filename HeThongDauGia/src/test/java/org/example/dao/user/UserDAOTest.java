package org.example.dao.user;

import org.example.testutil.SqlTestSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    @Test
    void updateBalanceUsesProvidedConnectionAndBindsParameters() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(1);

        boolean updated = new UserDAO().addBalance(recording.connection(), 3, new BigDecimal("1200"));

        assertTrue(updated);
        assertEquals("UPDATE Users SET balance = ? WHERE id = ?", recording.sql());
        assertEquals(new BigDecimal("1200"), recording.parameter(1));
        assertEquals(3, recording.parameter(2));
        assertTrue(recording.statementClosed());
    }

    @Test
    void updateBalanceReturnsFalseWhenNoRowsUpdated() throws SQLException {
        SqlTestSupport.RecordingConnection recording = SqlTestSupport.recordingConnection(0);

        assertFalse(new UserDAO().addBalance(recording.connection(), 3, BigDecimal.ZERO));
    }
}
