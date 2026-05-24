package privacychecker.model;

import java.util.List;
import java.util.Map;

public class BrowserScanRequest {
    private String username;
    
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

    // --- Behavioral Signals ---
    private List<String> visitedDomains;
    private int openTabsCount;
    private int totalHistoryItems;
    private int scanWindowDays;
    private Map<String, Long> timeSpentPerDomain;
    private int detectedTrackersCount;
    private int searchFrequency;
    private int insecureSiteCount;

    public BrowserScanRequest() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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

    public List<String> getVisitedDomains() { return visitedDomains; }
    public void setVisitedDomains(List<String> visitedDomains) { this.visitedDomains = visitedDomains; }

    public int getOpenTabsCount() { return openTabsCount; }
    public void setOpenTabsCount(int openTabsCount) { this.openTabsCount = openTabsCount; }

    public int getTotalHistoryItems() { return totalHistoryItems; }
    public void setTotalHistoryItems(int totalHistoryItems) { this.totalHistoryItems = totalHistoryItems; }

    public int getScanWindowDays() { return scanWindowDays; }
    public void setScanWindowDays(int scanWindowDays) { this.scanWindowDays = scanWindowDays; }

    public Map<String, Long> getTimeSpentPerDomain() { return timeSpentPerDomain; }
    public void setTimeSpentPerDomain(Map<String, Long> timeSpentPerDomain) { this.timeSpentPerDomain = timeSpentPerDomain; }

    public int getDetectedTrackersCount() { return detectedTrackersCount; }
    public void setDetectedTrackersCount(int detectedTrackersCount) { this.detectedTrackersCount = detectedTrackersCount; }

    public int getSearchFrequency() { return searchFrequency; }
    public void setSearchFrequency(int searchFrequency) { this.searchFrequency = searchFrequency; }

    public int getInsecureSiteCount() { return insecureSiteCount; }
    public void setInsecureSiteCount(int insecureSiteCount) { this.insecureSiteCount = insecureSiteCount; }
}
