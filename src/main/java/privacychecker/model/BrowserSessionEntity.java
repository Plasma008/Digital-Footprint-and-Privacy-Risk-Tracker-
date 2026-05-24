package privacychecker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "browser_sessions")
public class BrowserSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int riskScore;
    private String riskLevel;
    
    @Column(length = 1000)
    private String message;
    
    private LocalDateTime scannedAt = LocalDateTime.now();

    // --- Behavioral Data ---
    private int uniqueDomainsScanned;
    private int openTabsCount;
    private int totalHistoryItems;
    private int scanWindowDays;
    private double browsingEntropyScore;
    private int detectedTrackersCount;
    private int searchFrequency;
    private int insecureSiteCount;

    // --- User Profile ---
    private String primaryBrowser;
    private String ageBracket;
    private int socialMediaAccounts;

    // --- Security Posture ---
    private boolean usesVpn;
    private boolean uses2FA;
    private boolean usesPasswordManager;
    private boolean usesAdblocker;
    private boolean sharedDevice;
    private boolean identityLeakReported;

    @Column(length = 2000)
    private String topRiskyDomainsJson;

    public BrowserSessionEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public int getUniqueDomainsScanned() { return uniqueDomainsScanned; }
    public void setUniqueDomainsScanned(int uniqueDomainsScanned) { this.uniqueDomainsScanned = uniqueDomainsScanned; }

    public int getOpenTabsCount() { return openTabsCount; }
    public void setOpenTabsCount(int openTabsCount) { this.openTabsCount = openTabsCount; }

    public int getTotalHistoryItems() { return totalHistoryItems; }
    public void setTotalHistoryItems(int totalHistoryItems) { this.totalHistoryItems = totalHistoryItems; }

    public int getScanWindowDays() { return scanWindowDays; }
    public void setScanWindowDays(int scanWindowDays) { this.scanWindowDays = scanWindowDays; }

    public double getBrowsingEntropyScore() { return browsingEntropyScore; }
    public void setBrowsingEntropyScore(double browsingEntropyScore) { this.browsingEntropyScore = browsingEntropyScore; }

    public int getDetectedTrackersCount() { return detectedTrackersCount; }
    public void setDetectedTrackersCount(int detectedTrackersCount) { this.detectedTrackersCount = detectedTrackersCount; }

    public int getSearchFrequency() { return searchFrequency; }
    public void setSearchFrequency(int searchFrequency) { this.searchFrequency = searchFrequency; }

    public int getInsecureSiteCount() { return insecureSiteCount; }
    public void setInsecureSiteCount(int insecureSiteCount) { this.insecureSiteCount = insecureSiteCount; }

    public String getPrimaryBrowser() { return primaryBrowser; }
    public void setPrimaryBrowser(String primaryBrowser) { this.primaryBrowser = primaryBrowser; }

    public String getAgeBracket() { return ageBracket; }
    public void setAgeBracket(String ageBracket) { this.ageBracket = ageBracket; }

    public int getSocialMediaAccounts() { return socialMediaAccounts; }
    public void setSocialMediaAccounts(int socialMediaAccounts) { this.socialMediaAccounts = socialMediaAccounts; }

    public boolean isUsesVpn() { return usesVpn; }
    public void setUsesVpn(boolean usesVpn) { this.usesVpn = usesVpn; }

    public boolean isUses2FA() { return uses2FA; }
    public void setUses2FA(boolean uses2FA) { this.uses2FA = uses2FA; }

    public boolean isUsesPasswordManager() { return usesPasswordManager; }
    public void setUsesPasswordManager(boolean usesPasswordManager) { this.usesPasswordManager = usesPasswordManager; }

    public boolean isUsesAdblocker() { return usesAdblocker; }
    public void setUsesAdblocker(boolean usesAdblocker) { this.usesAdblocker = usesAdblocker; }

    public boolean isSharedDevice() { return sharedDevice; }
    public void setSharedDevice(boolean sharedDevice) { this.sharedDevice = sharedDevice; }

    public boolean isIdentityLeakReported() { return identityLeakReported; }
    public void setIdentityLeakReported(boolean identityLeakReported) { this.identityLeakReported = identityLeakReported; }

    public String getTopRiskyDomainsJson() { return topRiskyDomainsJson; }
    public void setTopRiskyDomainsJson(String topRiskyDomainsJson) { this.topRiskyDomainsJson = topRiskyDomainsJson; }
}
