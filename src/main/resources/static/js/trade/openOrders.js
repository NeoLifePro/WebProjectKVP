

(function() {

    const apiKeyInput    = document.getElementById("apiKey");
    const secretKeyInput = document.getElementById("secretKey");
    const symbolSelect   = document.getElementById("ordersSymbol");
    const refreshBtn     = document.getElementById("refreshNowBtn");

    const tableOpenBody  = document.getElementById("ordersTableBody");
    const tableOtherBody = document.getElementById("otherOrdersTableBody");

    // ===== Palīgfunkcija: formatē timestamp (ms) uz lokālu datuma/laika tekstu =====
    function formatDate(ms) {
        if (!ms) return "";
        const d = new Date(ms);
        return d.toLocaleString();
    }

    // ===== Palīgfunkcija: formatē skaitli ar noteiktu zīmju skaitu aiz komata =====
    function formatNum(v, d = 4) {
        if (v == null || isNaN(v)) return "-";
        return Number(v).toFixed(d);
    }

    // ===== Ielādē orderus no konkrēta endpoint un ieliek tabulā =====
    async function loadOrdersForTable(endpoint, tbody) {
        if (!tbody) return;

        const apiKey    = apiKeyInput ? apiKeyInput.value.trim() : "";
        const secretKey = secretKeyInput ? secretKeyInput.value.trim() : "";
        const symbol    = symbolSelect ? symbolSelect.value.trim() : "";

        // Pieprasījuma payload backendam
        const payload = {
            api_key: apiKey,
            api_secret: secretKey,
            symbol: symbol || null
        };

        try {
            const resp = await fetch(endpoint, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            // Ja ir kļūda — iztīram tabulu un parādam “No data”
            if (!resp.ok) {
                console.error("Orders API error:", endpoint, resp.status);
                renderTable([], tbody);
                return;
            }

            const data = await resp.json();
            renderTable(Array.isArray(data) ? data : [], tbody);

        } catch (e) {
            console.error("Orders fetch error:", endpoint, e);
            renderTable([], tbody);
        }
    }

    // ===== Uzzīmē tabulas rindas no orderu masīva =====
    function renderTable(orders, tbody) {
        tbody.innerHTML = "";

        // Ja nav orderu — parādam vienu rindu “No data.”
        if (!orders.length) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 8;
            td.textContent = "No data.";
            td.style.textAlign = "center";
            td.style.padding = "12px 0";
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        // Izveidojam rindu katram orderim
        orders.forEach(o => {
            const tr = document.createElement("tr");
            const sideClass = o.side === "BUY" ? "side-buy" : "side-sell";
            console.log(o.qty);

            tr.innerHTML = `
                <td>${formatDate(o.time)}</td>
                <td>${o.symbol || ""}</td>
                <td><span class="${sideClass}">${o.side}</span></td>
                <td>${formatNum(o.price, 4)}</td>
                <td>${formatNum(o.qty, 6)}</td>
                <td>${o.status || ""}</td>
                <td>${o.orderId}</td>
                <td>
                    <button class="cancel-btn"
                        data-symbol="${o.symbol}"
                        data-order-id="${o.orderId}">
                        Cancel
                    </button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    // ===== Atceļ orderi (cancel) pēc symbol + orderId =====
    async function cancelOrder(symbol, orderId) {
        const apiKey    = apiKeyInput ? apiKeyInput.value.trim() : "";
        const secretKey = secretKeyInput ? secretKeyInput.value.trim() : "";

        // Bez atslēgām atcelt orderi nevar
        if (!apiKey || !secretKey) {
            alert("Enter your API key and Secret key to cancel orders");
            return;
        }

        const payload = {
            api_key: apiKey,
            api_secret: secretKey,
            symbol: symbol,
            orderId: orderId
        };

        try {
            const resp = await fetch("/api/openOrder/cancel", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            // Ja atcelšana neizdevās — parādam kļūdu
            if (!resp.ok) {
                const text = await resp.text();
                console.error("Cancel error:", resp.status, text);
                alert("Unable to cancel order (see console)");
                return;
            }


            if (typeof refreshAll === "function") {
                refreshAll();
            } else {
                loadOrdersForTable("/api/openOrder/opened", tableOpenBody);
            }

        } catch (e) {
            console.error("Cancel fetch error:", e);
            alert("Error when canceling an order");
        }
    }

    // ===== Globālais klikšķu handlers: ja nospiesta Cancel poga, atceļ orderi =====
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".cancel-btn");
        if (!btn) return;

        const symbol  = btn.getAttribute("data-symbol");
        const orderId = btn.getAttribute("data-order-id");

        cancelOrder(symbol, Number(orderId));
    });

    // ===== Atjauno visas tabulas (šobrīd: tikai Open orders) =====
    function refreshAll() {

        loadOrdersForTable("/api/openOrder/opened", tableOpenBody);
    }

    // ===== Manuāla atjaunošana pēc pogas nospiešanas =====
    if (refreshBtn) {
        refreshBtn.addEventListener("click", refreshAll);
    }

    // ===== Kad mainās simbols — pārlādējam datus =====
    if (symbolSelect) {
        symbolSelect.addEventListener("change", refreshAll);
    }

    // ===== Sākotnējā ielāde + auto refresh ik pēc 3 sekundēm =====
    document.addEventListener("DOMContentLoaded", () => {
        refreshAll();
        setInterval(refreshAll, 3000);
    });

})();
