
document.addEventListener("DOMContentLoaded", () => {
    const elSuccess = document.getElementById("statSuccess");
    const elFailed  = document.getElementById("statFailed");
    const elTotal   = document.getElementById("statTotal");
    const elPercent = document.getElementById("statPercent");
    const donut     = document.getElementById("tradeDonut");

    // Ja nav kāda elementa — nav ko rēķināt/attēlot
    if (!elSuccess || !elFailed || !elTotal || !elPercent || !donut) return;

    const ENDPOINTS = ["/api/trades/", "/api/trades"];
    const PAGE_SIZE = 50;
    const MAX_PAGES = 200;

    // ===== Ierobežo skaitli intervālā [a; b] =====
    function clamp(n, a, b){ return Math.max(a, Math.min(b, n)); }

    // ===== Nosaka, vai statusu uzskatām par “veiksmīgu” =====
    function isSuccessStatus(text){
        const t = String(text || "").trim().toUpperCase();
        return t === "SUCCESS" || t === "SUCCESSFUL" || t === "FILLED" || t === "OK" || t === "DONE";
    }

    // ===== Nosaka, vai statusu uzskatām par “neveiksmīgu” =====
    function isFailedStatus(text){
        const t = String(text || "").trim().toUpperCase();
        return t === "FAILED" || t === "FAIL" || t === "ERROR" || t === "REJECTED" || t === "CANCELED" || t === "CANCELLED";
    }

    // ===== Uzstāda donut grafika procentu (CSS custom property) un tekstu =====
    function setDonutPercent(percent){
        const p = clamp(Number(percent) || 0, 0, 100);
        donut.style.setProperty("--p", String(p * 3.6));
        elPercent.textContent = `${p.toFixed(2)}%`;
    }

    // =====  success/failed/total un aprēķina failed% =====
    function render(success, failed){
        const total = success + failed;
        elSuccess.textContent = String(success);
        elFailed.textContent  = String(failed);
        elTotal.textContent   = String(total);

        const percent = total > 0 ? (failed / total) * 100 : 0;
        setDonutPercent(percent);
    }

    // ===== Ielādē vienu trades lapu no backend (page+size) =====
    async function fetchPage(page){
        const urls = ENDPOINTS.map(base => `${base}?page=${page}&size=${PAGE_SIZE}`);
        return await window.apiFetchJsonTry(urls, { method: "GET" });
    }

    // ===== Normalizē atbildi uz { items, totalPages, page } neatkarīgi no formāta =====
    function extractContent(data){
        if (Array.isArray(data)) return { items: data, totalPages: 1, page: 0 };
        const items = data?.content ?? data?.items ?? data?.data ?? [];
        const totalPages = (typeof data?.totalPages === "number") ? data.totalPages : 1;
        const page = (typeof data?.number === "number") ? data.number : 0;
        return { items, totalPages, page };
    }

    // ===== nolasa visas lapas un saskaita success/failed =====
    async function loadAllTradeStats(){
        if (!window.apiFetchJsonTry) {
            console.error("apiClient.js not loaded (apiFetchJsonTry missing)");
            return;
        }

        elPercent.textContent = "...";

        let success = 0;
        let failed = 0;

        // 1) Pirmā lapa, lai uzzinātu totalPages
        const first = await fetchPage(0);
        const firstParsed = extractContent(first);

        if (!Array.isArray(firstParsed.items)) throw new Error("Trades response is not a list");

        // Saskaita statusus pirmajā lapā
        for (const t of firstParsed.items) {
            const st = t.status ?? t.state ?? t.result ?? "";
            if (isSuccessStatus(st)) success++;
            else if (isFailedStatus(st)) failed++;
        }

        // 2) Pārējās lapas (ar MAX_PAGES limitu, lai nesalauztu UI)
        let totalPages = firstParsed.totalPages || 1;
        totalPages = clamp(totalPages, 1, MAX_PAGES);

        for (let p = 1; p < totalPages; p++) {
            const pageData = await fetchPage(p);
            const parsed = extractContent(pageData);

            if (!Array.isArray(parsed.items)) continue;

            for (const t of parsed.items) {
                const st = t.status ?? t.state ?? t.result ?? "";
                if (isSuccessStatus(st)) success++;
                else if (isFailedStatus(st)) failed++;
            }
        }

        render(success, failed);
    }

    // ===== Palaiž statistikas ielādi un apstrādā kļūdas =====
    loadAllTradeStats().catch(err => {
        console.error(err);
        elPercent.textContent = "—";
    });
});
