document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.querySelector("#assetsTable tbody");
    const pageInfo = document.getElementById("assetsPageInfo");
    const btnPrev = document.getElementById("assetsPrev");
    const btnNext = document.getElementById("assetsNext");

    const API_URL = "/api/balance/balance";
    const SYMBOLS = ["BTC", "ETH", "LTC", "DOGE", "TRX"];

    const priceCache = new Map(); // pair -> { price, ts }
    const PRICE_TTL_MS = 10_000;

    // ===== HTML escape, lai droši ieliktu tekstu tabulā un izvairītos no XSS =====
    function esc(v) {
        return String(v ?? "").replace(/[&<>"']/g, m => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#039;"
        }[m]));
    }

    // ===== Iestata pager kā “1 / 1” un atslēdz pogas (jo šeit nav īstas lapošanas) =====
    function setPagerOnePage() {
        if (pageInfo) pageInfo.textContent = "1 / 1";
        if (btnPrev) btnPrev.disabled = true;
        if (btnNext) btnNext.disabled = true;
    }

    // ===== Parāda kļūdu tabulā un atslēdz pager pogas =====
    function showError(msg) {
        if (tbody) tbody.innerHTML = `<tr><td colspan="4">Error loading assets — ${esc(msg)}</td></tr>`;
        if (pageInfo) pageInfo.textContent = "- / -";
        if (btnPrev) btnPrev.disabled = true;
        if (btnNext) btnNext.disabled = true;
    }

    // ===== Iegūst API atslēgas no cookie (ja eksistē getBinanceApiKeys) =====
    function getKeys() {
        if (typeof window.getBinanceApiKeys === "function") return window.getBinanceApiKeys();
        return { apiKey: "", secretKey: "" };
    }

    // ===== Droši pārvērš uz skaitli, ja nav derīgs — atgriež 0 =====
    function toNum(x) {
        const n = Number(x);
        return Number.isFinite(n) ? n : 0;
    }

    // ===== Formatē USD vērtību ar 2 zīmēm aiz komata =====
    function fmtUsd(x) {
        return toNum(x).toFixed(2);
    }

    // ===== Nosūta POST uz bilances endpointu un atgriež JSON =====
    async function postBalance(symbol, apiKey, secretKey) {
        const res = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify({
                api_key: apiKey,
                api_secret: secretKey,
                symbol
            })
        });

        const text = await res.text();
        if (!res.ok) throw new Error(`HTTP ${res.status}: ${text.slice(0, 180)}`);

        try {
            return JSON.parse(text);
        } catch {
            throw new Error(`Invalid JSON: ${text.slice(0, 180)}`);
        }
    }

    // ===== Iegūst monētas cenu no Binance (ticker/price) ar cache (TTL) =====
    async function fetchPrice(pair) {
        const now = Date.now();
        const cached = priceCache.get(pair);
        if (cached && (now - cached.ts) < PRICE_TTL_MS) return cached.price;

        const url = `https://api.binance.com/api/v3/ticker/price?symbol=${pair}`;
        const res = await fetch(url, { method: "GET" });
        if (!res.ok) throw new Error(`Price HTTP ${res.status} for ${pair}`);

        const data = await res.json();
        const price = Number(data?.price);
        if (!Number.isFinite(price) || price <= 0) throw new Error(`Bad price for ${pair}`);

        priceCache.set(pair, { price, ts: now });
        return price;
    }

    // ===== Atgriež aktīva USD kursu: stable = 1, citādi mēģina USDT, tad USDC =====
    async function getUsdRate(asset) {
        const a = (asset || "").toUpperCase();
        if (!a) return 0;

        if (a === "USDT" || a === "USDC" || a === "BUSD" || a === "TUSD") return 1;

        try {
            return await fetchPrice(`${a}USDT`);
        } catch {
            return await fetchPrice(`${a}USDC`);
        }
    }

    // ===== Pievieno vienu rindu tabulai (coin/free/reserved/usd) =====
    function appendRow(coin, free, reserved, usd) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${esc(coin)}</td>
      <td>${esc(free)}</td>
      <td>${esc(reserved)}</td>
      <td>${esc(usd)}</td>
    `;
        tbody.appendChild(tr);
    }

    // ===== ielādē bilances (USDC + 5 kripto) un aprēķina USD =====
    async function loadAssets() {
        try {
            if (!tbody) return;

            setPagerOnePage();

            const { apiKey, secretKey } = getKeys();
            if (!apiKey || !secretKey) {
                showError("API keys missing. Save API/Secret first.");
                return;
            }

            tbody.innerHTML = "";

            // 1) Paņem fiat (USDC) vienu reizi — symbol var būt jebkurš
            const first = await postBalance("ETH", apiKey, secretKey);
            const fiat = first?.fiat;

            if (fiat) {
                const fiatUsd = fmtUsd(fiat.total);
                appendRow(fiat.asset, fiat.free, fiat.locked, fiatUsd);
            }

            // 2) Paņem 5 kripto un aprēķina USD (total * rate)
            for (const sym of SYMBOLS) {
                const data = await postBalance(sym, apiKey, secretKey);
                const crypto = data?.crypto;
                if (!crypto) continue;

                const rate = await getUsdRate(crypto.asset);
                const usdValue = toNum(crypto.total) * rate;

                appendRow(
                    crypto.asset,
                    crypto.free,
                    crypto.locked,
                    fmtUsd(usdValue)
                );
            }

        } catch (err) {
            console.error(err);
            showError(err.message);
        }
    }

    loadAssets();
});
