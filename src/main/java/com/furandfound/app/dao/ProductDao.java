package com.furandfound.app.dao;

import com.furandfound.app.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductDao {
    private final JdbcTemplate jdbcTemplate;

    public ProductDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAllVisible() {
        String sql = "SELECT * FROM products WHERE is_visible = TRUE ORDER BY id DESC";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    public List<Product> findFeatured() {
        String sql = "SELECT * FROM products WHERE is_visible = TRUE AND is_featured = TRUE ORDER BY id DESC";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    public List<Product> findByCategorySlug(String categorySlug) {
        String sql = "SELECT p.* FROM products p JOIN categories c ON p.category_id = c.id WHERE p.is_visible = TRUE AND c.slug = ? ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, new Object[]{categorySlug}, new ProductRowMapper());
    }

    public List<Product> search(String term) {
        String sql = "SELECT * FROM products WHERE is_visible = TRUE AND (name LIKE ? OR description LIKE ? OR seo_description LIKE ?) ORDER BY id DESC";
        String like = "%" + term + "%";
        return jdbcTemplate.query(sql, new Object[]{like, like, like}, new ProductRowMapper());
    }

    public Product findBySlug(String slug) {
        List<Product> products = jdbcTemplate.query("SELECT * FROM products WHERE slug = ?", new Object[]{slug}, new ProductRowMapper());
        return products.isEmpty() ? null : products.get(0);
    }

    public void save(Product product) {
        jdbcTemplate.update(
                "INSERT INTO products(category_id, sku, name, slug, description, highlights, care_info, ideal_use, seo_description, price, stock_quantity, is_featured, is_visible, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                product.getCategoryId(), product.getSku(), product.getName(), product.getSlug(), product.getDescription(), product.getHighlights(), product.getCareInfo(), product.getIdealUse(), product.getSeoDescription(), product.getPrice(), product.getStockQuantity(), product.isFeatured(), product.isVisible(), product.getImageUrl()
        );
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setCategoryId(rs.getInt("category_id"));
            product.setSku(rs.getString("sku"));
            product.setName(rs.getString("name"));
            product.setSlug(rs.getString("slug"));
            product.setDescription(rs.getString("description"));
            product.setHighlights(rs.getString("highlights"));
            product.setCareInfo(rs.getString("care_info"));
            product.setIdealUse(rs.getString("ideal_use"));
            product.setSeoDescription(rs.getString("seo_description"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setStockQuantity(rs.getInt("stock_quantity"));
            product.setFeatured(rs.getBoolean("is_featured"));
            product.setVisible(rs.getBoolean("is_visible"));
            product.setImageUrl(rs.getString("image_url"));
            return product;
        }
    }
}
