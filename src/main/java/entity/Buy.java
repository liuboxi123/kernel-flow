package entity;

public class Buy {
    private long userId;
    private long productId;
    private String type;
    
    public Buy() {
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getProductId() {
        return productId;
    }
    
    public void setProductId(long productId) {
        this.productId = productId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
