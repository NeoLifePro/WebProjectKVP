

(function() {
    const cryptoSelect    = document.getElementById("cryptoName");
    const apiKeyInput     = document.getElementById("apiKey");
    const secretKeyInput  = document.getElementById("secretKey");

    const balFiatAvailable   = document.querySelector(".bal-fiat-available");
    const balFiatReserved    = document.querySelector(".bal-fiat-reserved");
    const balFiatTotal       = document.querySelector(".bal-fiat-total");

    const balCryptoAvailable = document.querySelector(".bal-crypto-available");
    const balCryptoReserved  = document.querySelector(".bal-crypto-reserved");
    const balCryptoTotal     = document.querySelector(".bal-crypto-total");

    // Ja nav pamata DOM elementu — nav jēgas turpināt (widget nestrādās)
    if (!cryptoSelect || !apiKeyInput || !secretKeyInput) {
        console.warn("Balance widget: missing DOM elements");
        return;
    }

    // ===== formatē skaitli ar noteiktu zīmju skaitu aiz komata =====
    function formatNumber(value, decimals) {
        if (value == null || isNaN(value)) return "0.00";
        return Number(value).toFixed(decimals);
    }

    // ===== ielādē bilances no servera un atjauno UI =====
    async function loadBalances() {
        const apiKey   = apiKeyInput.value.trim();
        const secretKey = secretKeyInput.value.trim();
        const crypto   = (cryptoSelect.value || "ETH").toUpperCase();

        // Ja atslēgas nav ievadītas, brīdinām konsolē (bet pieprasījumu tomēr mēģinām)
        if (!apiKey || !secretKey) {
            console.warn("API keys are empty, but sending request anyway");
        }

        // Pieprasījuma dati backendam
        const payload = {
            api_key: apiKey,
            api_secret: secretKey,
            symbol: crypto
        };

        try {
            const resp = await fetch("/api/balance/balance", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            // Ja serveris atbild ar kļūdu statusu — izvadām un pārtraucam
            if (!resp.ok) {
                console.error("Balance API error:", resp.status);
                return;
            }

            const data = await resp.json();

            // Gaidām struktūru:
            // { fiat: {asset, free, locked, total}, crypto: {asset, free, locked, total} }

            // ===== Fiat bilance (piem., USDC) =====
            if (data.fiat) {
                const assetFiat = data.fiat.asset || "USDC";
                if (balFiatAvailable) {
                    balFiatAvailable.textContent =
                        `${formatNumber(data.fiat.free, 2)} ${assetFiat}`;
                }
                if (balFiatReserved) {
                    balFiatReserved.textContent =
                        `${formatNumber(data.fiat.locked, 2)} ${assetFiat}`;
                }
                if (balFiatTotal) {
                    balFiatTotal.textContent =
                        `${formatNumber(data.fiat.total, 2)} ${assetFiat}`;
                }
            }

            // ===== Crypto bilance (piem., BTC/ETH) =====
            if (data.crypto) {
                const assetCrypto = data.crypto.asset || crypto;
                if (balCryptoAvailable) {
                    balCryptoAvailable.textContent =
                        `${formatNumber(data.crypto.free, 8)} ${assetCrypto}`;
                }
                if (balCryptoReserved) {
                    balCryptoReserved.textContent =
                        `${formatNumber(data.crypto.locked, 8)} ${assetCrypto}`;
                }
                if (balCryptoTotal) {
                    balCryptoTotal.textContent =
                        `${formatNumber(data.crypto.total, 8)} ${assetCrypto}`;
                }
            }

        } catch (err) {

            console.error("Balance fetch error:", err);
        }
    }

    // ===== kad lietotājs nomaina kripto izvēli, atjaunojam bilanci =====
    cryptoSelect.addEventListener("change", loadBalances);

    // ===== Sākotnējā ielāde + automātiska atjaunošana ik pēc 1 sekundes =====
    document.addEventListener("DOMContentLoaded", () => {
        loadBalances();
        setInterval(loadBalances, 1000);
    });

})();
