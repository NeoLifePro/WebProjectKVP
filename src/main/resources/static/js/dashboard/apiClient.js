
(function () {

    // ===== Atgriež Binance API atslēgas no globālās funkcijas  =====
    function getKeys() {
        if (typeof window.getBinanceApiKeys === "function") {
            return window.getBinanceApiKeys();
        }
        return { apiKey: "", secretKey: "" };
    }

    // ===== Droši nolasa atbildes body kā tekstu (pat ja rodas kļūda) =====
    async function readBodySafe(res) {
        try { return await res.text(); } catch { return ""; }
    }

    // ===== Pārbauda, vai atbildes teksts izskatās pēc HTML (piem., login lapa / redirect) =====
    function looksLikeHtml(text) {
        const t = (text || "").trim().toLowerCase();
        return t.startsWith("<!doctype") || t.startsWith("<html") || t.includes("<head") || t.includes("<body");
    }

    // ===== Universāls fetch JSON: pievieno headerus + dod labu diagnostiku kļūdām =====
    window.apiFetchJson = async function (url, options = {}) {
        const { apiKey, secretKey } = getKeys();

        const headers = new Headers(options.headers || {});
        headers.set("Accept", "application/json");
        headers.set("X-API-KEY", apiKey || "");
        headers.set("X-SECRET-KEY", secretKey || "");

        const res = await fetch(url, {
            ...options,
            headers,
            credentials: "same-origin",
        });

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        const text = await readBodySafe(res);

        // Ja HTTP statuss nav OK — metam kļūdu ar īsu atbildes fragmentu
        if (!res.ok) {
            const msg = text ? text.slice(0, 180) : res.statusText;
            throw new Error(`HTTP ${res.status} (${url}) — ${msg}`);
        }

        // Ja saņēmām HTML nevis JSON — visticamāk redirect uz login vai nepareizs endpoints
        if (ct.includes("text/html") || looksLikeHtml(text)) {
            throw new Error(`Got HTML instead of JSON (${url}). Probably redirect to login or wrong endpoint.`);
        }

        // Mēģinām parsēt JSON
        try {
            return JSON.parse(text);
        } catch (e) {
            throw new Error(`Invalid JSON (${url}). Body: ${text.slice(0, 180)}`);
        }
    };

    // ===== Mēģina vairākus endpointus pēc kārtas, līdz kāds nostrādā =====
    window.apiFetchJsonTry = async function (urls, options = {}) {
        let lastErr = null;
        for (const u of urls) {
            try {
                return await window.apiFetchJson(u, options);
            } catch (e) {
                lastErr = e;
            }
        }
        throw lastErr || new Error("All endpoints failed");
    };

})();
