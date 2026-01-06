package TradeBot.client;

import TradeBot.client.crypto.Indicators;
import TradeBot.client.order.*;

import java.util.Map;
import java.util.Vector;

public class Client {
    String apiKey;
    String secretKey;
    Indicators indicators;
    createOrder cr;
    GetPriceHistory GetPrice;
    Account account;
    GetVolumeHistory GetVolume;
    OpenOrder openOrder;
    public Client(String apiKey, String api_secret){
        this.apiKey = apiKey;
        this.secretKey = api_secret;
        cr = new createOrder(this.apiKey, this.secretKey);
        GetPrice = new GetPriceHistory(this.apiKey);
        account = new Account(this.apiKey, this.secretKey);
        openOrder = new OpenOrder(this.apiKey, this.secretKey);
        indicators = new Indicators();
        GetVolume = new GetVolumeHistory(this.apiKey);
    }
    //Account Info
    public Map<String, Double> GetAccountBalanceV2() {
        return account.getAccountBalanceV2();
    }

    // free un reserved
    public Map<String, Account.AssetBalance> GetAccountBalances(String... assets) {
        return account.getBalances(assets);
    }

    //Show Price History
    public double GetCryptoPrice(String CryptoName) {
        return GetPrice.GetCryptoPrice(CryptoName);
    }
    public Vector<Double> GetCryptoPrice_1H(String CryptoName){
        return GetPrice.GetCryptoPrice_1H(CryptoName);
    }
    public Vector<Double> GetCryptoPrice_5Min(String CryptoName){
        return GetPrice.GetCryptoPrice_5Min(CryptoName);
    }
    public Vector<Double> GetCryptoPrice_30Min(String CryptoName) {
        return GetPrice.GetCryptoPrice_30Min(CryptoName);
    }

    public Vector<Double> GetCryptoPrice_15Min(String CryptoName){
        return GetPrice.GetCryptoPrice_15Min(CryptoName);
    }
    //Show Volume History
    public double GetCryptoVolume(String Crypto){
        return GetVolume.GetCryptoVolume(Crypto);
    }
    public Vector<Double> GetCryptoVolume_15Min(String CryptoName){
        return GetVolume.GetCryptoVolume_15Min(CryptoName);
    }

    public Vector<Double> GetCryptoVolume_30Min(String CryptoName){
        return GetVolume.GetCryptoVolume_30Min(CryptoName);
    }
    //Create Orders
    public int createLimitOrder(String cryptoName, String side, String type , String qty, String price){
        return cr.createLimitOrder(cryptoName, side, type, qty, price);
    }

    public int createOrderMarket(String cryptoName, String side, String type, String qty){
        return cr.createOrderMarket(cryptoName, side, type, qty);
    }
    public int createOcoOrder(String cryptoName, String side, String qty,String price, String stopPrice, String stopLimitPrice){
        return cr.createOcoOrder(cryptoName, side, qty, price, stopPrice, stopLimitPrice);
    }

    //open orders
    public String getOpenOrders(String symbol) {
        try {
            return openOrder.getOpenOrders(symbol);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    //cancel

    public int cancelOrder(String symbol, long orderId) {
        try {
            return openOrder.cancelOrder(symbol, orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int cancelAllOrders(String symbol) {
        try {
            return openOrder.cancelAllOpenOrders(symbol);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
