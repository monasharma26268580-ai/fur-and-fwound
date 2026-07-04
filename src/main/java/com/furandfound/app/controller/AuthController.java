package com.furandfound.app.controller;

import com.furandfound.app.dao.UserDao;
import com.furandfound.app.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.SecureRandom;

@Controller
public class AuthController {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(UserDao userDao, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("otpMode", "email");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("user") RegistrationForm form, Model model) {
        if (userDao.existsByEmail(form.email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        User user = new User();
        user.setFullName(form.fullName);
        user.setEmail(form.email);
        user.setPasswordHash(passwordEncoder.encode(form.password));
        user.setPhone(form.phone);
        user.setRole("CUSTOMER");
        userDao.save(user);
        return "redirect:/login";
    }

    @PostMapping("/auth/google/demo")
    public String googleDemoLogin(HttpServletRequest request) {
        User user = userDao.findOrCreateCustomer(
                "Google Customer",
                "google.customer@furandfound.local",
                "",
                passwordEncoder.encode("google-demo-login")
        );
        signIn(user.getEmail(), request);
        return "redirect:/account";
    }

    @PostMapping("/auth/otp/send")
    public String sendOtp(@RequestParam String channel, @RequestParam String destination, HttpSession session, Model model) {
        String cleanDestination = destination == null ? "" : destination.trim();
        if (cleanDestination.isBlank()) {
            model.addAttribute("error", "Please enter your email or phone number.");
            model.addAttribute("otpMode", channel);
            return "login";
        }
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));
        session.setAttribute("otp_channel", channel);
        session.setAttribute("otp_destination", cleanDestination);
        session.setAttribute("otp_code", otp);
        model.addAttribute("otpSent", true);
        model.addAttribute("otpMode", channel);
        model.addAttribute("destination", cleanDestination);
        model.addAttribute("demoOtp", otp);
        return "login";
    }

    @PostMapping("/auth/otp/verify")
    public String verifyOtp(@RequestParam String otp, HttpSession session, HttpServletRequest request, Model model) {
        String expected = (String) session.getAttribute("otp_code");
        String channel = (String) session.getAttribute("otp_channel");
        String destination = (String) session.getAttribute("otp_destination");
        if (expected == null || !expected.equals(otp)) {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("otpSent", true);
            model.addAttribute("otpMode", channel == null ? "email" : channel);
            model.addAttribute("destination", destination);
            return "login";
        }

        String email = "email".equals(channel) ? destination : phoneEmail(destination);
        String phone = "phone".equals(channel) ? destination : "";
        String name = "email".equals(channel) ? destination.substring(0, destination.indexOf("@") > 0 ? destination.indexOf("@") : destination.length()) : "Phone Customer";
        User user = userDao.findOrCreateCustomer(name, email, phone, passwordEncoder.encode("otp-login-" + System.currentTimeMillis()));
        session.removeAttribute("otp_code");
        session.removeAttribute("otp_channel");
        session.removeAttribute("otp_destination");
        signIn(user.getEmail(), request);
        return "redirect:/account";
    }

    private void signIn(String email, HttpServletRequest request) {
        var userDetails = userDetailsService.loadUserByUsername(email);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private String phoneEmail(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        return "phone_" + (digits.isBlank() ? System.currentTimeMillis() : digits) + "@furandfound.local";
    }

    public static class RegistrationForm {
        @NotBlank
        private String fullName;
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
        private String phone;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
