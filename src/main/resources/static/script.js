// ═══════════════════════════════════════════════════
//   Digital Footprint & Privacy Risk Checker — script.js
//   Original API logic preserved + Browser AI analysis added
// ═══════════════════════════════════════════════════

// ──────────────────────────────────────────
// Toggle factor cards
// ──────────────────────────────────────────
document.querySelectorAll('.toggle-card').forEach(btn => {
    btn.addEventListener('click', () => btn.classList.toggle('active'));
});

// ──────────────────────────────────────────
// Mobile sidebar toggle
// ──────────────────────────────────────────
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('sidebarOverlay');
const menuBtn = document.getElementById('menuToggle');

function openSidebar() { sidebar.classList.add('open'); overlay.style.display = 'block'; }
function closeSidebar() { sidebar.classList.remove('open'); overlay.style.display = 'none'; }

if (menuBtn) menuBtn.addEventListener('click', openSidebar);
if (overlay) overlay.addEventListener('click', closeSidebar);

// ──────────────────────────────────────────
// Number steppers
// ──────────────────────────────────────────
document.querySelectorAll('[data-step-target]').forEach(btn => {
    btn.addEventListener('click', () => {
        const field = document.getElementById(btn.dataset.stepTarget);
        if (!field) return;
        const dir = parseInt(btn.dataset.dir, 10);
        const min = parseInt(field.min ?? 0, 10);
        const max = parseInt(field.max ?? 99, 10);
        const cur = parseInt(field.value || 0, 10);
        field.value = Math.min(max, Math.max(min, cur + dir));
    });
});

// ──────────────────────────────────────────
// Toast notifications
// ──────────────────────────────────────────
function showToast(message, type = 'error') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const iconSvg = type === 'error'
        ? `<svg class="toast-icon" viewBox="0 0 24 24" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`
        : `<svg class="toast-icon" viewBox="0 0 24 24" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`;

    toast.innerHTML = `${iconSvg}<span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastOut 0.3s ease both';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ──────────────────────────────────────────
// Server health check
// ──────────────────────────────────────────
async function checkHealth() {
    try {
        const res = await fetch('http://localhost:8080/api/footprint/test',
            { signal: AbortSignal.timeout(3000) });
        setServerStatus(res.ok);
    } catch {
        setServerStatus(false);
    }
}

function setServerStatus(online) {
    const dot = document.getElementById('serverDot');
    const lbl = document.getElementById('serverLabel');
    const topBadge = document.getElementById('topbarBadge');
    if (online) {
        dot?.classList.remove('offline');
        if (lbl) lbl.textContent = 'Server Online';
        if (topBadge) { topBadge.textContent = '● Online'; topBadge.style.color = '#22c55e'; }
    } else {
        dot?.classList.add('offline');
        if (lbl) lbl.textContent = 'Server Offline';
        if (topBadge) { topBadge.textContent = '● Offline'; topBadge.style.color = '#ef4444'; }
    }
}

checkHealth();
setInterval(checkHealth, 30000);

// ──────────────────────────────────────────
// EXTENSION DETECTION
// ──────────────────────────────────────────
let extensionInstalled = false;

window.addEventListener('message', (event) => {
    if (!event.data || event.data.direction !== 'FROM_EXTENSION') return;

    const { action } = event.data;

    if (action === 'EXTENSION_READY' || action === 'PONG') {
        extensionInstalled = true;
        updateExtensionBadge(true);
    }

    if (action === 'BROWSING_DATA_RESULT') {
        handleBrowsingDataResult(event.data);
    }
});

// Ping once to detect if it's installed
setTimeout(() => {
    window.postMessage({ direction: 'FROM_PAGE', action: 'PING' }, '*');
}, 800);

setTimeout(() => {
    if (!extensionInstalled) updateExtensionBadge(false);
}, 2000);

function updateExtensionBadge(installed) {
    const badge = document.getElementById('extStatusBadge');
    const btnScan = document.getElementById('browserScanBtn');
    const extNotice = document.getElementById('extInstallNotice');

    if (installed) {
        if (badge) {
            badge.textContent = '● Extension Active';
            badge.style.color = '#22c55e';
        }
        if (btnScan) btnScan.disabled = false;
        if (extNotice) extNotice.style.display = 'none';
    } else {
        if (badge) {
            badge.textContent = '● Extension Not Found';
            badge.style.color = '#f59e0b';
        }
        if (btnScan) btnScan.disabled = false; // allow running without extension (manual only)
        if (extNotice) extNotice.style.display = 'flex';
    }
}

// ──────────────────────────────────────────
// UNIFIED AI ANALYSIS FLOW
// ──────────────────────────────────────────

async function runUnifiedAnalysis() {
    const username = document.getElementById('username').value.trim();
    if (!username) { showToast('Please enter a username first.'); return; }

    const btn = document.getElementById('browserScanBtn');
    const btnOrig = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span><span>Initializing Model…</span>`;

    showBrowserProgress('Requesting behavioral signals…', 10);

    // Collect Profile and Security signals
    window._unifiedManualData = {
        primaryBrowser: document.getElementById('primaryBrowser').value,
        ageBracket: document.getElementById('ageBracket').value,
        socialMediaAccounts: parseInt(document.getElementById('socialMedia').value) || 0,
        usesVpn: document.getElementById('h-vpn').classList.contains('active'),
        uses2FA: document.getElementById('h-2fa').classList.contains('active'),
        usesPasswordManager: document.getElementById('h-passmgr').classList.contains('active'),
        usesAdblocker: document.getElementById('h-adblock').classList.contains('active'),
        sharedDevice: document.getElementById('h-shared').classList.contains('active'),
        identityLeakReported: document.getElementById('h-leak').classList.contains('active')
    };

    if (extensionInstalled) {
        window.postMessage({
            direction: 'FROM_PAGE',
            action: 'COLLECT_BROWSING_DATA',
            days: 7
        }, '*');
    } else {
        // Fallback for no extension
        showToast('Extension not found. Running model on manual inputs only.', 'info');
        handleBrowsingDataResult({
            success: true,
            data: {
                visitedDomains: [], topSites: [], openTabsCount: 0,
                totalHistoryItems: 0, timeSpentPerDomain: {}, scanWindowDays: 0,
                detectedTrackersCount: 0, searchFrequency: 0, insecureSiteCount: 0
            }
        });
    }

    window._pendingBrowserUsername = username;
    window._pendingBrowserBtn = { el: btn, orig: btnOrig };
}

async function handleBrowsingDataResult(eventData) {
    const btn = window._pendingBrowserBtn?.el;
    const btnOrig = window._pendingBrowserBtn?.orig;
    const username = window._pendingBrowserUsername || '';
    const manual = window._unifiedManualData || {};

    if (!eventData.success) {
        showToast('Extension error. Running fallback analysis.');
    }

    const behavior = eventData.data || {};
    showBrowserProgress('Synthesizing signals…', 50);

    const requestBody = {
        username,
        ...manual,
        ...behavior
    };

    try {
        const res = await fetch('http://localhost:8080/api/footprint/browser-analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        const result = await res.json();
        showBrowserProgress('Report generated!', 100);

        setTimeout(() => {
            hideBrowserProgress();
            displayUnifiedResult(result, behavior);
        }, 400);

        updateStats();
    } catch (err) {
        showToast('Unified analysis failed: ' + err.message);
        hideBrowserProgress();
    } finally {
        if (btn) { btn.disabled = false; btn.innerHTML = btnOrig; }
    }
}

function displayUnifiedResult(d, rawBehavior) {
    if (!d) { showToast('Backend returned empty analysis.'); return; }
    const card = document.getElementById('browserResult');
    card.style.display = 'block';

    const score = d.riskScore ?? 0;
    const level = d.riskLevel ?? 'UNKNOWN';
    const user = d.username ?? 'Anonymous';

    // Ring animation
    const circ = 2 * Math.PI * 50;
    const fillEl = document.getElementById('brFill');
    if (fillEl) fillEl.style.strokeDashoffset = circ - (score / 100) * circ;

    const numEl = document.getElementById('brRingNum');
    if (numEl) {
        let cur = 0;
        const target = score;
        const iv = setInterval(() => {
            if (target <= 0) { numEl.textContent = 0; clearInterval(iv); return; }
            cur = Math.min(cur + 2, target);
            numEl.textContent = cur;
            if (cur >= target) clearInterval(iv);
        }, 15);
    }

    const row = document.getElementById('brScoreRow');
    if (row) row.className = 'result-grid lvl-' + level;

    const pill = document.getElementById('brLevelPill');
    if (pill) pill.textContent = level;

    const msg = document.getElementById('brRiskMsg');
    if (msg) msg.textContent = d.message ?? 'No AI analysis details available.';

    const userEl = document.getElementById('brUsername');
    if (userEl) userEl.innerHTML = `Unified AI Verdict for <strong>${user}</strong>`;

    // New Stats
    const ds = document.getElementById('brStat-domains');
    if (ds) ds.textContent = d.uniqueDomainsScanned ?? 0;

    const ts = document.getElementById('brStat-trackers');
    if (ts) ts.textContent = rawBehavior.detectedTrackersCount ?? 0;

    const is = document.getElementById('brStat-insecure');
    if (is) is.textContent = rawBehavior.insecureSiteCount ?? 0;

    renderCategoryChart(d.categoryBreakdown ?? {});

    const riskyList = document.getElementById('brRiskyDomains');
    riskyList.innerHTML = '';
    (d.topRiskyDomains || []).forEach(domain => {
        riskyList.innerHTML += `<div class="risky-domain-row"><div class="risky-domain-dot"></div><span>${domain}</span></div>`;
    });
    if (!riskyList.innerHTML) riskyList.innerHTML = 'None detected.';

    const factorsList = document.getElementById('brFactorsList');
    factorsList.innerHTML = '';
    (d.riskFactors || []).forEach(f => {
        const div = document.createElement('div');
        div.className = 'factor-row';
        div.innerHTML = `<div class="factor-icon"><svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div><span>${f}</span>`;
        factorsList.appendChild(div);
    });

    const recsList = document.getElementById('brRecommendationsList');
    if (recsList) {
        recsList.innerHTML = '';
        let recommendations = [];
        
        if (score >= 80) { // CRITICAL
            recommendations.push("Immediately run a full system malware/antivirus scan.");
            recommendations.push("Change passwords on critical accounts (Email, Banking) immediately.");
            recommendations.push("Install a reputable, system-wide VPN to encrypt your traffic.");
            recommendations.push("Enable strict tracker blocking in your browser settings.");
        } else if (score >= 55) { // HIGH
            recommendations.push("Enable Multi-Factor Authentication (2FA) on all major accounts.");
            recommendations.push("Use a dedicated password manager to eliminate password reuse.");
            recommendations.push("Consider migrating to a privacy-focused browser (e.g., Brave or Firefox).");
        } else if (score >= 30) { // MEDIUM
            recommendations.push("Review and clear your browsing history, cache, and cookies regularly.");
            recommendations.push("Install an ad-blocker (e.g., uBlock Origin) to reduce background tracking.");
        } else { // LOW
            recommendations.push("Excellent hygiene! Maintain your current security habits.");
            recommendations.push("Periodically check for leaked credentials via data breach monitors.");
        }

        // Dynamic behavior-based recommendations
        if (rawBehavior && rawBehavior.detectedTrackersCount > 20) {
            recommendations.push("Unusually high tracker density detected. Ensure your Adblocker is active and updated.");
        }
        if (rawBehavior && rawBehavior.insecureSiteCount > 0) {
            recommendations.push("Warning: Stop visiting insecure (HTTP) sites to prevent connection interception.");
        }

        recommendations.forEach(r => {
            const div = document.createElement('div');
            div.className = 'factor-row';
            div.innerHTML = `
                <div class="factor-icon" style="background:rgba(52,211,153,0.1);border-color:rgba(52,211,153,0.3);">
                    <svg viewBox="0 0 24 24" style="stroke:#34d399;"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <span>${r}</span>
            `;
            recsList.appendChild(div);
        });
    }

    setTimeout(() => card.scrollIntoView({ behavior: 'smooth' }), 100);
}

// ── Shared Utils ──────────────────────────

async function loadHistory() {
    const username = document.getElementById('username').value.trim();
    if (!username) { showToast('Username required.'); return; }

    try {
        const data = await (await fetch(`http://localhost:8080/api/footprint/browser-history/${username}`)).json();
        const card = document.getElementById('historySection');
        const list = document.getElementById('historyList');
        list.innerHTML = '';
        card.style.display = 'block';

        data.reverse().forEach(r => {
            list.innerHTML += `
                <tr>
                    <td>${r.username}</td>
                    <td><span style="font-weight:bold">${r.riskScore}</span></td>
                    <td><span class="h-badge">${r.riskLevel}</span></td>
                    <td>${new Date(r.scannedAt || Date.now()).toLocaleDateString()}</td>
                </tr>`;
        });
        card.scrollIntoView({ behavior: 'smooth' });
    } catch { showToast('History unavailable.'); }
}

function renderCategoryChart(breakdown) {
    const container = document.getElementById('categoryChart');
    if (!container || !breakdown) return;
    const COLORS = { SOCIAL_MEDIA: '#818cf8', SHOPPING: '#fb923c', ADULT: '#f87171', GAMBLING: '#fbbf24', FINANCIAL: '#34d399', PRIVACY_TOOLS: '#22d3ee', DATA_BROKER: '#e879f9', NEUTRAL: '#94a3b8' };

    container.innerHTML = Object.entries(breakdown).sort((a, b) => b[1] - a[1]).map(([cat, val]) => {
        const pct = Math.round(val * 100);
        return `<div class="chart-row"><div class="chart-label">${cat}</div><div class="chart-bar-wrap"><div class="chart-bar" style="width:${pct}%;background:${COLORS[cat] || '#ccc'}"></div></div><span>${pct}%</span></div>`;
    }).join('');
}

function showBrowserProgress(label, pct) {
    const wrap = document.getElementById('browserProgress');
    wrap.style.display = 'block';
    document.getElementById('browserProgressLabel').textContent = label;
    document.getElementById('browserProgressBar').style.width = pct + '%';
}

function hideBrowserProgress() {
    document.getElementById('browserProgress').style.display = 'none';
}

let analysisCount = 0;
function updateStats() {
    analysisCount++;
    const el = document.getElementById('statAnalysisCount');
    if (el) el.textContent = analysisCount;
}

// Final Sidebar Nav logic
document.querySelectorAll('.nav-item[data-section]').forEach(item => {
    item.addEventListener('click', () => {
        document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
        item.classList.add('active');
        const section = item.dataset.section;
        if (section === 'analysis') window.scrollTo({ top: 0, behavior: 'smooth' });
        else if (section === 'results') document.getElementById('browserResult')?.scrollIntoView({ behavior: 'smooth' });
        else if (section === 'browser') document.getElementById('browserScanCard')?.scrollIntoView({ behavior: 'smooth' });
        else if (section === 'history') loadHistory();
        closeSidebar();
    });
});
