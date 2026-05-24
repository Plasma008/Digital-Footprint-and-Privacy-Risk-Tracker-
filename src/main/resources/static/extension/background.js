/**
 * ══════════════════════════════════════════════════════════════
 *  background.js — Chrome Extension Service Worker
 * ══════════════════════════════════════════════════════════════
 *
 * Runs silently in the background and:
 *  1. Tracks time spent on each domain (tab activation + visibility)
 *  2. Resets tracking data daily via chrome.alarms
 *  3. Responds to messages from content.js (the dashboard page)
 *     with the full browsing dataset for AI analysis
 */

// ── In-memory tracking state ──────────────────────────────────
let activeTabId     = null;
let activeTabDomain = null;
let activeSince     = null;  // timestamp when this tab became active

// Accumulated dwell-time map:  domain (string) → seconds (number)
let timeSpentMap = {};

// ─────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────
function extractDomain(url) {
    try {
        const u = new URL(url);
        // Strip "www." prefix for clean domain names
        return u.hostname.replace(/^www\./, '');
    } catch {
        return null;
    }
}

function flushCurrentTab() {
    if (activeTabDomain && activeSince) {
        const elapsed = Math.floor((Date.now() - activeSince) / 1000); // seconds
        if (elapsed > 0) {
            timeSpentMap[activeTabDomain] = (timeSpentMap[activeTabDomain] || 0) + elapsed;
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Tab tracking — fires when user switches tabs
// ─────────────────────────────────────────────────────────────
chrome.tabs.onActivated.addListener(async (activeInfo) => {
    flushCurrentTab();  // save time for old tab

    try {
        const tab = await chrome.tabs.get(activeInfo.tabId);
        activeTabId     = activeInfo.tabId;
        activeTabDomain = extractDomain(tab.url);
        activeSince     = Date.now();
    } catch {
        activeTabDomain = null;
        activeSince     = null;
    }
});

// ─────────────────────────────────────────────────────────────
//  URL change tracking — same tab navigated to new page
// ─────────────────────────────────────────────────────────────
chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    if (tabId === activeTabId && changeInfo.url) {
        flushCurrentTab();
        activeTabDomain = extractDomain(changeInfo.url);
        activeSince     = Date.now();
    }
});

// ─────────────────────────────────────────────────────────────
//  Tab closed — flush its time
// ─────────────────────────────────────────────────────────────
chrome.tabs.onRemoved.addListener((tabId) => {
    if (tabId === activeTabId) {
        flushCurrentTab();
        activeTabId     = null;
        activeTabDomain = null;
        activeSince     = null;
    }
});

// ─────────────────────────────────────────────────────────────
//  Daily reset alarm — clears tracking data at midnight
// ─────────────────────────────────────────────────────────────
chrome.alarms.create('dailyReset', { periodInMinutes: 1440 });

chrome.alarms.onAlarm.addListener((alarm) => {
    if (alarm.name === 'dailyReset') {
        timeSpentMap = {};
        chrome.storage.local.set({ timeSpentMap: {} });
    }
});

// ─────────────────────────────────────────────────────────────
//  Restore persisted data on service worker startup
// ─────────────────────────────────────────────────────────────
chrome.storage.local.get(['timeSpentMap'], (result) => {
    if (result.timeSpentMap) {
        timeSpentMap = result.timeSpentMap;
    }
});

// Periodically persist time map to storage (every 30s)
setInterval(() => {
    flushCurrentTab();
    chrome.storage.local.set({ timeSpentMap });
}, 30000);

// ─────────────────────────────────────────────────────────────
//  Message handler — called by content.js from the dashboard
// ─────────────────────────────────────────────────────────────
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action !== 'COLLECT_BROWSING_DATA') {
        sendResponse({ error: 'Unknown action' });
        return true;
    }

    const scanDays = message.days || 7;

    // Flush current session before collecting
    flushCurrentTab();

    // Start async data collection
    collectBrowsingData(scanDays)
        .then(data => sendResponse({ success: true, data }))
        .catch(err  => sendResponse({ success: false, error: err.message }));

    return true; // keep message channel open for async response
});

// ─────────────────────────────────────────────────────────────
//  External message handler — called by the dashboard page
//  via chrome.runtime.sendMessage(extensionId, message)
// ─────────────────────────────────────────────────────────────
chrome.runtime.onMessageExternal.addListener((message, sender, sendResponse) => {
    if (message.action !== 'COLLECT_BROWSING_DATA') {
        sendResponse({ error: 'Unknown action' });
        return true;
    }

    const scanDays = message.days || 7;
    flushCurrentTab();

    collectBrowsingData(scanDays)
        .then(data => sendResponse({ success: true, data }))
        .catch(err  => sendResponse({ success: false, error: err.message }));

    return true;
});

// ── Advanced Signal Detection ────────────────────────────────
const TRACKER_DOMAINS = new Set([
    'doubleclick.net', 'google-analytics.com', 'googletagmanager.com',
    'facebook.net', 'adnxs.com', 'quantserve.com', 'scorecardresearch.com',
    'advertising.com', 'amazon-adsystem.com', 'adnxs.com', 'casalemedia.com',
    'criteo.com', 'openx.net', 'pubmatic.com', 'rubiconproject.com',
    'taboola.com', 'outbrain.com', 'hotjar.com', 'intercom.io'
]);

const SEARCH_DOMAINS = new Set([
    'google.com', 'bing.com', 'duckduckgo.com', 'yahoo.com', 'baidu.com'
]);

// ─────────────────────────────────────────────────────────────
//  collectBrowsingData — upgraded for Unified Model
// ─────────────────────────────────────────────────────────────
async function collectBrowsingData(scanDays) {
    const msInDay  = 24 * 60 * 60 * 1000;
    const startTime = Date.now() - (scanDays * msInDay);

    // 1. History: all visited URLs in the window
    const historyItems = await chrome.history.search({
        text:       '',
        startTime:  startTime,
        maxResults: 10000
    });

    // Extract domains from history and count frequencies
    const totalHistoryItems = historyItems.length;
    const domainFrequencies = {};
    let detectedTrackersCount = 0, searchFrequency = 0, insecureSiteCount = 0;

    for (const item of historyItems) {
        const domain = extractDomain(item.url);
        if (domain) {
            domainFrequencies[domain] = (domainFrequencies[domain] || 0) + 1;
            if (TRACKER_DOMAINS.has(domain)) detectedTrackersCount++;
            if (SEARCH_DOMAINS.has(domain)) searchFrequency++;
            if (item.url && item.url.startsWith('http://')) insecureSiteCount++;
        }
    }

    // 2. Top sites (Enrich with history frequency)
    let topSites = [];
    try {
        const ts = await chrome.topSites.get();
        topSites = ts.map(s => extractDomain(s.url)).filter(Boolean);
        const frequentFromHistory = Object.entries(domainFrequencies)
            .sort((a,b) => b[1] - a[1])
            .slice(0, 20)
            .map(e => e[0]);
        topSites = Array.from(new Set([...topSites, ...frequentFromHistory]));
    } catch { 
        topSites = Object.keys(domainFrequencies).slice(0, 20);
    }

    // 3. Open tabs
    const tabs = await chrome.tabs.query({});
    const openTabsCount = tabs.length;
    const tabDomains = tabs.map(t => extractDomain(t.url)).filter(Boolean);

    // 4. Final Merge (Previous History + Real-time Explorations)
    const allDetectedDomains = new Set([
        ...Object.keys(domainFrequencies),
        ...topSites,
        ...tabDomains,
        ...Object.keys(timeSpentMap)
    ]);

    const visitedDomains = Array.from(allDetectedDomains);

    // Filter timeSpentMap to only include domains we just detected
    const filteredTimeSpent = {};
    for (const [domain, seconds] of Object.entries(timeSpentMap)) {
        if (allDetectedDomains.has(domain)) {
            filteredTimeSpent[domain] = seconds;
        }
    }

    return {
        visitedDomains,
        topSites,
        openTabsCount,
        totalHistoryItems,
        timeSpentPerDomain: filteredTimeSpent,
        scanWindowDays: scanDays,
        detectedTrackersCount,
        searchFrequency,
        insecureSiteCount
    };
}
