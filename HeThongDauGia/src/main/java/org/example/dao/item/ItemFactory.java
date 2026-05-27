package org.example.dao.item;


import org.example.entity.item.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemFactory {

    public static Item createItem(ResultSet rs, String prefix) throws SQLException {
        String type = rs.getString(prefix + "item_type");
        Item item;

        switch (type) {
            case "ELECTRONICS":
                item = new Electronics();
                ((Electronics) item).setWarrantyMonths(rs.getInt(prefix + "warranty_months"));
                break;
            case "ART":
                item = new Art();
                ((Art) item).setAuthor(rs.getString(prefix + "author"));
                break;
            case "VEHICLE":
                item = new Vehicle();
                ((Vehicle) item).setEngineType(rs.getString(prefix + "engine_type"));
                break;
            default:
                throw new SQLException("Loại hàng không tồn tại: " + type);
        }

        item.setId(rs.getInt(prefix + "id"));
        item.setName(rs.getString(prefix + "name"));
        item.setDescription(rs.getString(prefix + "description"));
        item.setStartingPrice(rs.getBigDecimal(prefix + "starting_price"));
        item.setStatus(rs.getString(prefix + "status"));

        return item;
    }

    public static Item createItem(ResultSet rs) throws SQLException {
        String type = rs.getString("item_type");
        Item item;

        switch (type) {
            case "ELECTRONICS":
                item = new Electronics();
                ((Electronics) item).setWarrantyMonths(rs.getInt("warranty_months"));
                break;
            case "ART":
                item = new Art();
                ((Art) item).setAuthor(rs.getString("author"));
                break;
            case "VEHICLE":
                item = new Vehicle();
                ((Vehicle) item).setEngineType(rs.getString("engine_type"));
                break;
            default:
                throw new SQLException("Loại hàng không tồn tại: " + type);
        }

        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getBigDecimal("starting_price"));
        item.setStatus(rs.getString("status"));

        return item;
    }
}
