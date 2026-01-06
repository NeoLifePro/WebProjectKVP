document.addEventListener('DOMContentLoaded', () => {
    const symbolSelect = document.getElementById('cryptoName');
    const chartContainer = document.getElementById('chartContainer');
    const symbolLabel = document.getElementById('chartSymbolLabel');
    const exchangeLabel = document.getElementById('chartExchangeLabel');
    const intervalButtons = document.querySelectorAll('#intervalTabs button');

    // Pārbaudām, vai ir vajadzīgie DOM elementi (select + konteiners)
    if (!symbolSelect || !chartContainer) {
        console.warn('Chart: элемент select#cryptoName или #chartContainer не найден');
        return;
    }

    chartContainer.style.borderStyle = 'none';
    chartContainer.style.opacity = '1';

    // ===== Izveido grafiku ar LightweightCharts un piesaista to konteinerim =====
    const chart = LightweightCharts.createChart(chartContainer, {
        width: chartContainer.clientWidth,
        height: chartContainer.clientHeight || 280,
        layout: {
            background: { color: 'transparent' },
            textColor: '#e5e7eb',
        },
        grid: {
            vertLines: { color: 'rgba(148,163,184,0.2)' },
            horzLines: { color: 'rgba(148,163,184,0.2)' },
        },
        rightPriceScale: {
            borderColor: 'rgba(148,163,184,0.4)',
        },
        timeScale: {
            borderColor: 'rgba(148,163,184,0.4)',
            timeVisible: true,
            secondsVisible: false,
        },
        crosshair: {
            mode: LightweightCharts.CrosshairMode.Normal,
        },
    });

    // ===== Pievieno sveču (candlestick) sēriju =====
    const candleSeries = chart.addCandlestickSeries({
        upColor: '#22c55e',
        downColor: '#ef4444',
        wickUpColor: '#22c55e',
        wickDownColor: '#ef4444',
        borderVisible: false,
    });

    // ===== pie loga izmēra maiņas pielāgojam grafika izmērus =====
    window.addEventListener('resize', () => {
        chart.applyOptions({
            width: chartContainer.clientWidth,
            height: chartContainer.clientHeight || 280,
        });
    });

    // ===== pašreizējais intervāls un auto-atjaunošanas timers =====
    let currentInterval = '1m';
    const REFRESH_MS = 100;
    let refreshTimer = null;

    // ===== atgriež bāzes simbolu no select (piem., ETH) =====
    function getBaseSymbol() {
        return (symbolSelect.value || 'ETH').toUpperCase();
    }

    // ===== izveido Binance pāra simbolu (piem., ETHUSDT) =====
    function getBinanceSymbol() {
        return getBaseSymbol() + 'USDT';
    }

    // ===== atjauno labelus virs grafika (pāris + intervāls) =====
    function updateLabels() {
        const base = getBaseSymbol();
        symbolLabel.textContent = `Chart — ${base}/USDT`;
        exchangeLabel.textContent = `${currentInterval.toUpperCase()} • BINANCE`;
    }

    // ===== Ielādē sākotnējo sveču vēsturi (limit=200) no Binance =====
    async function loadInitialKlines() {
        const symbol = getBinanceSymbol();
        const limit = 200;
        const url = `https://api.binance.com/api/v3/klines?symbol=${symbol}&interval=${currentInterval}&limit=${limit}`;

        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();

            const candles = data.map(k => ({
                time: k[0] / 1000,
                open: parseFloat(k[1]),
                high: parseFloat(k[2]),
                low: parseFloat(k[3]),
                close: parseFloat(k[4]),
            }));

            candleSeries.setData(candles);
            chart.timeScale().fitContent();
        } catch (e) {
            console.error('Ошибка загрузки истории с Binance:', e);
        }
    }

    // ===== Atjauno pēdējo sveci (limit=1) — reāllaika atjaunošana =====
    async function updateLastKline() {
        const symbol = getBinanceSymbol();
        const url = `https://api.binance.com/api/v3/klines?symbol=${symbol}&interval=${currentInterval}&limit=1`;

        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            if (!data || !data.length) return;

            const k = data[0];
            const bar = {
                time: k[0] / 1000,
                open: parseFloat(k[1]),
                high: parseFloat(k[2]),
                low: parseFloat(k[3]),
                close: parseFloat(k[4]),
            };

            candleSeries.update(bar);
        } catch (e) {
            console.error('Ошибка обновления последней свечи:', e);
        }
    }

    // ===== Palaiž automātisko atjaunošanu ar setInterval =====
    function startAutoUpdate() {
        if (refreshTimer) clearInterval(refreshTimer);
        refreshTimer = setInterval(updateLastKline, REFRESH_MS);
    }

    // ===== Apstādina automātisko atjaunošanu =====
    function stopAutoUpdate() {
        if (refreshTimer) {
            clearInterval(refreshTimer);
            refreshTimer = null;
        }
    }

    // ===== Notikums: monētas maiņa (select) — pārlādē vēsturi un restartē atjaunošanu =====
    symbolSelect.addEventListener('change', async () => {
        stopAutoUpdate();
        updateLabels();
        await loadInitialKlines();
        startAutoUpdate();
    });

    // ===== Notikumi: intervāla pogas — maina intervālu un pārlādē datus =====
    intervalButtons.forEach(btn => {
        btn.addEventListener('click', async () => {
            const newInterval = btn.dataset.interval;
            if (!newInterval || newInterval === currentInterval) return;

            // Aktīvās pogas stils
            intervalButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            currentInterval = newInterval;

            stopAutoUpdate();
            updateLabels();
            await loadInitialKlines();
            startAutoUpdate();
        });
    });

    // ===== Pirmā palaišana: uzliek labelus, ielādē vēsturi un ieslēdz auto-update =====
    (async () => {
        updateLabels();
        await loadInitialKlines();
        startAutoUpdate();
    })();
});
