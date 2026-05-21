package org.example.testutil;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class SqlTestSupport {
    private SqlTestSupport() {
    }

    public static RecordingConnection recordingConnection(int updateCount) {
        RecordingConnection recording = new RecordingConnection(updateCount);
        recording.connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("prepareStatement".equals(name)) {
                        recording.sql = (String) args[0];
                        return recording.preparedStatement();
                    }
                    if ("close".equals(name)) {
                        recording.connectionClosed = true;
                        return null;
                    }
                    if ("isClosed".equals(name)) {
                        return recording.connectionClosed;
                    }
                    throw new SQLException("Unsupported Connection method in test: " + name);
                });
        return recording;
    }

    public static final class RecordingConnection {
        private final int updateCount;
        private final Map<Integer, Object> parameters = new HashMap<>();
        private Connection connection;
        private String sql;
        private boolean statementClosed;
        private boolean connectionClosed;

        private RecordingConnection(int updateCount) {
            this.updateCount = updateCount;
        }

        public Connection connection() {
            return connection;
        }

        public String sql() {
            return sql;
        }

        public Object parameter(int index) {
            return parameters.get(index);
        }

        public boolean statementClosed() {
            return statementClosed;
        }

        private PreparedStatement preparedStatement() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class<?>[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if (name.startsWith("set") && args != null && args.length >= 2) {
                            parameters.put((Integer) args[0], args[1]);
                            return null;
                        }
                        if ("executeUpdate".equals(name)) {
                            return updateCount;
                        }
                        if ("close".equals(name)) {
                            statementClosed = true;
                            return null;
                        }
                        throw new SQLException("Unsupported PreparedStatement method in test: " + name);
                    });
        }
    }
}
