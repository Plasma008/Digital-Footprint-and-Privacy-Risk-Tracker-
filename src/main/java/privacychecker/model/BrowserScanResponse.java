package privacychecker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class BrowserScanResponse {
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("riskScore")
    private int riskScore;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("riskFactors")
    private List<String> riskFactors;
    
    @JsonProperty("categoryBreakdown")
    private Map<String, Double> categoryBreakdown;
    
    @JsonProperty("topRiskyDomains")
    private List<String> topRiskyDomains;
    
    @JsonProperty("browsingEntropyScore")
    private double browsingEntropyScore;
    
    @JsonProperty("uniqueDomainsScanned")
    private int uniqueDomainsScanned;

    public BrowserScanResponse() {}

    public BrowserScanResponse(String username, int riskScore, String riskLevel, String message, 
                              List<String> riskFactors, Map<String, Double> categoryBreakdown, 
                              List<String> topRiskyDomains, double browsingEntropyScore, 
                              int uniqueDomainsScanned) {
        this.username = username;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.message = message;
        this.riskFactors = riskFactors;
        this.categoryBreakdown = categoryBreakdown;
        this.topRiskyDomains = topRiskyDomains;
        this.browsingEntropyScore = browsingEntropyScore;
        this.uniqueDomainsScanned = uniqueDomainsScanned;
    }

    // Manual Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<String> getTopRiskyDomains() { return topRiskyDomains; }
    public void setTopRiskyDomains(List<String> topRiskyDomains) { this.topRiskyDomains = topRiskyDomains; }

    public double getBrowsingEntropyScore() { return browsingEntropyScore; }
    public void setBrowsingEntropyScore(double browsingEntropyScore) { this.browsingEntropyScore = browsingEntropyScore; }

    public int getUniqueDomainsScanned() { return uniqueDomainsScanned; }
    public void setUniqueDomainsScanned(int uniqueDomainsScanned) { this.uniqueDomainsScanned = uniqueDomainsScanned; }
}
