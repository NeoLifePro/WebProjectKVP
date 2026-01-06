// ===== IND UI + API calls =====
(function () {
    // indikatoru nosaukumi un atbilstošie API endpointi
    const indicatorConfig = {
        RSI:  { path: '/api/ind/rsi14',    label: 'RSI(14)' },
        EMA:  { path: '/api/ind/ema14',    label: 'EMA(14)' },
        SMA:  { path: '/api/ind/sma14',    label: 'SMA(14)' },
        MACD: { path: '/api/ind/macdHist', label: 'MACD Hist' }
    };

    const symbolSelect       = document.getElementById('cryptoName');
    const toggleBtn          = document.getElementById('indicatorsToggleBtn');
    const panel              = document.getElementById('indicatorsDropdownPanel');
    const selectedContainer  = document.getElementById('selectedIndicators');

    // ===== atgriež izvēlēto simbolu pareizā formā (piem., BTCUSDT) =====
    function getCurrentSymbol() {
        const base = (symbolSelect && symbolSelect.value ? symbolSelect.value : 'BTC').toUpperCase();
        return base.endsWith('USDT') ? base : base + 'USDT';
    }

    // ===== Iegūst konkrēta indikatora vērtību no backend API =====
    async function fetchIndicator(name) {
        const cfg = indicatorConfig[name];
        if (!cfg) return null;

        // Pieprasījuma ķermenis (šobrīd api_key/api_secret ir null)
        const body = {
            symbol: getCurrentSymbol(),
            api_key: null,
            api_secret: null
        };

        try {
            const resp = await fetch(cfg.path, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            });

            // Kļūdas statusa apstrāde
            if (!resp.ok) {
                console.error('Indicator error:', name, resp.status);
                return 'err';
            }

            // Atbilde nāk kā teksts — mēģinām pārvērst uz skaitli
            const text = await resp.text();
            const num  = parseFloat(text.replace(',', '.'));

            // Ja nav skaitlis — atgriežam tekstu; ja ir — noapaļojam līdz 2 zīmēm
            if (!Number.isFinite(num)) return text;
            return num.toFixed(2);
        } catch (e) {
            console.error('Indicator fetch exception', name, e);
            return 'err';
        }
    }

    // ===== Izvada izvēlētos indikatorus UI (kā “pills”) =====
    async function renderSelectedIndicators() {
        if (!selectedContainer) return;

        // Notīram veco saturu
        selectedContainer.innerHTML = '';

        // Savācam visus atzīmētos checkbox indikatorus
        const checked = document.querySelectorAll('.indicators-list input[type="checkbox"]:checked');
        if (checked.length === 0) {
            selectedContainer.textContent = 'Пока нет выбранных индикаторов.';
            return;
        }

        const symbol = getCurrentSymbol();

        // Katram atzīmētajam indikatoram: ielādē vērtību un uztaisa “pill”
        const promises = Array.from(checked).map(async (chk) => {
            const name = chk.value;
            const cfg  = indicatorConfig[name];
            const value = await fetchIndicator(name);

            const pill = document.createElement('div');
            pill.className = 'indicator-pill';
            pill.textContent = `${cfg ? cfg.label : name} [${symbol}]: ${value}`;
            selectedContainer.appendChild(pill);
        });

        // Gaida, kamēr visi indikatori ir ielādēti
        await Promise.all(promises);
    }

    // ===== Dropdown atvēršana/aizvēršana (toggle poga + klikšķis ārpus paneļa) =====
    if (toggleBtn && panel) {
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            panel.classList.toggle('open');
        });

        document.addEventListener('click', () => {
            panel.classList.remove('open');
        });

        panel.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }

    // ===== Notikums: checkbox maiņa — pārzīmē izvēlētos indikatorus =====
    document.querySelectorAll('.indicators-list input[type="checkbox"]').forEach(chk => {
        chk.addEventListener('change', () => {
            renderSelectedIndicators();
        });
    });

    // ===== Notikums: simbols mainās — atjaunojam indikatoru vērtības =====
    if (symbolSelect) {
        symbolSelect.addEventListener('change', () => {
            renderSelectedIndicators();
        });
    }

    // ===== Sākotnējā ielāde =====
    renderSelectedIndicators();
})();
