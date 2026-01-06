package TradeBot.client.crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;

public class Indicators {


    public double RSI(Vector<Double> prices) {
        int period = 32; // как у тебя и было
        if (prices == null || prices.size() < period + 1) {
            return 0.0;
        }
        double[] closes = toArray(prices);
        double[] rsiArr = rsi(closes, period);
        double last = lastValid(rsiArr);
        return Double.isNaN(last) ? 0.0 : last;
    }



    private static double[] toArray(Vector<Double> v) {
        double[] arr = new double[v.size()];
        for (int i = 0; i < v.size(); i++) {
            arr[i] = v.get(i);
        }
        return arr;
    }

    private static double lastValid(double[] arr) {
        for (int i = arr.length - 1; i >= 0; i--) {
            if (!Double.isNaN(arr[i])) {
                return arr[i];
            }
        }
        return Double.NaN;
    }

    // ===================== 1. SMA =====================

    public static double[] sma(double[] values, int period) {
        int n = values.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;


        int startIdx = 0;
        while (startIdx < n && Double.isNaN(values[startIdx])) {
            startIdx++;
        }


        if (startIdx == n) {
            return result;
        }


        int firstSmaIdx = startIdx + period - 1;
        if (firstSmaIdx >= n) {
            return result;
        }


        double sum = 0.0;
        for (int i = startIdx; i <= firstSmaIdx; i++) {
            sum += values[i];
        }
        result[firstSmaIdx] = sum / period;


        for (int i = firstSmaIdx + 1; i < n; i++) {
            sum += values[i] - values[i - period];
            result[i] = sum / period;
        }

        return result;
    }


    // ===================== 2. EMA  =====================

    public static double[] ema(double[] values, int period) {
        int n = values.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;


        int startIdx = 0;
        while (startIdx < n && Double.isNaN(values[startIdx])) {
            startIdx++;
        }


        if (startIdx == n) {
            return result;
        }


        int firstEmaIdx = startIdx + period - 1;
        if (firstEmaIdx >= n) {
            return result;
        }


        double sum = 0.0;
        for (int i = startIdx; i <= firstEmaIdx; i++) {
            sum += values[i];
        }
        double prevEma = sum / period;
        double alpha = 2.0 / (period + 1.0);

        result[firstEmaIdx] = prevEma;


        for (int i = firstEmaIdx + 1; i < n; i++) {
            double v = values[i];
            if (Double.isNaN(v)) {

                result[i] = Double.NaN;
            } else {
                prevEma = alpha * v + (1.0 - alpha) * prevEma;
                result[i] = prevEma;
            }
        }

        return result;
    }


    // ===================== 3. WMA  =====================

    public static double[] wma(double[] values, int period) {
        int n = values.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        int weightSum = period * (period + 1) / 2;

        for (int i = period - 1; i < n; i++) {
            double acc = 0.0;
            int weight = 1;
            for (int j = i - period + 1; j <= i; j++) {
                acc += values[j] * weight;
                weight++;
            }
            result[i] = acc / weightSum;
        }
        return result;
    }

    // ===================== 4. ROC – Rate of Change =====================

    public static double[] roc(double[] values, int period) {
        int n = values.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        for (int i = period; i < n; i++) {
            double prev = values[i - period];
            if (prev == 0.0) {
                result[i] = Double.NaN;
            } else {
                result[i] = (values[i] - prev) / prev * 100.0;
            }
        }
        return result;
    }

    // ===================== 5. RSI =====================

    public static double[] rsi(double[] closes, int period) {
        int n = closes.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n <= period) return result;

        double gain = 0.0;
        double loss = 0.0;


        for (int i = 1; i <= period; i++) {
            double change = closes[i] - closes[i - 1];
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0) {
            result[period] = 100.0;
        } else {
            double rs = avgGain / avgLoss;
            result[period] = 100.0 - 100.0 / (1.0 + rs);
        }

        for (int i = period + 1; i < n; i++) {
            double change = closes[i] - closes[i - 1];
            double currentGain = 0.0;
            double currentLoss = 0.0;

            if (change > 0) {
                currentGain = change;
            } else {
                currentLoss = -change;
            }

            avgGain = (avgGain * (period - 1) + currentGain) / period;
            avgLoss = (avgLoss * (period - 1) + currentLoss) / period;

            if (avgLoss == 0) {
                result[i] = 100.0;
            } else {
                double rs = avgGain / avgLoss;
                result[i] = 100.0 - 100.0 / (1.0 + rs);
            }
        }

        return result;
    }

    // ===================== 6. MACD =====================

    public static class MacdResult {
        public final double[] macd;
        public final double[] signal;
        public final double[] histogram;

        public MacdResult(double[] macd, double[] signal, double[] histogram) {
            this.macd = macd;
            this.signal = signal;
            this.histogram = histogram;
        }
    }

    public static MacdResult macd(double[] closes,
                                  int fastPeriod,
                                  int slowPeriod,
                                  int signalPeriod) {
        int n = closes.length;
        double[] fast = ema(closes, fastPeriod);
        double[] slow = ema(closes, slowPeriod);
        double[] macdLine = new double[n];
        Arrays.fill(macdLine, Double.NaN);

        for (int i = 0; i < n; i++) {
            if (Double.isNaN(fast[i]) || Double.isNaN(slow[i])) continue;
            macdLine[i] = fast[i] - slow[i];
        }

        double[] signal = ema(macdLine, signalPeriod);
        double[] hist = new double[n];
        Arrays.fill(hist, Double.NaN);

        for (int i = 0; i < n; i++) {
            if (Double.isNaN(macdLine[i]) || Double.isNaN(signal[i])) continue;
            hist[i] = macdLine[i] - signal[i];
        }

        return new MacdResult(macdLine, signal, hist);
    }

    // ===================== 7. Bollinger Bands =====================

    public static class BollingerBands {
        public final double[] middle;
        public final double[] upper;
        public final double[] lower;

        public BollingerBands(double[] middle, double[] upper, double[] lower) {
            this.middle = middle;
            this.upper = upper;
            this.lower = lower;
        }
    }

    public static BollingerBands bollinger(double[] closes, int period, double k) {
        int n = closes.length;
        double[] middle = sma(closes, period);
        double[] upper = new double[n];
        double[] lower = new double[n];
        Arrays.fill(upper, Double.NaN);
        Arrays.fill(lower, Double.NaN);

        if (period <= 0 || n == 0) {
            return new BollingerBands(middle, upper, lower);
        }

        for (int i = period - 1; i < n; i++) {
            if (Double.isNaN(middle[i])) continue;
            double mean = middle[i];
            double sumSq = 0.0;
            int start = i - period + 1;

            for (int j = start; j <= i; j++) {
                double diff = closes[j] - mean;
                sumSq += diff * diff;
            }

            double std = Math.sqrt(sumSq / period);
            upper[i] = mean + k * std;
            lower[i] = mean - k * std;
        }

        return new BollingerBands(middle, upper, lower);
    }

    // ===================== 8. Stochastic %K и %D =====================

    public static class StochasticResult {
        public final double[] k;
        public final double[] d;

        public StochasticResult(double[] k, double[] d) {
            this.k = k;
            this.d = d;
        }
    }

    public static StochasticResult stochastic(double[] highs,
                                              double[] lows,
                                              double[] closes,
                                              int kPeriod,
                                              int dPeriod) {
        int n = closes.length;
        double[] k = new double[n];
        Arrays.fill(k, Double.NaN);

        if (kPeriod <= 0 || n == 0) {
            return new StochasticResult(k, sma(k, dPeriod));
        }

        for (int i = kPeriod - 1; i < n; i++) {
            double highest = highs[i];
            double lowest = lows[i];
            int start = i - kPeriod + 1;

            for (int j = start; j <= i; j++) {
                if (highs[j] > highest) highest = highs[j];
                if (lows[j] < lowest) lowest = lows[j];
            }

            double range = highest - lowest;
            if (range == 0.0) {
                k[i] = 0.0;
            } else {
                k[i] = (closes[i] - lowest) / range * 100.0;
            }
        }

        double[] d = sma(k, dPeriod);
        return new StochasticResult(k, d);
    }

    // ===================== 9. ATR – Average True Range =====================

    public static double[] atr(double[] highs,
                               double[] lows,
                               double[] closes,
                               int period) {
        int n = closes.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        double[] tr = new double[n];
        if (n > 0) {
            tr[0] = highs[0] - lows[0];
        }

        for (int i = 1; i < n; i++) {
            double highLow = highs[i] - lows[i];
            double highClose = Math.abs(highs[i] - closes[i - 1]);
            double lowClose = Math.abs(lows[i] - closes[i - 1]);
            tr[i] = Math.max(highLow, Math.max(highClose, lowClose));
        }

        if (n <= period) return result;

        double sumTR = 0.0;
        for (int i = 0; i < period; i++) {
            sumTR += tr[i];
        }
        double prevAtr = sumTR / period;
        result[period - 1] = prevAtr;

        for (int i = period; i < n; i++) {
            prevAtr = (prevAtr * (period - 1) + tr[i]) / period;
            result[i] = prevAtr;
        }

        return result;
    }

    // ===================== 10. CCI =====================

    public static double[] cci(double[] highs,
                               double[] lows,
                               double[] closes,
                               int period) {
        int n = closes.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        double[] tp = new double[n]; // typical price
        for (int i = 0; i < n; i++) {
            tp[i] = (highs[i] + lows[i] + closes[i]) / 3.0;
        }

        double[] tpSma = sma(tp, period);

        for (int i = period - 1; i < n; i++) {
            if (Double.isNaN(tpSma[i])) continue;

            double meanDev = 0.0;
            int start = i - period + 1;

            for (int j = start; j <= i; j++) {
                meanDev += Math.abs(tp[j] - tpSma[i]);
            }
            meanDev /= period;

            if (meanDev == 0.0) continue;

            result[i] = (tp[i] - tpSma[i]) / (0.015 * meanDev);
        }

        return result;
    }

    // ===================== 11. OBV – On Balance Volume =====================

    public static double[] obv(double[] closes, double[] volumes) {
        int n = closes.length;
        double[] result = new double[n];
        if (n == 0) return result;

        result[0] = volumes[0];

        for (int i = 1; i < n; i++) {
            if (closes[i] > closes[i - 1]) {
                result[i] = result[i - 1] + volumes[i];
            } else if (closes[i] < closes[i - 1]) {
                result[i] = result[i - 1] - volumes[i];
            } else {
                result[i] = result[i - 1];
            }
        }

        return result;
    }

    // ===================== 12. VWAP – Volume Weighted Average Price =====================

    public static double[] vwap(double[] highs,
                                double[] lows,
                                double[] closes,
                                double[] volumes) {
        int n = closes.length;
        double[] result = new double[n];

        double cumPV = 0.0;
        double cumVol = 0.0;

        for (int i = 0; i < n; i++) {
            double typicalPrice = (highs[i] + lows[i] + closes[i]) / 3.0;
            cumPV += typicalPrice * volumes[i];
            cumVol += volumes[i];

            if (cumVol == 0.0) {
                result[i] = Double.NaN;
            } else {
                result[i] = cumPV / cumVol;
            }
        }

        return result;
    }

    // ===================== 13. MFI – Money Flow Index =====================

    public static double[] mfi(double[] highs,
                               double[] lows,
                               double[] closes,
                               double[] volumes,
                               int period) {
        int n = closes.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        double[] tp = new double[n]; // typical price
        for (int i = 0; i < n; i++) {
            tp[i] = (highs[i] + lows[i] + closes[i]) / 3.0;
        }

        double[] posFlow = new double[n];
        double[] negFlow = new double[n];

        for (int i = 1; i < n; i++) {
            double rawFlow = tp[i] * volumes[i];
            if (tp[i] > tp[i - 1]) {
                posFlow[i] = rawFlow;
                negFlow[i] = 0.0;
            } else if (tp[i] < tp[i - 1]) {
                posFlow[i] = 0.0;
                negFlow[i] = rawFlow;
            } else {
                posFlow[i] = 0.0;
                negFlow[i] = 0.0;
            }
        }

        if (n <= period) return result;

        for (int i = period; i < n; i++) {
            double posSum = 0.0;
            double negSum = 0.0;
            int start = i - period + 1;

            for (int j = start; j <= i; j++) {
                posSum += posFlow[j];
                negSum += negFlow[j];
            }

            if (negSum == 0.0) {
                result[i] = 100.0;
            } else {
                double mfr = posSum / negSum;
                result[i] = 100.0 - 100.0 / (1.0 + mfr);
            }
        }

        return result;
    }

    // ===================== 14. Ichimoku – Tenkan и Kijun =====================

    public static class IchimokuResult {
        public final double[] tenkan;
        public final double[] kijun;

        public IchimokuResult(double[] tenkan, double[] kijun) {
            this.tenkan = tenkan;
            this.kijun = kijun;
        }
    }

    public static IchimokuResult ichimoku(double[] highs,
                                          double[] lows,
                                          int tenkanPeriod,
                                          int kijunPeriod) {
        int n = highs.length;
        double[] tenkan = new double[n];
        double[] kijun = new double[n];
        Arrays.fill(tenkan, Double.NaN);
        Arrays.fill(kijun, Double.NaN);

        // Tenkan-sen
        if (tenkanPeriod > 0) {
            for (int i = tenkanPeriod - 1; i < n; i++) {
                double highest = highs[i];
                double lowest = lows[i];
                int start = i - tenkanPeriod + 1;

                for (int j = start; j <= i; j++) {
                    if (highs[j] > highest) highest = highs[j];
                    if (lows[j] < lowest) lowest = lows[j];
                }

                tenkan[i] = (highest + lowest) / 2.0;
            }
        }

        // Kijun-sen
        if (kijunPeriod > 0) {
            for (int i = kijunPeriod - 1; i < n; i++) {
                double highest = highs[i];
                double lowest = lows[i];
                int start = i - kijunPeriod + 1;

                for (int j = start; j <= i; j++) {
                    if (highs[j] > highest) highest = highs[j];
                    if (lows[j] < lowest) lowest = lows[j];
                }

                kijun[i] = (highest + lowest) / 2.0;
            }
        }

        return new IchimokuResult(tenkan, kijun);
    }

    // ===================== 15. Williams %R =====================

    public static double[] williamsR(double[] highs,
                                     double[] lows,
                                     double[] closes,
                                     int period) {
        int n = closes.length;
        double[] result = new double[n];
        Arrays.fill(result, Double.NaN);

        if (period <= 0 || n == 0) return result;

        for (int i = period - 1; i < n; i++) {
            double highest = highs[i];
            double lowest = lows[i];
            int start = i - period + 1;

            for (int j = start; j <= i; j++) {
                if (highs[j] > highest) highest = highs[j];
                if (lows[j] < lowest) lowest = lows[j];
            }

            double range = highest - lowest;
            if (range == 0.0) {
                result[i] = 0.0;
            } else {
                result[i] = -100.0 * (highest - closes[i]) / range;
            }
        }

        return result;
    }

}
