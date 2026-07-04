package com.furandfound.app.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer createOrder(Integer userId, java.math.BigDecimal totalAmount, String address, String paymentRef) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO orders(user_id, total_amount, shipping_address, payment_status, payment_reference) VALUES (?, ?, ?, 'PENDING', ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setBigDecimal(2, totalAmount);
            ps.setString(3, address);
            ps.setString(4, paymentRef);
            return ps;
        }, holder);
        Number generatedId = holder.getKey();
        return generatedId != null ? generatedId.intValue() : 0;
    }

    public void addOrderItem(Integer orderId, Integer productId, String productName, int quantity, java.math.BigDecimal unitPrice) {
        jdbcTemplate.update("INSERT INTO order_items(order_id, product_id, product_name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)", orderId, productId, productName, quantity, unitPrice);
    }

    public void updatePaymentStatus(Integer orderId, String status) {
        jdbcTemplate.update("UPDATE orders SET payment_status = ? WHERE id = ?", status, orderId);
    }

    public void updateOrderStatus(Integer orderId, String status) {
        jdbcTemplate.update("UPDATE orders SET status = ? WHERE id = ?", status, orderId);
    }

    public void createPayment(Integer orderId, String providerOrderId, java.math.BigDecimal amount) {
        jdbcTemplate.update("INSERT INTO payments(order_id, provider, provider_order_id, amount, status) VALUES (?, 'RAZORPAY', ?, ?, 'PENDING')", orderId, providerOrderId, amount);
    }

    public void markPaymentPaid(Integer orderId, String providerPaymentId, String signature) {
        jdbcTemplate.update("UPDATE payments SET status = 'PAID', provider_payment_id = ?, signature = ?, paid_at = CURRENT_TIMESTAMP WHERE order_id = ?", providerPaymentId, signature, orderId);
    }

    public List<Map<String, Object>> findByUserId(Integer userId) {
        return jdbcTemplate.queryForList("SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC", userId);
    }
}
