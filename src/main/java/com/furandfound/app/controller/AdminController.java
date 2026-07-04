package com.furandfound.app.controller;

import com.furandfound.app.dao.ProductDao;
import com.furandfound.app.model.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ProductDao productDao;

    public AdminController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("products", productDao.findAllVisible());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productDao.findAllVisible());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String saveProduct(Product product) {
        product.setVisible(true);
        productDao.save(product);
        return "redirect:/admin/products";
    }
}
