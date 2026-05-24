/**
 * ══════════════════════════════════════════════════════════════
 *  content.js — Extension Content Script
 * ══════════════════════════════════════════════════════════════
 *
 * Injected into the dashboard page (localhost:8080).
 *
 * Acts as a bridge between the dashboard (index.html) and the
 * extension service worker (background.js):
 *
 *  Dashboard JS  →  window.postMessage  →  content.js
 *  content.js    →  chrome.runtime.sendMessage  →  background.js
 *  background.js →  sendResponse  →  content.js
 *  content.js    →  window.postMessage  →  Dashboard JS
 *
 * This bridge is needed because dashboard JS cannot directly call
 * chrome.runtime.sendMessage (it's a webpage, not extension code).
 */

// ── Listen for messages from the dashboard page ───────────────
window.addEventListener('message', (event) => {
    // Only accept messages from same origin (localhost:8080)
    if (event.source !== window) return;
    if (!event.data || event.data.direction !== 'FROM_PAGE') return;

    const { action, days } = event.data;

    if (action === 'COLLECT_BROWSING_DATA') {
        // Forward request to background service worker
        chrome.runtime.sendMessage({ action: 'COLLECT_BROWSING_DATA', days: days || 7 }, (response) => {
            // Forward response back to dashboard page
            window.postMessage({
                direction: 'FROM_EXTENSION',
                action:    'BROWSING_DATA_RESULT',
                success:   response?.success ?? false,
                data:      response?.data    ?? null,
                error:     response?.error   ?? 'No response from extension background'
            }, '*');
        });
    }

    if (action === 'PING') {
        window.postMessage({
            direction: 'FROM_EXTENSION',
            action:    'PONG',
            installed: true
        }, '*');
    }
});

// ── On load: announce extension is installed to the dashboard ─
window.postMessage({
    direction: 'FROM_EXTENSION',
    action:    'EXTENSION_READY',
    installed: true
}, '*');
