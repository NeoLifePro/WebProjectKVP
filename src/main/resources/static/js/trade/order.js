// === Cenas iegūšana ===
// ===== Iegūst tirgus cenu no backend (/api/price) pēc simbola =====
async function marketPrices(symbol) {
    try {
        const response = await fetch('/api/price/?symbol=' + encodeURIComponent(symbol));

        if (!response.ok) {
            console.error('Price API error:', response.status, await response.text());
            return -1;
        }

        const text = await response.text();
        const val = parseFloat(text);
        if (Number.isNaN(val)) {
            console.error('Price parse error, got:', text);
            return -1;
        }
        return val;
    } catch (e) {
        console.error('Price fetch failed:', e);
        return -1;
    }
}

const symbolSelect = document.getElementById('cryptoName');

document.addEventListener('DOMContentLoaded', () => {
    const boxes = document.querySelectorAll('.order-box');

    // ===== Inicializē katru order-box  =====
    boxes.forEach(setupOrderBox);

    // ===== Notikums: mainās izvēlētais simbols  =====
    if (symbolSelect) {
        symbolSelect.addEventListener('change', async () => {
            const base = symbolSelect.value.toUpperCase();

            for (const box of boxes) {
                const baseLabel = box.querySelector('.base-symbol');
                const btn = box.querySelector('.action-btn');
                const side = box.dataset.side;

                if (baseLabel) baseLabel.textContent = base;
                if (btn) btn.textContent = (side === 'buy' ? 'Buy ' : 'Sell ') + base;

                const priceInput = box.querySelector('.price-input');
                if (priceInput) {
                    const price = await getMarketPrice();
                    if (price > 0) priceInput.value = price.toFixed(2);
                }

                recalcBox(box);
            }
        });

        // Piespiedu “change” notikums, lai uzreiz viss ielādējas pie starta
        symbolSelect.dispatchEvent(new Event('change'));
    }
});

// ===== atgriež aktuālo tirgus cenu (izmanto marketPrices) =====
async function getMarketPrice() {
    let fullSymbol;

    if (typeof getFullSymbol === 'function') {
        fullSymbol = getFullSymbol();
    } else if (symbolSelect) {
        fullSymbol = symbolSelect.value.toUpperCase() + 'USDC';
    } else {
        return 0;
    }

    if (typeof marketPrices === 'function') {
        try {
            const price = await marketPrices(fullSymbol);
            return Number.isFinite(price) ? price : 0;
        } catch (e) {
            console.error('marketPrices() error:', e);
            return 0;
        }
    }

    return 0;
}

// ===== Inicializē vienu order-box: tabus, input notikumus un sākotnējo aprēķinu =====
function setupOrderBox(box) {
    const tabs = box.querySelectorAll('.order-tab');
    const priceInput = box.querySelector('.price-input');
    const amountInput = box.querySelector('.amount-input');
    const totalInput = box.querySelector('.total-input');


    box._state = { lastChanged: 'amount' };

    // ===== Notikums: ordera tipa tabu maiņa (limit/market) =====
    tabs.forEach(btn => {
        btn.addEventListener('click', async () => {
            tabs.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const type = btn.dataset.type;

            // Market režīmā cenu ņemam no tirgus un bloķējam price input
            if (type === 'market') {
                const price = await getMarketPrice();
                if (price > 0) priceInput.value = price.toFixed(2);
                priceInput.readOnly = true;
                priceInput.classList.add('readonly');
            } else {
                // Limit režīmā cenu var rediģēt
                priceInput.readOnly = false;
                priceInput.classList.remove('readonly');
            }

            recalcBox(box);
        });
    });

    // ===== Notikums: price input maiņa =====
    priceInput.addEventListener('input', () => recalcBox(box));

    // ===== Notikums: amount input maiņa =====
    amountInput.addEventListener('input', () => {
        box._state.lastChanged = 'amount';
        recalcBox(box);
    });

    // ===== Notikums: total input maiņa =====
    totalInput.addEventListener('input', () => {
        box._state.lastChanged = 'total';
        recalcBox(box);
    });

    // Sākotnējais aprēķins
    recalcBox(box);
}

// ===== Pārrēķina amount/total laukus pēc price un lastChanged =====
function recalcBox(box) {
    const priceInput = box.querySelector('.price-input');
    const amountInput = box.querySelector('.amount-input');
    const totalInput = box.querySelector('.total-input');
    const estText = box.querySelector('.est-text');

    const lastChanged = box._state.lastChanged;
    const price = parseFloat(priceInput.value) || 0;
    let amount = parseFloat(amountInput.value) || 0;
    let total = parseFloat(totalInput.value) || 0;

    // Ja cena nav derīga — “≈ 0.00”
    if (price <= 0) {
        if (estText) estText.textContent = '≈ 0.00 USDC';
        return;
    }


    if (lastChanged === 'amount') {
        total = price * amount;
        totalInput.value = total.toFixed(2);
    } else {
        amount = total / price;
        amountInput.value = amount.toFixed(4);
    }

    // Atjaunojam aptuveno vērtību
    if (estText) {
        estText.textContent = '≈ ' + (price * amount).toFixed(2) + ' USDC';
    }
}

// ==== ORDERS ====

// ===== atgriež simbolu ordera izveidei =====
function getFullSymbolForOrder() {
    if (typeof getFullSymbol === 'function') {
        return getFullSymbol();
    }

    const select = document.getElementById('cryptoName');
    return select ? select.value.toUpperCase() : null;
}

document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('.order-box .action-btn');

    // ===== Notikums: nospiež Buy/Sell pogu — izveido orderi (limit/market) =====
    buttons.forEach(btn => {
        btn.addEventListener('click', async () => {
            const box = btn.closest('.order-box');
            if (!box) return;

            const side = box.dataset.side;
            const activeTab = box.querySelector('.order-tab.active');
            const type = activeTab ? activeTab.dataset.type : 'limit';

            const price = parseFloat(box.querySelector('.price-input').value);
            const amount = parseFloat(box.querySelector('.amount-input').value);
            const symbol = getFullSymbolForOrder();

            if (!symbol) {
                alert('Unable to determine trading pair');
                return;
            }

            if (!amount || amount <= 0) {
                alert('Please enter a valid amount');
                return;
            }

            // Nolasa atslēgas no globālā helpera
            let apiKey = '';
            let apiSecret = '';

            if (typeof getBinanceApiKeys === 'function') {
                const keys = getBinanceApiKeys();
                apiKey = keys.apiKey || '';
                apiSecret = keys.secretKey || '';
            }

            // Bez atslēgām orderi izveidot nevar
            if (!apiKey || !apiSecret) {
                alert('Please save your Binance API key and Secret key first');
                return;
            }

            // Payload backendam
            const payload = {
                api_key: apiKey,
                api_secret: apiSecret,
                symbol: symbol,
                price: type === 'limit' ? String(price) : null,
                side: side.toUpperCase(),
                type: type.toUpperCase(),
                qty: String(amount)
            };

            // Izvēlamies endpointu pēc ordera tipa
            const url = type === 'limit'
                ? '/api/order/limit'
                : '/api/order/market';

            try {
                const resp = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                // Kļūdu apstrāde
                if (!resp.ok) {
                    const text = await resp.text();
                    alert('Failed to create order: ' + text);
                    return;
                }

                console.log('Order created successfully');
            } catch (e) {
                alert('Network error while creating order');
            }
        });
    });
});
