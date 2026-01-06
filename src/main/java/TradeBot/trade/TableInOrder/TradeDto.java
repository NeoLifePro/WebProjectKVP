package TradeBot.trade.TableInOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradeDto {

    private LocalDateTime time;
    private String pair;
    private String side;
    private BigDecimal price;
    private BigDecimal amount;
    private String status;

    public TradeDto(LocalDateTime time,
                    String pair,
                    String side,
                    BigDecimal price,
                    BigDecimal amount,
                    String status) {
        this.time = time;
        this.pair = pair;
        this.side = side;
        this.price = price;
        this.amount = amount;
        this.status = status;
    }


    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public String getPair() { return pair; }
    public void setPair(String pair) { this.pair = pair; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
