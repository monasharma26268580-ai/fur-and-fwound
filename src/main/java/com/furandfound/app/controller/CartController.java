package com.furandfound.app.controller;

import com.furandfound.app.dao.CartDao;
import com.furandfound.app.dao.ProductDao;
import com.furandfound.app.dao.UserDao;
import com.furandfound.app.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {
    private final CartDao cartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    public CartController(CartDao cartDao, UserDao userDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @PostMapping("/cart/add/{slug}")
    public String addToCart(@PathVariable String slug, @RequestParam(defaultValue = "1") int quantity, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        var product = productDao.findBySlug(slug);
        if (product != null) {
            cartDao.addItem(user.getId(), product.getId(), quantity);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Authentication authentication, Model model) {
        User user = userDao.findByEmail(authentication.getName());
        model.addAttribute("items", cartDao.findByUserId(user.getId()));
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Integer productId, @RequestParam int quantity, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        if (quantity <= 0) {
            cartDao.removeItem(user.getId(), productId);
        } else {
            cartDao.updateQuantity(user.getId(), productId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable Integer productId, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        cartDao.removeItem(user.getId(), productId);
        return "redirect:/cart";
    }
}
