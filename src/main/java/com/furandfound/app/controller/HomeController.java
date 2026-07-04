package com.furandfound.app.controller;

import com.furandfound.app.dao.ProductDao;
import com.furandfound.app.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    private final ProductDao productDao;

    public HomeController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productDao.findAllVisible();
        model.addAttribute("products", products);
        model.addAttribute("featuredProducts", productDao.findFeatured());
        model.addAttribute("pageTitle", "Fur & Found");
        return "home";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String q, Model model) {
        List<Product> products = (q == null || q.isBlank()) ? productDao.findAllVisible() : productDao.search(q);
        model.addAttribute("products", products);
        model.addAttribute("query", q);
        return "shop";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, Model model) {
        model.addAttribute("products", productDao.findByCategorySlug(slug));
        model.addAttribute("categorySlug", slug);
        return "shop";
    }

    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        Product product = productDao.findBySlug(slug);
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/contact")
    public String contact() { return "contact"; }

    @GetMapping("/journal")
    public String journal() { return "journal"; }

    @GetMapping("/journal/{slug}")
    public String journalDetail(@PathVariable String slug, Model model) {
        BlogArticle article = articles().get(slug);
        if (article == null) {
            return "redirect:/journal";
        }
        model.addAttribute("article", article);
        return "journal-detail";
    }

    @GetMapping({"/travel", "/travel-with-pets"})
    public String travel() { return "travel"; }

    @GetMapping("/adoption")
    public String adoption() { return "adoption"; }

    @GetMapping("/policies")
    public String policies() { return "policies"; }

    private Map<String, BlogArticle> articles() {
        return Map.of(
                "why-dogs-teach-us-to-slow-down",
                new BlogArticle(
                        "Lifestyle",
                        "Why Dogs Teach Us to Slow Down",
                        "Discover how dogs help us live more mindfully, appreciate everyday moments, and slow down in a fast-paced world.",
                        Arrays.asList(
                                "In a world obsessed with productivity, deadlines, and constant notifications, dogs remind us of something we've forgotten: how to simply be present.",
                                "They don't worry about tomorrow's meetings. They don't scroll endlessly before bed. They don't rush through sunsets or morning walks. Dogs experience life exactly as it happens.",
                                "Perhaps that's why sharing life with a dog feels so special.",
                                "Living in the Moment",
                                "Watch a dog during a walk. Every smell is interesting. Every breeze deserves attention. Every new route feels like an adventure.",
                                "While humans often focus on the destination, dogs teach us to appreciate the journey.",
                                "Research has shown that spending time with pets can reduce stress and improve emotional well-being. But beyond science, there's something deeper happening. Dogs encourage us to slow down and reconnect with the world around us.",
                                "The Beauty of Everyday Rituals",
                                "Pet parents quickly discover that some of life's most meaningful moments are surprisingly simple: the morning stretch, the excited greeting at the door, and the evening walk after a long day.",
                                "These small rituals create structure, comfort, and connection. In many ways, dogs help us rediscover joy in ordinary moments.",
                                "A Reminder to Be Present",
                                "Modern life often pulls our attention in countless directions. Dogs pull it back.",
                                "They remind us that a walk can be just a walk. A cuddle can be enough. A quiet afternoon can be time well spent.",
                                "Perhaps that's one of the greatest gifts they give us. Not more excitement. Not more productivity. Just more presence."
                        )
                ),
                "the-story-of-indias-indie-dogs",
                new BlogArticle(
                        "Stories",
                        "The Story of India's Indie Dogs",
                        "Learn about India's beloved indie dogs, their history, unique qualities, and why they make incredible companions.",
                        Arrays.asList(
                                "Long before designer breeds became popular, India's streets, villages, and communities were home to a remarkable dog: the Indian Pariah Dog, often lovingly called an Indie.",
                                "Today, indie dogs are gaining recognition for their intelligence, resilience, and loving nature.",
                                "A Breed Shaped by Nature",
                                "Unlike many modern breeds that were selectively bred for specific traits, indie dogs evolved naturally over thousands of years.",
                                "Their characteristics were shaped by climate, environment, and survival.",
                                "The result is a healthy, adaptable, and incredibly intelligent companion.",
                                "Why Indie Dogs Are Special",
                                "Indies are known for strong immunity, high intelligence, excellent adaptability, loyalty to their families, and low maintenance grooming needs.",
                                "They thrive in Indian weather conditions and often enjoy better overall health compared to many imported breeds.",
                                "Breaking Common Myths",
                                "One common misconception is that indie dogs are less trainable. The reality is often the opposite.",
                                "Many indie dogs learn quickly, understand routines exceptionally well, and form strong bonds with their families.",
                                "Choosing Adoption",
                                "Thousands of wonderful indie dogs in shelters are waiting for homes. Adoption not only changes a dog's life, it changes yours.",
                                "Every adopted dog becomes part of a story built on trust, patience, and second chances. And often, those stories become the most meaningful ones."
                        )
                ),
                "why-every-dog-deserves-a-vacation",
                new BlogArticle(
                        "Travel",
                        "Why Every Dog Deserves a Vacation",
                        "Discover the benefits of travelling with your dog and why pet-friendly adventures create unforgettable memories.",
                        Arrays.asList(
                                "Family vacations are meant to be shared. And for many pet parents, dogs are family.",
                                "Across India, more people are choosing pet-friendly stays, road trips, and outdoor adventures that include their four-legged companions.",
                                "New Places, New Experiences",
                                "Dogs are naturally curious. New environments provide mental stimulation and enrichment.",
                                "Different scents, different sounds, and different routes to explore can be an exciting experience that keeps dogs engaged and happy.",
                                "Strengthening the Bond",
                                "Travel often means spending more uninterrupted time together. Whether it's a mountain getaway or a beachside stay, shared experiences create stronger bonds between pets and their humans.",
                                "The Rise of Pet-Friendly Travel",
                                "India's travel industry is slowly becoming more welcoming to pet parents. More cafes, resorts, and homestays now offer pet-friendly options.",
                                "This shift reflects a larger cultural change: pets are no longer seen as companions on the side. They are family members.",
                                "Making Memories Together",
                                "Years from now, you may not remember every hotel room or highway stop. But you'll remember your dog watching the sunrise beside you.",
                                "You'll remember the road trips. The muddy paws. The adventures. Because the best vacations are often the ones shared together."
                        )
                )
        );
    }

    public record BlogArticle(String category, String title, String metaDescription, List<String> paragraphs) {}
}
