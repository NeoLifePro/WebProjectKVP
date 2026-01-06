

(function () {

    // ===== iestata cookie ar noteiktu derīguma termiņu =====
    function setCookie(name, value, days) {
        let expires = "";
        if (days) {
            const date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + encodeURIComponent(value) + expires + "; path=/; SameSite=Lax";
    }

    // ===== nolasa cookie vērtību pēc nosaukuma =====
    function getCookie(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) {
                return decodeURIComponent(c.substring(nameEQ.length, c.length));
            }
        }
        return null;
    }

    // ===== DOM notikumu apstrāde pēc lapas ielādes =====
    document.addEventListener("DOMContentLoaded", function () {
        const apiInput = document.getElementById("apiKey");
        const secretInput = document.getElementById("secretKey");
        const saveBtn = document.getElementById("saveApiKeysBtn");
        const msg = document.getElementById("apiSaveMsg");

        // Ja obligātie elementi nav atrasti — pārtraucam
        if (!apiInput || !secretInput || !saveBtn) return;

        // Nolasa iepriekš saglabātās atslēgas no cookie
        const savedApi = getCookie("binance_api_key");
        const savedSecret = getCookie("binance_secret_key");

        // Ja bija saglabāts — ieliekam laukos
        if (savedApi) apiInput.value = savedApi;
        if (savedSecret) secretInput.value = savedSecret;

        // ===== saglabāt API atslēgas cookie pēc pogas nospiešanas =====
        saveBtn.addEventListener("click", function (e) {
            e.preventDefault();

            // Saglabājam uz 365 dienām
            setCookie("binance_api_key", apiInput.value || "", 365);
            setCookie("binance_secret_key", secretInput.value || "", 365);

            // Parādam īsu paziņojumu, ka saglabāts
            if (msg) {
                msg.style.display = "inline";
                setTimeout(() => {
                    msg.style.display = "none";
                }, 2000);
            }
        });

        // ===== atgriež Binance API atslēgas no cookie =====
        window.getBinanceApiKeys = function () {
            return {
                apiKey: getCookie("binance_api_key") || "",
                secretKey: getCookie("binance_secret_key") || ""
            };
        };
    });

})();
