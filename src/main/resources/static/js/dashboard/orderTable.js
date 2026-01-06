
document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.querySelector("#ordersTable tbody");
    const pageInfo = document.getElementById("ordersPageInfo");
    const btnPrev = document.getElementById("ordersPrev");
    const btnNext = document.getElementById("ordersNext");

    const ENDPOINTS = ["/api/trades/", "/api/trades"];

    let currentPage = 0;
    const pageSize = 8;
    let totalPages = 1;

    // ===== HTML escape, lai droši ieliktu tekstu tabulā =====
    function esc(v) {
        return String(v ?? "").replace(/[&<>"']/g, m => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#039;"
        }[m]));
    }

    // ===== Parāda kļūdu tabulā un atslēdz lapošanas pogas =====
    function showError(err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="6">Error loading trades — ${esc(err.message)}</td></tr>`;
        pageInfo.textContent = `- / -`;
        btnPrev.disabled = true;
        btnNext.disabled = true;
    }

    // ===== Ielādē trades sarakstu konkrētai lapai =====
    async function loadTrades(page) {
        const urls = ENDPOINTS.map(base => `${base}?page=${page}&size=${pageSize}`);

        try {
            const data = await window.apiFetchJsonTry(urls, { method: "GET" });

            // Atbalsta vairākus iespējamos formātus: page.content, items, vai uzreiz array
            const content = data?.content ?? data?.items ?? data ?? [];
            if (!Array.isArray(content)) {
                throw new Error("Trades API: response is not an array/page");
            }

            tbody.innerHTML = "";

            // Uzzīmē katru trade kā rindu
            content.forEach(trade => {
                const side = (trade.side || "").toUpperCase();
                const status = (trade.status || "").toUpperCase();

                const time = trade.time || trade.createdAt || trade.created_at || "";
                const pair = trade.pair || trade.symbol || "";
                const price = trade.price ?? "";
                const amount = trade.amount ?? "";

                const tr = document.createElement("tr");
                tr.innerHTML = `
          <td>${esc(time)}</td>
          <td>${esc(pair)}</td>
          <td class="${side === "BUY" ? "order-side-buy" : "order-side-sell"}">${esc(side)}</td>
          <td>${esc(price)}</td>
          <td>${esc(amount)}</td>
          <td class="${status === "SUCCESS" ? "order-status-success" : "order-status-failed"}">${esc(status)}</td>
        `;
                tbody.appendChild(tr);
            });

            // Pager info (ja backend neatgriež totalPages/number, tad paliek 1 lapa)
            totalPages = typeof data?.totalPages === "number" ? data.totalPages : 1;
            currentPage = typeof data?.number === "number" ? data.number : page;

            pageInfo.textContent = `${currentPage + 1} / ${totalPages}`;
            btnPrev.disabled = currentPage <= 0;
            btnNext.disabled = currentPage >= totalPages - 1;

        } catch (err) {
            showError(err);
        }
    }

    // ===== iepriekšējā lapa =====
    btnPrev.addEventListener("click", () => {
        if (currentPage > 0) loadTrades(currentPage - 1);
    });

    // ===== nākamā lapa =====
    btnNext.addEventListener("click", () => {
        if (currentPage < totalPages - 1) loadTrades(currentPage + 1);
    });

    // ===== Sākotnējā ielāde =====
    loadTrades(currentPage);
});
