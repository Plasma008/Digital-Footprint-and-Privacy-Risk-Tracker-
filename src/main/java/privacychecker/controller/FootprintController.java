package privacychecker.controller;

import java.util.List;
import privacychecker.model.BrowserScanRequest;
import privacychecker.model.BrowserScanResponse;
import privacychecker.model.BrowserSessionEntity;
import privacychecker.service.BrowserRiskScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/footprint")
public class FootprintController {

    private final BrowserRiskScoringService  browserRiskScoringService;

    public FootprintController(BrowserRiskScoringService browserRiskScoringService) {
        this.browserRiskScoringService = browserRiskScoringService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Privacy Checker AI is live!");
    }

    // --- Unified AI Analysis ---
    @PostMapping("/browser-analyze")
    public ResponseEntity<BrowserScanResponse> browserAnalyze(@RequestBody BrowserScanRequest request) {
        return ResponseEntity.ok(browserRiskScoringService.analyze(request));
    }

    // --- History ---
    @GetMapping("/browser-history/{username}")
    public List<BrowserSessionEntity> getBrowserHistory(@PathVariable String username) {
        return browserRiskScoringService.getBrowserHistory(username);
    }
}