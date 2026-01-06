const indicators = [
    { id: "sma14",      label: "SMA (14)",                 path: "/api/ind/sma14" },
    { id: "ema14",      label: "EMA (14)",                 path: "/api/ind/ema14" },
    { id: "wma14",      label: "WMA (14)",                 path: "/api/ind/wma14" },
    { id: "roc10",      label: "ROC (10)",                 path: "/api/ind/roc10" },
    { id: "rsi14",      label: "RSI (14)",                 path: "/api/ind/rsi14" },
    { id: "macdLine",   label: "MACD Line",                path: "/api/ind/macdLine" },
    { id: "macdSignal", label: "MACD Signal",              path: "/api/ind/macdSignal" },
    { id: "macdHist",   label: "MACD Histogram",           path: "/api/ind/macdHist" },
    { id: "bbMiddle",   label: "BB Middle",                path: "/api/ind/bbMiddle" },
    { id: "bbUpper",    label: "BB Upper",                 path: "/api/ind/bbUpper" },
    { id: "bbLower",    label: "BB Lower",                 path: "/api/ind/bbLower" }
];

const tbody = document.getElementById("indicatorBody");
const loadBtn = document.getElementById("loadBtn");
const statusText = document.getElementById("statusText");

let autoTimer = null;


const prevValues = {};

/* ===== helpers ===== */
function fmt(n){
    return Number.isFinite(n) ? n.toFixed(4) : "—";
}

function setLevelCell(cell, level){
    if (!cell) return;

    if (!level){
        cell.innerHTML = `<span class="level-badge level-none"><span class="dot none"></span> —</span>`;
        return;
    }

    const text = level === "low" ? "low" : (level === "med" ? "medium" : "high");
    cell.innerHTML = `
    <span class="level-badge level-${level}">
      <span class="dot ${level}"></span> ${text}
    </span>`;
}

function setTrendCell(cell, dir){
    if (!cell) return;

    if (dir === "none"){
        cell.innerHTML = `<span class="trend-badge trend-flat"><span class="arrow">—</span> —</span>`;
        return;
    }

    if (dir === "up"){
        cell.innerHTML = `<span class="trend-badge trend-up"><span class="arrow">↑</span> up</span>`;
        return;
    }

    if (dir === "down"){
        cell.innerHTML = `<span class="trend-badge trend-down"><span class="arrow">↓</span> down</span>`;
        return;
    }

    cell.innerHTML = `<span class="trend-badge trend-flat"><span class="arrow">→</span> neutral</span>`;
}

function levelFor(id, v){
    if (!Number.isFinite(v)) return null;

    // RSI: classic thresholds
    if (id === "rsi14"){
        if (v < 30) return "low";
        if (v > 70) return "high";
        return "med";
    }

    // ROC: abs thresholds
    if (id === "roc10"){
        const a = Math.abs(v);
        if (a < 0.5) return "low";
        if (a < 2) return "med";
        return "high";
    }

    // MACD values can be large, so wider thresholds
    if (id.startsWith("macd")){
        const a = Math.abs(v);
        if (a < 0.1) return "low";
        if (a < 1) return "med";
        return "high";
    }

    const a = Math.abs(v);
    if (a < 1) return "low";
    if (a < 50) return "med";
    return "high";
}


function trendFromPrev(curr, prev){
    if (!Number.isFinite(curr) || !Number.isFinite(prev)) return "none";


    const eps = Math.max(Math.abs(prev) * 0.0001, 0.0005);
    const d = curr - prev;

    if (d > eps) return "up";
    if (d < -eps) return "down";
    return "neutral";
}

indicators.forEach(ind => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
    <td class="ind-name">${ind.label}</td>
    <td class="ind-value" data-val="${ind.id}">—</td>
    <td data-lvl="${ind.id}"></td>
    <td data-tr="${ind.id}"></td>
    <td class="ind-status" data-st="${ind.id}">—</td>
  `;
    tbody.appendChild(tr);

    setLevelCell(tr.querySelector(`[data-lvl="${ind.id}"]`), null);
    setTrendCell(tr.querySelector(`[data-tr="${ind.id}"]`), "none");
});

/* ===== load ===== */
async function loadIndicators(){
    const symbol = (document.getElementById("symbol").value || "BTCUSDT").trim().toUpperCase();
    statusText.textContent = "Loading...";

    let hadErrors = false;

    for (const ind of indicators){
        const valCell = document.querySelector(`[data-val="${ind.id}"]`);
        const lvlCell = document.querySelector(`[data-lvl="${ind.id}"]`);
        const trCell  = document.querySelector(`[data-tr="${ind.id}"]`);
        const stCell  = document.querySelector(`[data-st="${ind.id}"]`);

        try{
            const res = await fetch(ind.path, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ symbol })
            });

            const txt = await res.text();

            if (!res.ok){
                hadErrors = true;

                if (valCell) valCell.textContent = "—";
                setLevelCell(lvlCell, null);
                setTrendCell(trCell, "none");

                if (stCell){
                    stCell.textContent = `Error ${res.status}`;
                    stCell.classList.remove("ok");
                    stCell.classList.add("err");
                }
                continue;
            }

            const num = parseFloat(txt);

            // value
            if (valCell) valCell.textContent = Number.isFinite(num) ? fmt(num) : txt;

            // status
            if (stCell){
                stCell.textContent = "ok";
                stCell.classList.remove("err");
                stCell.classList.add("ok");
            }

            // level
            setLevelCell(lvlCell, levelFor(ind.id, num));

            // trend per indicator
            const prev = prevValues[ind.id];
            const dir = trendFromPrev(num, prev);
            setTrendCell(trCell, dir);

            // store prev
            if (Number.isFinite(num)) prevValues[ind.id] = num;

        } catch (e){
            hadErrors = true;
            console.error(ind.id, e);

            if (stCell){
                stCell.textContent = "error";
                stCell.classList.remove("ok");
                stCell.classList.add("err");
            }
            setTrendCell(trCell, "none");
        }
    }

    statusText.textContent = hadErrors ? "Updated (with errors)" : "Updated";
}

/* manual */
loadBtn.addEventListener("click", () => loadIndicators());

/* auto update 10s */
function startAuto(){
    if (autoTimer) clearInterval(autoTimer);
    autoTimer = setInterval(loadIndicators, 10000);
}

loadIndicators();
startAuto();
