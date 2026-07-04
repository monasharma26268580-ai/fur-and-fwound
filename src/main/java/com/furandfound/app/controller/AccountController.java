package com.furandfound.app.controller;

import com.furandfound.app.dao.CartDao;
import com.furandfound.app.dao.OrderDao;
import com.furandfound.app.dao.UserDao;
import com.furandfound.app.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {
    private final UserDao userDao;
    private final CartDao cartDao;
    private final OrderDao orderDao;

    public AccountController(UserDao userDao, CartDao cartDao, OrderDao orderDao) {
        this.userDao = userDao;
        this.cartDao = cartDao;
        this.orderDao = orderDao;
    }

    @GetMapping("/account")
    public String account(Authentication authentication, Model model) {
        User user = userDao.findByEmail(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartDao.findByUserId(user.getId()));
        model.addAttribute("orders", orderDao.findByUserId(user.getId()));
        return "account";
    }
}
