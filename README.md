# Fur & Found Ecommerce

Premium pet-care ecommerce website built with Java, Spring Boot MVC, Thymeleaf, JDBC, MySQL-ready schema, and Razorpay checkout hooks.

## What Is Implemented

- Public storefront: home, shop, category, product detail, travel with pets, adoption, journal, about, contact, and policy pages.
- Customer account flow: register, login, logout, account dashboard, wishlist, bag, and checkout.
- Admin area: private product dashboard and product creation.
- JDBC schema: users, roles, categories, products, variants, images, cart, wishlist, orders, payments, travel partners, blog posts, and audit logs.
- Seed data: Fur & Found categories, product catalog, travel partners, and journal posts from the provided documents.
- Security basics: BCrypt password hashing, Spring Security sessions, CSRF form protection, prepared JDBC statements, admin role protection, and production secure-cookie settings.
- Payment: Razorpay-ready checkout response and backend HMAC signature verification when a real Razorpay secret is configured.

## Run Locally

Install Maven, then run:

```powershell
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

The default local profile uses an in-memory H2 database in MySQL compatibility mode.

## Demo Accounts

The seed file creates:

```text
admin@furandfound.com
customer@furandfound.com
```

Set fresh passwords for these seeded accounts before publishing.

## Production Publishing

Use the production profile:

```powershell
mvn clean package
java -jar target/fur-and-found-1.0.0.jar --spring.profiles.active=prod
```

Required environment variables are listed in `.env.example`. Do not commit real DB passwords or Razorpay secrets.

Before going live:

- Create a MySQL database and run `src/main/resources/schema.sql`.
- Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `RAZORPAY_KEY_ID`, and `RAZORPAY_KEY_SECRET`.
- Put the app behind HTTPS using a reverse proxy or hosting provider SSL.
- Set Razorpay live keys only after test payments pass.
- Replace demo users and passwords.
- Publish privacy policy, shipping policy, return policy, terms, and contact information.

## Private Data Rule

Supplier links, supplier phone numbers, COGS, margin percentages, and the Optimized Pricing & Margin Matrix are intentionally excluded from public templates and seed data.
