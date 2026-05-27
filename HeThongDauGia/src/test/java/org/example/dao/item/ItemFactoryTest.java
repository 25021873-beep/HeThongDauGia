package org.example.dao.item;

import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.testutil.ResultSetStub;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    @Test
    void createsElectronicsFromResultSet() throws SQLException {
        ResultSet rs = ResultSetStub.from(baseValues("ELECTRONICS", Map.of("warranty_months", 24)));

        Item item = ItemFactory.createItem(rs);

        assertInstanceOf(Electronics.class, item);
        assertEquals(24, ((Electronics) item).getWarrantyMonths());
        assertCommonFields(item);
    }

    @Test
    void createsArtFromResultSet() throws SQLException {
        ResultSet rs = ResultSetStub.from(baseValues("ART", Map.of("author", "Picasso")));

        Item item = ItemFactory.createItem(rs);

        assertInstanceOf(Art.class, item);
        assertEquals("Picasso", ((Art) item).getAuthor());
        assertCommonFields(item);
    }

    @Test
    void createsVehicleFromResultSet() throws SQLException {
        ResultSet rs = ResultSetStub.from(baseValues("VEHICLE", Map.of("engine_type", "V8")));

        Item item = ItemFactory.createItem(rs);

        assertInstanceOf(Vehicle.class, item);
        assertEquals("V8", ((Vehicle) item).getEngineType());
        assertCommonFields(item);
    }

    @Test
    void throwsForUnknownType() {
        ResultSet rs = ResultSetStub.from(baseValues("BOOK", Map.of()));

        assertThrows(SQLException.class, () -> ItemFactory.createItem(rs));
    }

    private static Map<String, Object> baseValues(String type, Map<String, Object> extra) {
        Map<String, Object> values = new HashMap<>();
        values.put("item_type", type);
        values.put("id", 10);
        values.put("name", "Item");
        values.put("description", "Description");
        values.put("starting_price", new BigDecimal("100.00"));
        values.put("status", "AVAILABLE");
        values.putAll(extra);
        return values;
    }

    private static void assertCommonFields(Item item) {
        assertEquals(10, item.getId());
        assertEquals("Item", item.getName());
        assertEquals("Description", item.getDescription());
        assertEquals(new BigDecimal("100.00"), item.getStartingPrice());
        assertEquals("AVAILABLE", item.getStatus());
    }
}
