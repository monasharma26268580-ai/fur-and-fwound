package com.furandfound.app.model;

import java.math.BigDecimal;

public class Product {
    private Integer id;
    private Integer categoryId;
    private String sku;
    private String name;
    private String slug;
    private String description;
    private String highlights;
    private String careInfo;
    private String idealUse;
    private String seoDescription;
    private BigDecimal price;
    private int stockQuantity;
    private boolean featured;
    private boolean visible;
    private String imageUrl;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }
    public String getCareInfo() { return careInfo; }
    public void setCareInfo(String careInfo) { this.careInfo = careInfo; }
    public String getIdealUse() { return idealUse; }
    public void setIdealUse(String idealUse) { this.idealUse = idealUse; }
    public String getSeoDescription() { return seoDescription; }
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
