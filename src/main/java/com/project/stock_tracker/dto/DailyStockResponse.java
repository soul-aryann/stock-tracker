package com.project.stock_tracker.dto;

public class DailyStockResponse {
    private String date;
    private double open;    
    private double close;
    private double high;
    private double low;
    private long volume;

    public DailyStockResponse(String date, double open, double high, double low, double close, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getDate() { return date; }
    public double getOpen() { return open; }
    public double getClose() { return close; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public long getVolume() { return volume; }
}
