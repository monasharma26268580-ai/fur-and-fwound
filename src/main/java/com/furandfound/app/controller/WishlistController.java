package com.furandfound.app.controller;

import com.furandfound.app.dao.ProductDao;
import com.furandfound.app.dao.UserDao;
import com.furandfound.app.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {
    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final ProductDao productDao;

    public WishlistController(JdbcTemplate jdbcTemplate, UserDao userDao, ProductDao productDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping("/wishlist")
    public String wishlist(Authentication authentication, Model model) {
        User user = userDao.findByEmail(authentication.getName());
        List<Map<String, Object>> items = jdbcTemplate.queryForList("SELECT p.id, p.name, p.slug, p.image_url, p.price FROM wishlist_items wi JOIN products p ON wi.product_id = p.id WHERE wi.user_id = ?", user.getId());
        model.addAttribute("items", items);
        return "wishlist";
    }

    @PostMapping("/wishlist/add/{slug}")
    public String addToWishlist(@PathVariable String slug, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        var product = productDao.findBySlug(slug);
        if (product != null) {
            jdbcTemplate.update("INSERT INTO wishlist_items(user_id, product_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = user_id", user.getId(), product.getId());
        }
        return "redirect:/wishlist";
    }

    @PostMapping("/wishlist/remove/{productId}")
    public String removeFromWishlist(@PathVariable Integer productId, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        jdbcTemplate.update("DELETE FROM wishlist_items WHERE user_id = ? AND product_id = ?", user.getId(), productId);
        return "redirect:/wishlist";
    }
}
