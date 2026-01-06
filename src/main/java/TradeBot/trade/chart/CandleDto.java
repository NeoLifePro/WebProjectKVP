package TradeBot.trade.chart;// package TradeBot.Market;

public class CandleDto {
    private long time;      // seconds
    private double open;
    private double high;
    private double low;
    private double close;

    public CandleDto(long time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public long getTime() { return time; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
}
