package entity;

public class Search {
    private String goodName;
    private String goodDesc;
    private Double goodPrice;
    
    public Search() {
    }
    public Search(String goodName, String goodDesc, Double goodPrice) {
        this.goodName = goodName;
        this.goodDesc = goodDesc;
        this.goodPrice = goodPrice;
    }
    
    public String getGoodName() {
        return goodName;
    }
    
    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }
    
    public String getGoodDesc() {
        return goodDesc;
    }
    
    public void setGoodDesc(String goodDesc) {
        this.goodDesc = goodDesc;
    }
    
    public Double getGoodPrice() {
        return goodPrice;
    }
    
    public void setGoodPrice(Double goodPrice) {
        this.goodPrice = goodPrice;
    }
}
