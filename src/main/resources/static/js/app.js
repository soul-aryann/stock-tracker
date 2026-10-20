// Apex Markets — Equity Intelligence & Portfolio Desk

let currentSymbol = 'IBM';
let currentTimeframeDays = 30;
let historyChart = null;

// DOM Elements
const deskTabs = document.querySelectorAll('.desk-tab');
const workspacePanes = document.querySelectorAll('.workspace-pane');
const searchInput = document.getElementById('symbol-search-input');
const searchDropdown = document.getElementById('search-dropdown');
const searchSpinner = document.getElementById('search-spinner');
const toastContainer = document.getElementById('toast-container');
const ribbonTrack = document.getElementById('ribbon-track');

// App Initialization
document.addEventListener('DOMContentLoaded', () => {
    initDeskNavigation();
    initGlobalShortcuts();
    initSearchEngine();
    initChartControls();
    initWatchlistDesk();
    initPortfolioDesk();
    initAlertsDesk();

    // Initial Load: active dossier and watchlist only to preserve rate limits
    loadSecurityDossier(currentSymbol);
    loadWatchlist();
});

// Toast Notifications
function notify(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast-item ${type}`;
    toast.textContent = message;
    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(6px)';
        setTimeout(() => toast.remove(), 250);
    }, 3200);
}

// Global Shortcuts
function initGlobalShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Press '/' to jump to ticker search
        if (e.key === '/' && !['INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement.tagName)) {
            e.preventDefault();
            searchInput.focus();
            searchInput.select();
        }
        // Press 'Escape' to dismiss modals / search
        if (e.key === 'Escape') {
            searchDropdown.classList.add('hidden');
            const modal = document.getElementById('holding-modal');
            if (modal && !modal.classList.contains('hidden')) {
                modal.classList.add('hidden');
            }
        }
    });
}

// Desk Navigation
function initDeskNavigation() {
    deskTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            deskTabs.forEach(t => {
                t.classList.remove('active');
                t.setAttribute('aria-selected', 'false');
            });
            workspacePanes.forEach(p => p.classList.remove('active'));

            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');
            const targetPane = document.getElementById(tab.getAttribute('data-tab'));
            if (targetPane) {
                targetPane.classList.add('active');
            }

            const tabId = tab.getAttribute('data-tab');
            if (tabId === 'watchlist-tab') loadWatchlist();
            if (tabId === 'portfolio-tab') loadPortfolio();
            if (tabId === 'alerts-tab') loadAlerts();
        });
    });

    document.getElementById('refresh-all-btn').addEventListener('click', () => {
        loadSecurityDossier(currentSymbol);
        loadWatchlist();
        loadPortfolio();
        loadAlerts();
        notify('Workspace synchronized with live data');
    });
}

// Search Engine
let searchDebounce = null;
function initSearchEngine() {
    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.trim();
        clearTimeout(searchDebounce);

        if (query.length < 2) {
            searchDropdown.classList.add('hidden');
            searchSpinner.classList.add('hidden');
            return;
        }

        searchSpinner.classList.remove('hidden');
        searchDebounce = setTimeout(async () => {
            try {
                const res = await fetch(`/api/v1/stocks/search?query=${encodeURIComponent(query)}`);
                if (!res.ok) throw new Error('Search failed');
                const results = await res.json();
                renderSearchResults(results);
            } catch (err) {
                console.error(err);
            } finally {
                searchSpinner.classList.add('hidden');
            }
        }, 450);
    });

    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !searchDropdown.contains(e.target)) {
            searchDropdown.classList.add('hidden');
        }
    });
}

function renderSearchResults(results) {
    searchDropdown.innerHTML = '';
    if (!results || results.length === 0) {
        searchDropdown.innerHTML = '<div class="search-row-item" style="color: var(--text-muted); font-size: 11px;">No securities matched query</div>';
        searchDropdown.classList.remove('hidden');
        return;
    }

    results.forEach(item => {
        const row = document.createElement('div');
        row.className = 'search-row-item';
        row.innerHTML = `
            <div>
                <span class="search-primary">${item.symbol}</span>
                <span class="search-secondary">${item.name}</span>
            </div>
            <span class="search-meta">${item.region || ''} • ${item.currency || ''}</span>
        `;
        row.addEventListener('click', () => {
            currentSymbol = item.symbol;
            searchInput.value = '';
            searchDropdown.classList.add('hidden');
            document.getElementById('nav-dashboard').click();
            loadSecurityDossier(currentSymbol);
        });
        searchDropdown.appendChild(row);
    });

    searchDropdown.classList.remove('hidden');
}

// Security Dossier (Terminal Tab)
async function loadSecurityDossier(symbol) {
    document.getElementById('active-symbol-badge').textContent = symbol;
    document.getElementById('chart-loader').classList.remove('hidden');

    // 1. Fetch Real-time Quote
    try {
        const quoteRes = await fetch(`/api/v1/stocks/${symbol}`);
        if (quoteRes.ok) {
            const quote = await quoteRes.json();
            document.getElementById('active-stock-price').textContent = `$${quote.price.toFixed(2)}`;
            document.getElementById('active-stock-updated').textContent = `Settlement Date: ${quote.lastUpdated || '--'}`;

            // Populate Live Session Statistics
            document.getElementById('metric-high').textContent = quote.high > 0 ? `$${quote.high.toFixed(2)}` : '--';
            document.getElementById('metric-low').textContent = quote.low > 0 ? `$${quote.low.toFixed(2)}` : '--';
            document.getElementById('metric-open').textContent = quote.open > 0 ? `$${quote.open.toFixed(2)}` : '--';
            document.getElementById('metric-prev-close').textContent = quote.previousClose > 0 ? `$${quote.previousClose.toFixed(2)}` : '--';
            document.getElementById('metric-volume').textContent = quote.volume > 0 ? quote.volume.toLocaleString() : '--';

            // Day Change
            const changeEl = document.getElementById('metric-change');
            const headlineChangeEl = document.getElementById('active-stock-change');
            if (quote.change !== 0 || (quote.changePercent && quote.changePercent !== '0.0%')) {
                const sign = quote.change >= 0 ? '+' : '';
                const text = `${sign}$${quote.change.toFixed(2)} (${quote.changePercent})`;
                changeEl.textContent = text;
                changeEl.className = quote.change >= 0 ? 'delta-tag gain' : 'delta-tag loss';

                headlineChangeEl.textContent = text;
                headlineChangeEl.className = quote.change >= 0 ? 'delta-tag gain' : 'delta-tag loss';
                headlineChangeEl.classList.remove('hidden');
            } else {
                changeEl.textContent = '--';
                headlineChangeEl.classList.add('hidden');
            }
        } else {
            const errText = await quoteRes.text();
            document.getElementById('active-stock-price').textContent = 'UNAVAILABLE';
            document.getElementById('active-stock-change').classList.add('hidden');
            notify(errText || 'Security quote unavailable', 'error');
        }
    } catch (err) {
        console.error(err);
    }

    // 2. Fetch Fundamentals Overview
    try {
        const overviewRes = await fetch(`/api/v1/stocks/${symbol}/overview`);
        if (overviewRes.ok) {
            const overview = await overviewRes.json();
            if (overview.Name) {
                document.getElementById('active-stock-name').textContent = overview.Name;
            }
            if (overview.Exchange) {
                document.getElementById('active-stock-exchange').textContent = overview.Exchange;
            }
            document.getElementById('metric-marketcap').textContent = overview.MarketCapitalization ? formatMagnitude(overview.MarketCapitalization) : '--';
            document.getElementById('metric-pe').textContent = overview.PERatio || '--';
            document.getElementById('metric-sector').textContent = overview.Sector || 'Equity';
            if (overview.Description) {
                document.getElementById('metric-description').textContent = overview.Description;
            } else {
                document.getElementById('metric-description').textContent = `${symbol} equity asset. Live session quotes, real-time settlement values, and volume history tracked via exchange feeds.`;
            }
        }
    } catch (err) {
        console.error(err);
    }

    // 3. Historical Chart
    loadHistoryChart(symbol, currentTimeframeDays);
}

async function loadHistoryChart(symbol, days) {
    document.getElementById('chart-loader').classList.remove('hidden');
    try {
        const res = await fetch(`/api/v1/stocks/${symbol}/history?days=${days}`);
        if (!res.ok) throw new Error('History fetch failed');
        const historyData = await res.json();
        
        // Compute Period Range and Session Metrics from historical data
        if (historyData.length > 0) {
            const latest = historyData[0];
            const prev = historyData.length > 1 ? historyData[1] : null;

            if (document.getElementById('metric-high').textContent === '--' && latest.high) {
                document.getElementById('metric-high').textContent = `$${latest.high.toFixed(2)}`;
            }
            if (document.getElementById('metric-low').textContent === '--' && latest.low) {
                document.getElementById('metric-low').textContent = `$${latest.low.toFixed(2)}`;
            }
            if (document.getElementById('metric-open').textContent === '--' && latest.open) {
                document.getElementById('metric-open').textContent = `$${latest.open.toFixed(2)}`;
            }
            if (document.getElementById('metric-volume').textContent === '--' && latest.volume) {
                document.getElementById('metric-volume').textContent = latest.volume.toLocaleString();
            }
            if (prev && document.getElementById('metric-prev-close').textContent === '--' && prev.close) {
                document.getElementById('metric-prev-close').textContent = `$${prev.close.toFixed(2)}`;
                const diff = latest.close - prev.close;
                const pct = ((diff / prev.close) * 100).toFixed(2);
                const sign = diff >= 0 ? '+' : '';
                const changeStr = `${sign}$${diff.toFixed(2)} (${sign}${pct}%)`;

                const changeEl = document.getElementById('metric-change');
                if (changeEl.textContent === '--') {
                    changeEl.textContent = changeStr;
                    changeEl.className = diff >= 0 ? 'delta-tag gain' : 'delta-tag loss';
                }

                const headlineChangeEl = document.getElementById('active-stock-change');
                if (headlineChangeEl.textContent === '--' || headlineChangeEl.classList.contains('hidden')) {
                    headlineChangeEl.textContent = changeStr;
                    headlineChangeEl.className = diff >= 0 ? 'delta-tag gain' : 'delta-tag loss';
                    headlineChangeEl.classList.remove('hidden');
                }
            }

            const highs = historyData.map(d => d.high || d.close);
            const lows = historyData.map(d => d.low || d.close);
            const periodHigh = Math.max(...highs);
            const periodLow = Math.min(...lows);
            document.getElementById('metric-period-range').textContent = `$${periodLow.toFixed(2)} — $${periodHigh.toFixed(2)}`;
        }

        renderChart(historyData.slice().reverse());
    } catch (err) {
        console.error(err);
    } finally {
        document.getElementById('chart-loader').classList.add('hidden');
    }
}

// Chart Console Controls
function initChartControls() {
    document.querySelectorAll('.tf-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tf-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentTimeframeDays = parseInt(btn.getAttribute('data-days'), 10);
            loadHistoryChart(currentSymbol, currentTimeframeDays);
        });
    });

    document.getElementById('add-to-watchlist-btn').addEventListener('click', async () => {
        try {
            const res = await fetch('/api/v1/stocks/favourites', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ symbol: currentSymbol })
            });
            if (res.ok) {
                notify(`${currentSymbol} added to watchlist`, 'success');
                loadWatchlist();
            } else {
                const msg = await res.text();
                notify(msg, 'error');
            }
        } catch (err) {
            notify('Error registering to watchlist', 'error');
        }
    });

    document.getElementById('open-position-btn').addEventListener('click', () => {
        document.getElementById('holding-symbol').value = currentSymbol;
        document.getElementById('holding-date').value = new Date().toISOString().split('T')[0];
        document.getElementById('holding-modal').classList.remove('hidden');
    });

    // Quick inline alert on the dossier pane
    const inlineAlertForm = document.getElementById('quick-alert-inline-form');
    if (inlineAlertForm) {
        inlineAlertForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const direction = document.getElementById('inline-alert-direction').value;
            const targetPrice = parseFloat(document.getElementById('inline-alert-target').value);
            try {
                const res = await fetch('/api/v1/alerts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ symbol: currentSymbol, direction, targetPrice })
                });
                if (res.ok) {
                    inlineAlertForm.reset();
                    notify(`Alert armed for ${currentSymbol} @ $${targetPrice.toFixed(2)}`, 'success');
                } else {
                    notify('Failed to arm alert', 'error');
                }
            } catch (err) {
                notify('Error creating alert', 'error');
            }
        });
    }
}

function renderChart(data) {
    const ctx = document.getElementById('stockHistoryChart').getContext('2d');
    const labels = data.map(d => d.date);
    const prices = data.map(d => d.close);

    const isPositive = prices.length >= 2 ? prices[prices.length - 1] >= prices[0] : true;
    const lineColor = isPositive ? '#34D399' : '#F87171';

    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, isPositive ? 'rgba(52, 211, 153, 0.15)' : 'rgba(248, 113, 113, 0.15)');
    gradient.addColorStop(1, 'rgba(16, 18, 20, 0.0)');

    if (historyChart) {
        historyChart.destroy();
    }

    historyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Close',
                data: prices,
                borderColor: lineColor,
                borderWidth: 1.8,
                backgroundColor: gradient,
                fill: true,
                tension: 0.15,
                pointRadius: prices.length > 40 ? 0 : 2.5,
                pointHoverRadius: 5,
                pointBackgroundColor: lineColor
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'index'
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#1C1F24',
                    titleColor: '#9DA1A8',
                    bodyColor: '#F0EFEA',
                    borderColor: '#333842',
                    borderWidth: 1,
                    padding: 8,
                    displayColors: false,
                    titleFont: { family: 'IBM Plex Mono', size: 10 },
                    bodyFont: { family: 'IBM Plex Mono', size: 12, weight: 600 },
                    callbacks: {
                        label: (ctx) => `Close: $${parseFloat(ctx.parsed.y).toFixed(2)}`
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(39, 43, 50, 0.6)', drawBorder: false },
                    ticks: { color: '#5C616B', font: { family: 'IBM Plex Mono', size: 9 }, maxTicksLimit: 8 }
                },
                y: {
                    position: 'right',
                    grid: { color: 'rgba(39, 43, 50, 0.6)', drawBorder: false },
                    ticks: {
                        color: '#5C616B',
                        font: { family: 'IBM Plex Mono', size: 10 },
                        callback: (v) => `$${v.toFixed(0)}`
                    }
                }
            }
        }
    });
}

// Watchlist Desk
function initWatchlistDesk() {
    const addBtn = document.getElementById('quick-add-watchlist-btn');
    const input = document.getElementById('quick-add-watchlist-input');

    addBtn.addEventListener('click', async () => {
        const symbol = input.value.trim().toUpperCase();
        if (!symbol) return;
        try {
            const res = await fetch('/api/v1/stocks/favourites', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ symbol })
            });
            if (res.ok) {
                input.value = '';
                notify(`${symbol} added to watchlist`, 'success');
                loadWatchlist();
            } else {
                const msg = await res.text();
                notify(msg, 'error');
            }
        } catch (err) {
            notify('Failed to save to watchlist', 'error');
        }
    });
}

async function loadWatchlist() {
    const tbody = document.getElementById('watchlist-tbody');
    try {
        const res = await fetch('/api/v1/stocks/favourites');
        if (!res.ok) throw new Error('Failed to load watchlist');
        const list = await res.json();

        // Render Ribbon
        if (ribbonTrack) {
            if (list.length === 0) {
                ribbonTrack.innerHTML = '<span class="ribbon-placeholder">No tracked tickers in active watchlist.</span>';
            } else {
                ribbonTrack.innerHTML = list.map(item => `
                    <span class="ribbon-item" onclick="viewSecurity('${item.symbol}')">
                        <strong>${item.symbol}</strong>
                        <span>$${item.price > 0 ? item.price.toFixed(2) : '--'}</span>
                    </span>
                `).join('<span style="color: var(--rule-subtle);">•</span>');
            }
        }

        // Render Table
        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="table-empty-notice">No tracked symbols in watchlist. Enter a ticker above to begin.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(item => `
            <tr>
                <td class="col-symbol"><span style="cursor: pointer;" onclick="viewSecurity('${item.symbol}')">${item.symbol}</span></td>
                <td class="col-numeric" style="font-weight: 600; color: var(--text-primary);">$${item.price > 0 ? item.price.toFixed(2) : 'Unavailable'}</td>
                <td class="col-date">${item.lastUpdated || '--'}</td>
                <td class="col-actions">
                    <button class="command-btn" style="height: 22px; font-size: 10px; padding: 0 6px;" onclick="viewSecurity('${item.symbol}')">View</button>
                    <button class="command-btn-danger-tight" onclick="removeWatchlistSecurity('${item.symbol}')">Drop</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
    }
}

window.viewSecurity = function(symbol) {
    currentSymbol = symbol;
    document.getElementById('nav-dashboard').click();
    loadSecurityDossier(symbol);
};

window.removeWatchlistSecurity = async function(symbol) {
    try {
        const res = await fetch(`/api/v1/stocks/favourites/${symbol}`, { method: 'DELETE' });
        if (res.ok) {
            notify(`${symbol} removed from watchlist`, 'success');
            loadWatchlist();
        } else {
            notify('Failed to drop security', 'error');
        }
    } catch (err) {
        notify('Error removing security', 'error');
    }
};

// Portfolio Desk
function initPortfolioDesk() {
    const modal = document.getElementById('holding-modal');
    const openBtn = document.getElementById('open-add-holding-modal-btn');
    const closeBtn = document.getElementById('close-holding-modal-btn');
    const cancelBtn = document.getElementById('cancel-holding-btn');
    const form = document.getElementById('add-holding-form');

    openBtn.addEventListener('click', () => {
        document.getElementById('holding-symbol').value = currentSymbol;
        document.getElementById('holding-date').value = new Date().toISOString().split('T')[0];
        modal.classList.remove('hidden');
    });

    closeBtn.addEventListener('click', () => modal.classList.add('hidden'));
    cancelBtn.addEventListener('click', () => modal.classList.add('hidden'));

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const symbol = document.getElementById('holding-symbol').value.trim().toUpperCase();
        const quantity = parseFloat(document.getElementById('holding-quantity').value);
        const buyPrice = parseFloat(document.getElementById('holding-price').value);
        const buyDate = document.getElementById('holding-date').value;

        try {
            const res = await fetch('/api/v1/portfolio', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ symbol, quantity, buyPrice, buyDate })
            });
            if (res.ok) {
                modal.classList.add('hidden');
                form.reset();
                notify(`Position in ${symbol} recorded`, 'success');
                loadPortfolio();
            } else {
                notify('Failed to record position', 'error');
            }
        } catch (err) {
            notify('Error recording position', 'error');
        }
    });
}

async function loadPortfolio() {
    try {
        const res = await fetch('/api/v1/portfolio/summary');
        if (!res.ok) throw new Error('Failed to load portfolio');
        const data = await res.json();

        // Update Summary Cells
        document.getElementById('stat-invested').textContent = `$${data.totalInvested.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
        document.getElementById('stat-current').textContent = `$${data.currentValue.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

        const pnlEl = document.getElementById('stat-pnl');
        pnlEl.textContent = `${data.totalProfitLoss >= 0 ? '+' : ''}$${data.totalProfitLoss.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
        pnlEl.style.color = data.totalProfitLoss >= 0 ? 'var(--color-gain)' : 'var(--color-loss)';

        const returnEl = document.getElementById('stat-return');
        returnEl.textContent = `${data.totalReturnPercentage >= 0 ? '+' : ''}${data.totalReturnPercentage.toFixed(2)}%`;
        returnEl.style.color = data.totalReturnPercentage >= 0 ? 'var(--color-gain)' : 'var(--color-loss)';

        // Populate Table
        const tbody = document.getElementById('portfolio-tbody');
        if (!data.holdings || data.holdings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="table-empty-notice">No portfolio holdings recorded. Record a position to compute book value.</td></tr>';
            return;
        }

        tbody.innerHTML = data.holdings.map(h => {
            const isGain = h.unrealizedProfitLoss >= 0;
            const deltaClass = isGain ? 'delta-tag gain' : 'delta-tag loss';
            const sign = isGain ? '+' : '';
            return `
                <tr>
                    <td class="col-symbol"><span style="cursor: pointer;" onclick="viewSecurity('${h.symbol}')">${h.symbol}</span></td>
                    <td class="col-numeric">${h.quantity}</td>
                    <td class="col-numeric">$${h.buyPrice.toFixed(2)}</td>
                    <td class="col-numeric" style="font-weight: 600; color: var(--text-primary);">$${h.currentPrice.toFixed(2)}</td>
                    <td class="col-numeric">$${h.totalInvested.toFixed(2)}</td>
                    <td class="col-numeric" style="font-weight: 600; color: var(--text-primary);">$${h.currentValue.toFixed(2)}</td>
                    <td class="col-numeric"><span class="${deltaClass}">${sign}$${h.unrealizedProfitLoss.toFixed(2)}</span></td>
                    <td class="col-numeric"><span class="${deltaClass}">${sign}${h.returnPercentage.toFixed(2)}%</span></td>
                    <td class="col-actions">
                        <button class="command-btn-danger-tight" onclick="deletePosition(${h.id})">Drop</button>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error(err);
    }
}

window.deletePosition = async function(id) {
    try {
        const res = await fetch(`/api/v1/portfolio/${id}`, { method: 'DELETE' });
        if (res.ok) {
            notify('Position dropped from portfolio', 'success');
            loadPortfolio();
        } else {
            notify('Failed to drop position', 'error');
        }
    } catch (err) {
        notify('Error deleting position', 'error');
    }
};

// Alerts Desk
function initAlertsDesk() {
    const form = document.getElementById('create-alert-form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const symbol = document.getElementById('alert-symbol').value.trim().toUpperCase();
        const direction = document.getElementById('alert-direction').value;
        const targetPrice = parseFloat(document.getElementById('alert-target-price').value);

        try {
            const res = await fetch('/api/v1/alerts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ symbol, direction, targetPrice })
            });
            if (res.ok) {
                form.reset();
                notify(`Alert registered for ${symbol} @ $${targetPrice.toFixed(2)}`, 'success');
                loadAlerts();
            } else {
                notify('Failed to register alert', 'error');
            }
        } catch (err) {
            notify('Error creating alert', 'error');
        }
    });

    document.getElementById('trigger-check-alerts-btn').addEventListener('click', async () => {
        try {
            const res = await fetch('/api/v1/alerts/check', { method: 'POST' });
            if (res.ok) {
                notify('Evaluated all active monitors against feeds', 'info');
                loadAlerts();
            }
        } catch (err) {
            notify('Error evaluating alerts', 'error');
        }
    });
}

async function loadAlerts() {
    const tbody = document.getElementById('alerts-tbody');
    try {
        const res = await fetch('/api/v1/alerts');
        if (!res.ok) throw new Error('Failed to load alerts');
        const list = await res.json();

        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="table-empty-notice">No price alerts registered.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(a => {
            const statusClass = a.triggered ? 'status-pill triggered' : 'status-pill pending';
            return `
                <tr>
                    <td class="col-symbol"><span style="cursor: pointer;" onclick="viewSecurity('${a.symbol}')">${a.symbol}</span></td>
                    <td style="font-family: var(--font-mono); font-size: 11px;">${a.direction === 'ABOVE' ? 'Rise Above (≥)' : 'Drop Below (≤)'}</td>
                    <td class="col-numeric" style="font-weight: 600; color: var(--text-primary);">$${a.targetPrice.toFixed(2)}</td>
                    <td class="col-numeric">${a.currentPrice > 0 ? `$${a.currentPrice.toFixed(2)}` : '--'}</td>
                    <td><span class="${statusClass}">${a.triggered ? 'TRIGGERED' : 'ARMED'}</span></td>
                    <td class="col-actions">
                        <button class="command-btn-danger-tight" onclick="deleteAlertRule(${a.id})">Drop</button>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error(err);
    }
}

window.deleteAlertRule = async function(id) {
    try {
        const res = await fetch(`/api/v1/alerts/${id}`, { method: 'DELETE' });
        if (res.ok) {
            notify('Alert monitor dropped', 'success');
            loadAlerts();
        } else {
            notify('Failed to drop alert', 'error');
        }
    } catch (err) {
        notify('Error deleting alert', 'error');
    }
};

function formatMagnitude(val) {
    const num = parseFloat(val);
    if (isNaN(num)) return val;
    if (num >= 1e12) return `$${(num / 1e12).toFixed(2)}T`;
    if (num >= 1e9) return `$${(num / 1e9).toFixed(2)}B`;
    if (num >= 1e6) return `$${(num / 1e6).toFixed(2)}M`;
    return `$${num.toLocaleString()}`;
}
