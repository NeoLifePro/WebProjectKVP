document.addEventListener('DOMContentLoaded', () => {
    const tbody = document.querySelector('.trades-card table tbody');
    const prevBtn = document.getElementById('trades-prev');
    const nextBtn = document.getElementById('trades-next');
    const pageSpan = document.getElementById('trades-page');

    let currentPage = 0;
    let totalPages = 1;
    const pageSize = 10;

    // ===== Ielādē darījumu sarakstu no backend ar lapošanu (page/size) =====
    async function loadTrades(page = 0) {
        try {
            const response = await fetch(`/api/trades/?page=${page}&size=${pageSize}`, {
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                console.error('Error loading trades:', response.status);
                return;
            }

            const data = await response.json();

            // Atjaunojam lapošanas parametrus
            totalPages = data.totalPages;
            currentPage = data.number;

            renderTrades(data.content);
            updateFooter();
        } catch (e) {
            console.error('Error:', e);
        }
    }

    // ===== Uzzīmē tabulas rindas no darījumu masīva =====
    function renderTrades(trades) {
        tbody.innerHTML = '';

        // Ja nav darījumu — parādām “No trades yet”
        if (!trades || trades.length === 0) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 6;
            td.textContent = 'No trades yet';
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        // Izveido rindu katram darījumam
        trades.forEach(t => {
            const tr = document.createElement('tr');

            const timeTd = document.createElement('td');
            timeTd.textContent = formatTime(t.time);

            const pairTd = document.createElement('td');
            pairTd.textContent = t.pair;

            const sideTd = document.createElement('td');
            sideTd.textContent = t.side;
            sideTd.style.fontWeight = '600';

            const priceTd = document.createElement('td');
            priceTd.textContent = t.price;

            const amountTd = document.createElement('td');
            amountTd.textContent = t.amount;

            const statusTd = document.createElement('td');
            statusTd.textContent = t.status;

            // Statusa stila klase pēc vērtības
            if (t.status === 'FILLED') {
                statusTd.classList.add('status-success');
            } else if (t.status === 'CANCELED') {
                statusTd.classList.add('status-failed');
            }

            tr.appendChild(timeTd);
            tr.appendChild(pairTd);
            tr.appendChild(sideTd);
            tr.appendChild(priceTd);
            tr.appendChild(amountTd);
            tr.appendChild(statusTd);

            tbody.appendChild(tr);
        });
    }

    // ===== Formatē ISO laiku uz lokālu laika formātu =====
    function formatTime(isoString) {
        if (!isoString) return '';
        const d = new Date(isoString);
        return d.toLocaleTimeString();
    }

    // ===== Atjauno lapošanas UI =====
    function updateFooter() {
        pageSpan.textContent = (currentPage + 1).toString();

        if (currentPage <= 0) {
            prevBtn.style.opacity = 0.5;
            prevBtn.style.pointerEvents = 'none';
        } else {
            prevBtn.style.opacity = 1;
            prevBtn.style.pointerEvents = 'auto';
        }

        // Next poga: deaktivizē, ja esam pēdējā lapā
        if (currentPage >= totalPages - 1) {
            nextBtn.style.opacity = 0.5;
            nextBtn.style.pointerEvents = 'none';
        } else {
            nextBtn.style.opacity = 1;
            nextBtn.style.pointerEvents = 'auto';
        }
    }

    // ===== Notikums: prev poga — ielādē iepriekšējo lapu =====
    prevBtn.addEventListener('click', () => {
        if (currentPage > 0) {
            loadTrades(currentPage - 1);
        }
    });


    nextBtn.addEventListener('click', () => {
        if (currentPage < totalPages - 1) {
            loadTrades(currentPage + 1);
        }
    });

    loadTrades(0);
});
