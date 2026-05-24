package privacychecker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import privacychecker.model.BrowserScanRequest;
import privacychecker.model.BrowserScanResponse;
import privacychecker.model.BrowserSessionEntity;
import privacychecker.repository.BrowserSessionRepository;
import privacychecker.service.NaiveBayesSiteClassifier.Category;
import privacychecker.service.NaiveBayesSiteClassifier.ClassificationResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ══════════════════════════════════════════════════════════════
 *  Browser Risk Scoring Service
 * ══════════════════════════════════════════════════════════════
 *
 * Orchestrates the full AI-powered browser analysis pipeline:
 *
 *  1. Classify every visited domain via NaiveBayesSiteClassifier
 *  2. Build a category frequency distribution
 *  3. Compute Shannon Entropy  H = -Σ p_i · log₂(p_i)
 *  4. Apply time-weighted scoring (how long was spent on sites)
 *  5. Compute composite risk score from all signals
 *  6. Generate natural-language risk factor explanations
 *  7. Persist session to browser_sessions MySQL table
 */
@Service
public class BrowserRiskScoringService {

    private final NaiveBayesSiteClassifier classifier;
    private final BrowserSessionRepository  sessionRepository;
    private final ObjectMapper              objectMapper;

    public BrowserRiskScoringService(NaiveBayesSiteClassifier classifier,
                                     BrowserSessionRepository sessionRepository,
                                     ObjectMapper objectMapper) {
        this.classifier        = classifier;
        this.sessionRepository = sessionRepository;
        this.objectMapper      = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────
    //  Main analysis pipeline
    // ─────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────
    //  Main analysis pipeline (UNIFIED)
    // ─────────────────────────────────────────────────────────────
    public BrowserScanResponse analyze(BrowserScanRequest request) {
        List<String> domains = (request.getVisitedDomains() != null) ? request.getVisitedDomains() : List.of();
        
        // ── Step 1: Real-time Behavioral Classification ───────────
        Map<Category, Integer> categoryCounts = new EnumMap<>(Category.class);
        Map<String, Category> domainCategory = new LinkedHashMap<>();
        for (Category cat : Category.values()) categoryCounts.put(cat, 0);

        for (String domain : domains) {
            ClassificationResult res = classifier.classify(domain);
            categoryCounts.merge(res.category(), 1, (a, b) -> a + b);
            domainCategory.put(domain, res.category());
        }

        int total = Math.max(domains.size(), 1);
        double entropy = calculateEntropy(categoryCounts, total);

        // ── Step 2: Probabilistic Inference (Log-Odds Stacking) ────
        // We start with a base log-odds (0.0 means 50/50 probability of risk)
        double logOdds = 0.0;

        // Evidence from User Profile
        logOdds += getBrowserEvidence(request.getPrimaryBrowser());
        logOdds += getAgeEvidence(request.getAgeBracket());

        // Evidence from Security Posture
        if (request.isUsesVpn())             logOdds -= 1.2; // Strong safety evidence
        if (request.isUses2FA())             logOdds -= 1.0;
        if (request.isUsesAdblocker())      logOdds -= 0.8;
        if (request.isUsesPasswordManager()) logOdds -= 0.6;
        if (request.isSharedDevice())        logOdds += 1.8; // Strong risk evidence
        if (request.isIdentityLeakReported()) logOdds += 2.5; // Critical risk evidence

        // Evidence from Behavioral Footprint
        double trackerDensity = (double) request.getDetectedTrackersCount() / total;
        logOdds += (trackerDensity * 3.5); // High tracking relative to activity
        
        double insecureDensity = (double) request.getInsecureSiteCount() / total;
        logOdds += (insecureDensity * 2.0);

        logOdds += (entropy - 1.5) * 0.5; // Pattern complexity as evidence

        // ── Step 3: Convert Log-Odds to Probability [0-1] ──────────
        double probability = 1.0 / (1.0 + Math.exp(-logOdds));
        int finalScore = (int) Math.round(probability * 100.0);

        // ── Step 4: Map back to Response / Persistence ────────────
        String riskLevel = getRiskLevel(finalScore);
        String message   = buildMessage(riskLevel, domains.size(), entropy);

        BrowserSessionEntity entity = new BrowserSessionEntity();
        populateEntity(entity, request, finalScore, riskLevel, message, entropy, domainCategory);
        sessionRepository.save(entity);

        Map<String, Double> breakdown = getCategoryBreakdown(categoryCounts, total);
        List<String> riskFactors = generateRiskFactors(request, entropy, domains.size());

        // Collect Top 10 Risky Domains (High exposure)
        List<String> topSites = domainCategory.entrySet().stream()
            .limit(20
            )
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        return new BrowserScanResponse(
            request.getUsername(), finalScore, riskLevel, message, riskFactors,
            breakdown, topSites, Math.round(entropy * 100.0) / 100.0, domains.size()
        );
    }

    private double calculateEntropy(Map<Category, Integer> counts, int total) {
        double entropy = 0.0;
        for (Category cat : Category.values()) {
            double p = (double) counts.get(cat) / total;
            if (p > 0) entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private double getBrowserEvidence(String browser) {
        if (browser == null) return 0.2;
        return switch (browser.toLowerCase()) {
            case "brave", "tor" -> -1.5;
            case "firefox" -> -0.8;
            case "duckduckgo" -> -1.2;
            case "chrome", "edge" -> 0.4;
            case "safari" -> 0.1;
            default -> 0.2;
        };
    }

    private double getAgeEvidence(String age) {
        if (age == null) return 0.0;
        return switch (age) {
            case "18-25" -> 0.6; // High experimental activity
            case "60+" -> 0.4;   // Target for phishing
            default -> 0.0;
        };
    }

    private Map<String, Double> getCategoryBreakdown(Map<Category, Integer> counts, int total) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map.Entry<Category, Integer> e : counts.entrySet()) {
            double f = (double) e.getValue() / total;
            if (f > 0.001) map.put(e.getKey().name(), Math.round(f * 1000.0) / 1000.0);
        }
        return map;
    }

    private void populateEntity(BrowserSessionEntity e, BrowserScanRequest req, int score, String level, String msg, double entropy, Map<String, Category> domains) {
        e.setUsername(req.getUsername());
        e.setRiskScore(score);
        e.setRiskLevel(level);
        e.setMessage(msg);
        e.setUniqueDomainsScanned(domains.size());
        e.setOpenTabsCount(req.getOpenTabsCount());
        e.setBrowsingEntropyScore(Math.round(entropy * 100.0) / 100.0);
        e.setDetectedTrackersCount(req.getDetectedTrackersCount());
        e.setInsecureSiteCount(req.getInsecureSiteCount());
        
        // New Profile/Security
        e.setPrimaryBrowser(req.getPrimaryBrowser());
        e.setAgeBracket(req.getAgeBracket());
        e.setUsesVpn(req.isUsesVpn());
        e.setUses2FA(req.isUses2FA());
        e.setUsesAdblocker(req.isUsesAdblocker());
        e.setUsesPasswordManager(req.isUsesPasswordManager());
        e.setSharedDevice(req.isSharedDevice());
        e.setIdentityLeakReported(req.isIdentityLeakReported());

        try {
            e.setTopRiskyDomainsJson(objectMapper.writeValueAsString(
                domains.entrySet().stream().limit(10).map(Map.Entry::getKey).collect(Collectors.toList())
            ));
        } catch (Exception ignored) {}
    }

    public List<BrowserSessionEntity> getBrowserHistory(String username) {
        return sessionRepository.findByUsernameOrderByScannedAtDesc(username);
    }

    private String getRiskLevel(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 55) return "HIGH";
        if (score >= 30) return "MEDIUM";
        return "LOW";
    }

    private String buildMessage(String level, int domains, double entropy) {
        return switch (level) {
            case "CRITICAL" -> "Inference engine detects critical pattern alignment with data exposure. High uniqueness & tracked behavior identified.";
            case "HIGH" -> "Significant risk probability derived from browser choice and tracking density.";
            case "MEDIUM" -> "Moderate risk profile. Some behavioral patterns suggest footprint leakage.";
            default -> "Low risk. Security posture effectively mitigates digital footprint exposure.";
        };
    }

    private List<String> generateRiskFactors(BrowserScanRequest r, double entropy, int uniqueDomains) {
        List<String> f = new ArrayList<>();
        if (r.isIdentityLeakReported()) f.add("Critical: Identity leakage evidence detected in external datasets");
        if (r.isSharedDevice()) f.add("High Risk: Shared environment significantly expands local footprint vulnerability");
        if (r.getDetectedTrackersCount() > 20) f.add("Real-time Tracking: High density of persistent tracking probes");
        
        // Positive Evidence
        if (r.isUsesVpn()) f.add("Safety Signal: Active VPN encrypts behavioral metadata");
        if (r.isUsesAdblocker()) f.add("Safety Signal: Adblocker reduces real-time script tracking");
        
        if (entropy > 2.5) f.add("Footprint: High browsing diversity increases pattern uniqueness");
        if (uniqueDomains > 50) f.add("Footprint: Wide domain breadth allows for cross-site correlation");
        
        if (f.isEmpty()) f.add("Optimal hygiene patterns detected by AI model.");
        return f;
    }
}
