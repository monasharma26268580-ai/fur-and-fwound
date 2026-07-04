package com.furandfound.app.dao;

import com.furandfound.app.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CartDao {
    private final JdbcTemplate jdbcTemplate;

    public CartDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CartItem> findByUserId(Integer userId) {
        String sql = "SELECT p.id as product_id, p.name as product_name, p.image_url, p.price, ci.quantity FROM cart_items ci JOIN products p ON ci.product_id = p.id WHERE ci.user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new CartItemRowMapper());
    }

    public void addItem(Integer userId, Integer productId, int quantity) {
        jdbcTemplate.update("INSERT INTO cart_items(user_id, product_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + ?", userId, productId, quantity, quantity);
    }

    public void updateQuantity(Integer userId, Integer productId, int quantity) {
        jdbcTemplate.update("UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?", quantity, userId, productId);
    }

    public void removeItem(Integer userId, Integer productId) {
        jdbcTemplate.update("DELETE FROM cart_items WHERE user_id = ? AND product_id = ?", userId, productId);
    }

    public void clear(Integer userId) {
        jdbcTemplate.update("DELETE FROM cart_items WHERE user_id = ?", userId);
    }

    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            CartItem item = new CartItem();
            item.setProductId(rs.getInt("product_id"));
            item.setProductName(rs.getString("product_name"));
            item.setImageUrl(rs.getString("image_url"));
            item.setPrice(rs.getBigDecimal("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        }
    }
}
