package com.furandfound.app.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.furandfound.app.dao.CartDao;
import com.furandfound.app.dao.OrderDao;
import com.furandfound.app.dao.UserDao;
import com.furandfound.app.model.User;

@Controller
public class CheckoutController {
    private final CartDao cartDao;
    private final UserDao userDao;
    private final OrderDao orderDao;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;
    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    public CheckoutController(CartDao cartDao, UserDao userDao, OrderDao orderDao) {
        this.cartDao = cartDao;
        this.userDao = userDao;
        this.orderDao = orderDao;
    }

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        User user = userDao.findByEmail(authentication.getName());
        model.addAttribute("items", cartDao.findByUserId(user.getId()));
        model.addAttribute("razorpayKeyId", razorpayKeyId);
        return "checkout";
    }

    @PostMapping("/api/checkout/razorpay-order")
    @ResponseBody
    public Map<String, Object> createRazorpayOrder(@RequestBody(required = false) Map<String, Object> payload, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        var items = cartDao.findByUserId(user.getId());
        BigDecimal total = items.stream()
                .filter(item -> item.getPrice() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean demoMode = razorpayKeySecret == null || razorpayKeySecret.equals("demo-secret");
        String providerOrderId = demoMode ? "order_ff_" + System.currentTimeMillis() : createRazorpayProviderOrder(total);
        if (providerOrderId == null) {
            return Map.of("error", "Unable to create Razorpay order. Please try again.");
        }
        Integer orderId = orderDao.createOrder(user.getId(), total, payload == null ? "Address to be confirmed" : String.valueOf(payload.getOrDefault("address", "Address to be confirmed")), providerOrderId);
        for (var item : items) {
            orderDao.addOrderItem(orderId, item.getProductId(), item.getProductName(), item.getQuantity(), item.getPrice());
        }
        orderDao.createPayment(orderId, providerOrderId, total);
        Map<String, Object> response = new HashMap<>();
        response.put("localOrderId", orderId);
        response.put("orderId", providerOrderId);
        response.put("amount", total.multiply(BigDecimal.valueOf(100)).intValueExact());
        response.put("currency", "INR");
        response.put("name", "Fur & Found");
        response.put("description", "Pet care essentials");
        response.put("key", razorpayKeyId);
        return response;
    }

    @PostMapping("/checkout/confirm")
    @ResponseBody
    public Map<String, Object> confirmCheckout(@RequestBody Map<String, Object> payload, Authentication authentication) {
        User user = userDao.findByEmail(authentication.getName());
        Integer orderId = Integer.valueOf(String.valueOf(payload.get("localOrderId")));
        String providerOrderId = String.valueOf(payload.getOrDefault("razorpay_order_id", ""));
        String providerPaymentId = String.valueOf(payload.getOrDefault("razorpay_payment_id", "demo-payment"));
        String signature = String.valueOf(payload.getOrDefault("razorpay_signature", "demo-signature"));
        boolean demoMode = razorpayKeySecret == null || razorpayKeySecret.equals("demo-secret");
        if (!demoMode && !isValidSignature(providerOrderId, providerPaymentId, signature)) {
            orderDao.updatePaymentStatus(orderId, "FAILED");
            return Map.of("ok", false, "message", "Payment verification failed.");
        }
        cartDao.clear(user.getId());
        orderDao.markPaymentPaid(orderId, providerPaymentId, signature);
        orderDao.updatePaymentStatus(orderId, "PAID");
        orderDao.updateOrderStatus(orderId, "CONFIRMED");
        return Map.of("ok", true, "redirect", "/account");
    }

    private boolean isValidSignature(String orderId, String paymentId, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(signature);
        } catch (Exception ex) {
            return false;
        }
    }

    private String createRazorpayProviderOrder(BigDecimal total) {
        try {
            int paise = total.multiply(BigDecimal.valueOf(100)).intValueExact();
            String auth = Base64.getEncoder().encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes(StandardCharsets.UTF_8));
            String body = "{\"amount\":" + paise + ",\"currency\":\"INR\",\"receipt\":\"ff_" + System.currentTimeMillis() + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());
            return matcher.find() ? matcher.group(1) : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
