package privacychecker.service;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ══════════════════════════════════════════════════════════════
 *  Naive Bayes Site Classifier  —  AI Component (Pure Java)
 * ══════════════════════════════════════════════════════════════
 *
 * Classifies any domain name into one of 8 risk categories using
 * probabilistic Naive Bayes inference with Laplace smoothing.
 *
 * Mathematical model:
 *   P(category | tokens) ∝ P(category) × ∏ P(token | category)
 *
 * Token likelihoods are computed from an embedded vocabulary
 * (pre-trained knowledge of which tokens appear in which categories).
 * Laplace smoothing prevents zero-probability for unseen tokens.
 *
 * The classifier outputs both a predicted category AND a confidence
 * score (posterior probability), making it a full probabilistic model.
 */
@Component
public class NaiveBayesSiteClassifier {

    // ── Risk Categories ──────────────────────────────────────────
    public enum Category {
        SOCIAL_MEDIA,
        SHOPPING,
        ADULT,
        GAMBLING,
        FINANCIAL,
        PRIVACY_TOOLS,
        DATA_BROKER,
        NEUTRAL
    }

    /**
     * Prior probabilities P(category) — representing the base rate of
     * each category in average internet usage (web traffic distribution).
     * These are the "class priors" in the Bayesian model.
     */
    private static final Map<Category, Double> PRIORS = new EnumMap<>(Category.class);

    /**
     * Risk weight per category — used by the scoring engine to translate
     * classification results into a risk score. Negative = risk reducer.
     */
    public static final Map<Category, Double> RISK_WEIGHTS = new EnumMap<>(Category.class);

    /**
     * Vocabulary per category — the "training data" for the Naive Bayes model.
     * Each set contains domain tokens (substrings) that strongly indicate
     * membership in that category.
     */
    private static final Map<Category, Set<String>> TOKEN_VOCAB = new EnumMap<>(Category.class);

    static {
        // ── Priors (must sum to ~1.0) ──────────────────────────────
        PRIORS.put(Category.SOCIAL_MEDIA,   0.18);
        PRIORS.put(Category.SHOPPING,       0.14);
        PRIORS.put(Category.ADULT,          0.10);
        PRIORS.put(Category.GAMBLING,       0.05);
        PRIORS.put(Category.FINANCIAL,      0.08);
        PRIORS.put(Category.PRIVACY_TOOLS,  0.04);
        PRIORS.put(Category.DATA_BROKER,    0.02);
        PRIORS.put(Category.NEUTRAL,        0.39);

        // ── Risk weights (used by BrowserRiskScoringService) ───────
        RISK_WEIGHTS.put(Category.SOCIAL_MEDIA,   0.30);
        RISK_WEIGHTS.put(Category.SHOPPING,       0.18);
        RISK_WEIGHTS.put(Category.ADULT,          0.25);
        RISK_WEIGHTS.put(Category.GAMBLING,       0.22);
        RISK_WEIGHTS.put(Category.FINANCIAL,      0.12);
        RISK_WEIGHTS.put(Category.PRIVACY_TOOLS, -0.20);  // lowers risk
        RISK_WEIGHTS.put(Category.DATA_BROKER,    0.28);
        RISK_WEIGHTS.put(Category.NEUTRAL,        0.02);

        // ── Vocabulary (token → category likelihood) ───────────────

        TOKEN_VOCAB.put(Category.SOCIAL_MEDIA, new HashSet<>(Arrays.asList(
            "facebook", "instagram", "twitter", "tiktok", "snapchat", "linkedin",
            "pinterest", "reddit", "tumblr", "whatsapp", "telegram", "discord",
            "threads", "mastodon", "bluesky", "vk", "weibo", "wechat",
            "youtube", "twitch", "kick", "clubhouse", "bereal", "social",
            "share", "feed", "timeline", "reels", "stories", "dm", "chat"
        )));

        TOKEN_VOCAB.put(Category.SHOPPING, new HashSet<>(Arrays.asList(
            "amazon", "flipkart", "ebay", "walmart", "shopify", "meesho",
            "myntra", "snapdeal", "aliexpress", "etsy", "shein", "ajio",
            "nykaa", "jiomart", "indiamart", "paytmmall", "bigbasket",
            "blinkit", "zepto", "grofers", "shop", "store", "cart",
            "buy", "checkout", "order", "deals", "sale", "market",
            "product", "offer", "discount", "price", "shipping", "delivery"
        )));

        TOKEN_VOCAB.put(Category.ADULT, new HashSet<>(Arrays.asList(
            "porn", "xxx", "adult", "nude", "sex", "escort", "nsfw", "erotic",
            "hentai", "onlyfans", "fansly", "cam", "mature", "xvideo",
            "xhamster", "redtube", "pornhub", "literotica", "rule34",
            "hotscope", "camsoda", "stripchat", "livejasmin"
        )));

        TOKEN_VOCAB.put(Category.GAMBLING, new HashSet<>(Arrays.asList(
            "bet", "casino", "poker", "lottery", "slots", "roulette", "bingo",
            "dream11", "mpl", "rummy", "gamble", "wager", "bet365", "betway",
            "draftkings", "fanduel", "stake", "1xbet", "mostbet",
            "cricket", "fantasy", "winzo", "junglee", "teen", "patti",
            "sportsbet", "oddschecker", "bovada", "mybookie"
        )));

        TOKEN_VOCAB.put(Category.FINANCIAL, new HashSet<>(Arrays.asList(
            "bank", "hdfc", "sbi", "icici", "axis", "kotak", "pnb", "canara",
            "paypal", "paytm", "gpay", "phonepe", "razorpay", "stripe",
            "crypto", "bitcoin", "ethereum", "binance", "coinbase", "wazirx",
            "zerodha", "groww", "upstox", "angelone", "loan", "credit",
            "debit", "wallet", "upi", "neft", "trading", "invest",
            "mutual", "fund", "insurance", "lic", "stock", "equity",
            "forex", "commodity", "nse", "bse", "sebi", "rbi"
        )));

        TOKEN_VOCAB.put(Category.PRIVACY_TOOLS, new HashSet<>(Arrays.asList(
            "duckduckgo", "proton", "tutanota", "tor", "vpn", "nordvpn",
            "expressvpn", "mullvad", "privacyguides", "brave", "firefox",
            "ghostery", "ublock", "adguard", "signal", "wickr", "keybase",
            "haveibeenpwned", "bitwarden", "keepass", "privacy", "secure",
            "encrypted", "anonymous", "eff", "tails", "whonix", "startpage",
            "searx", "peertube", "element", "matrix"
        )));

        TOKEN_VOCAB.put(Category.DATA_BROKER, new HashSet<>(Arrays.asList(
            "intelius", "spokeo", "whitepages", "beenverified", "peoplefinder",
            "pipl", "intellius", "mylife", "truthfinder", "instantcheckmate",
            "usearch", "radaris", "zabasearch", "peoplesmarter",
            "publicrecords", "backgroundcheck", "lookup", "findperson",
            "addresses", "phonebook", "peoplesearch", "checkr"
        )));

        TOKEN_VOCAB.put(Category.NEUTRAL, new HashSet<>(Arrays.asList(
            "google", "wikipedia", "stackoverflow", "github", "gitlab",
            "microsoft", "apple", "oracle", "ibm", "adobe", "docs",
            "news", "bbc", "cnn", "ndtv", "thehindu", "medium",
            "dev", "hashnode", "coursera", "udemy", "edx", "khanacademy",
            "openai", "gemini", "claude", "weather", "maps", "translate",
            "calendar", "mail", "drive", "cloud", "notion", "obsidian",
            "figma", "canva", "slack", "jira", "confluence", "trello"
        )));
    }

    // ── Classification Result Record ─────────────────────────────
    public record ClassificationResult(Category category, double confidence) {}

    /**
     * Classify a domain name using Naive Bayes inference.
     *
     * Algorithm:
     *  1. Tokenize domain into meaningful substrings
     *  2. For each category, compute log-posterior:
     *       log P(cat|tokens) = log P(cat) + Σ log P(token|cat)
     *  3. Apply softmax to convert log-posteriors → probabilities
     *  4. Return (argmax category, max probability)
     *
     * @param domain  a raw domain string like "www.facebook.com"
     * @return ClassificationResult with predicted category and confidence
     */
    public ClassificationResult classify(String domain) {
        Set<String> tokens = tokenize(domain);
        Map<Category, Double> logPosteriors = new EnumMap<>(Category.class);

        for (Category cat : Category.values()) {
            double logProb = Math.log(PRIORS.get(cat));
            Set<String> vocab = TOKEN_VOCAB.get(cat);
            int vocabSize = vocab.size();

            for (String token : tokens) {
                // Laplace (add-1) smoothing:
                // P(token|cat) = (seen ? vocabSize+1 : 1) / (vocabSize+2)
                double likelihood = vocab.contains(token)
                    ? (double)(vocabSize + 1) / (vocabSize + 2)
                    : 1.0 / (vocabSize + 2);
                logProb += Math.log(likelihood);
            }

            logPosteriors.put(cat, logProb);
        }

        // Softmax normalization (numerically stable via max-shift)
        double maxLog = logPosteriors.values().stream()
            .mapToDouble(Double::doubleValue).max().orElse(0.0);
        double sumExp = logPosteriors.values().stream()
            .mapToDouble(v -> Math.exp(v - maxLog)).sum();

        Category bestCategory = Category.NEUTRAL;
        double bestProbability = 0.0;

        for (Map.Entry<Category, Double> entry : logPosteriors.entrySet()) {
            double prob = Math.exp(entry.getValue() - maxLog) / sumExp;
            if (prob > bestProbability) {
                bestProbability = prob;
                bestCategory   = entry.getKey();
            }
        }

        return new ClassificationResult(bestCategory, bestProbability);
    }

    /**
     * Tokenize a domain string into meaningful lowercase tokens.
     *
     * Examples:
     *   "www.facebook.com"       → {"facebook"}
     *   "m.banking.hdfc.in"      → {"banking", "hdfc"}
     *   "casino-royale-bet.co"   → {"casino", "royale", "bet"}
     */
    private Set<String> tokenize(String domain) {
        if (domain == null || domain.isBlank()) return Set.of();

        // Strip protocol
        domain = domain.replaceAll("https?://", "").toLowerCase().trim();

        // Split on delimiters
        String[] parts = domain.split("[.\\-_/]");

        Set<String> tokens = new HashSet<>();
        for (String part : parts) {
            String clean = part.strip();
            if (!clean.isBlank() && !isStopWord(clean) && clean.length() > 1) {
                tokens.add(clean);
            }
        }
        return tokens;
    }

    private boolean isStopWord(String token) {
        return Set.of(
            "www", "com", "net", "org", "in", "co", "io", "app", "web",
            "the", "go", "get", "my", "us", "uk", "de", "fr", "jp", "au",
            "ca", "eu", "cn", "ru", "br", "mobile", "api", "cdn", "static"
        ).contains(token);
    }
}
