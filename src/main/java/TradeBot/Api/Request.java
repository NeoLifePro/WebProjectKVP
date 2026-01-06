package TradeBot.Api;

import TradeBot.client.order.Account;
public class Request {
    // api
    private String api_key;
    private String api_secret;

    // order
    private String symbol;
    private String price;
    private String side;
    private String type;
    private String qty;
    private long orderId;
    public Request() {}

    public String getApi_key() { return api_key; }
    public void setApi_key(String api_key) { this.api_key = api_key; }

    public String getApi_secret() { return api_secret; }
    public void setApi_secret(String api_secret) { this.api_secret = api_secret; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}
