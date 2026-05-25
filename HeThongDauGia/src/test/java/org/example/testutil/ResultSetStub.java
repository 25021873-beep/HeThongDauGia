package org.example.testutil;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public final class ResultSetStub {
    private ResultSetStub() {
    }

    public static ResultSet from(Map<String, Object> values) {
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getString".equals(name)) {
                        Object value = values.get(args[0]);
                        return value == null ? null : value.toString();
                    }
                    if ("getInt".equals(name)) {
                        Object value = values.get(args[0]);
                        return value == null ? 0 : ((Number) value).intValue();
                    }
                    if ("getDouble".equals(name)) {
                        Object value = values.get(args[0]);
                        return value == null ? 0.0 : ((Number) value).doubleValue();
                    }
                    if ("getBigDecimal".equals(name)) {
                        return (BigDecimal) values.get(args[0]);
                    }
                    if ("getObject".equals(name) && args.length == 2 && args[1] == LocalDateTime.class) {
                        return (LocalDateTime) values.get(args[0]);
                    }
                    if ("close".equals(name)) {
                        return null;
                    }
                    if ("wasNull".equals(name)) {
                        return false;
                    }
                    throw new SQLException("Unsupported ResultSet method in test: " + name);
                });
    }
}
