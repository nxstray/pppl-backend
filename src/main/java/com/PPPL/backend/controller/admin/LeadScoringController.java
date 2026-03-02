package com.PPPL.backend.controller.admin;

import com.PPPL.backend.config.cache.RateLimiterRedisConfig;
import com.PPPL.backend.data.common.ApiResponse;
import com.PPPL.backend.data.lead.LeadAnalysisDTO;
import com.PPPL.backend.data.lead.LeadScoringResponse;
import com.PPPL.backend.handler.RateLimitExceededException;
import com.PPPL.backend.model.layanan.RequestLayanan;
import com.PPPL.backend.repository.layanan.RequestLayananRepository;
import com.PPPL.backend.security.JwtUtil;
import com.PPPL.backend.service.lead.GeminiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/lead-scoring")
public class LeadScoringController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RequestLayananRepository requestLayananRepository;

    @Autowired
    private RateLimiterRedisConfig rateLimiterRedisConfig;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Check rate limit before processing (Redis-based)
     */
    private void checkRateLimit(Integer adminId) {
        if (!rateLimiterRedisConfig.allowAiRequest(adminId)) {
            long waitForRefill = rateLimiterRedisConfig.getAiTimeUntilReset(adminId);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Maksimal 15 requests per 2 menit. Coba lagi dalam " + waitForRefill + " detik.",
                waitForRefill
            );
        }
    }

    /**
     * Analyze lead using AI (manual trigger oleh admin)
     * Rate Limited: 15 requests per 2 minutes (Redis-based)
     */
    @PostMapping("/analyze/{idRequest}")
    public ResponseEntity<ApiResponse<LeadScoringResponse>> analyzeLead(
            @PathVariable Integer idRequest,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Integer adminId = jwtUtil.getAdminIdFromToken(token);

            // Check rate limit (Redis)
            checkRateLimit(adminId);

            // Validate request exists
            if (!requestLayananRepository.existsById(idRequest)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Request dengan ID " + idRequest + " tidak ditemukan"));
            }

            // Process analysis (will be cache in GeminiService)
            LeadScoringResponse result = geminiService.reAnalyzeLead(idRequest);

            // Get remaining requests (Redis)
            long remaining = rateLimiterRedisConfig.getAiRemainingTokens(adminId);

            String message = "Lead berhasil dianalisa. Remaining requests: " + remaining + "/15";

            return ResponseEntity.ok()
                .header("X-Rate-Limit-Remaining", String.valueOf(remaining))
                .header("X-Rate-Limit-Limit", "15")
                .body(ApiResponse.success(message, result));

        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal menganalisa lead: " + e.getMessage()));
        }
    }

    /**
     * Batch analyze all unanalyzed leads
     * Rate Limited: 15 requests per 2 minutes
     */
    @PostMapping("/analyze-all")
    public ResponseEntity<ApiResponse<BatchAnalysisResult>> analyzeAllPendingLeads(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Integer adminId = jwtUtil.getAdminIdFromToken(token);

            // Get pending leads
            List<RequestLayanan> pendingLeads = requestLayananRepository
                .findAll()
                .stream()
                .filter(r -> r.getAiAnalyzed() == null || !r.getAiAnalyzed())
                .collect(Collectors.toList());

            if (pendingLeads.isEmpty()) {
                return ResponseEntity.ok()
                    .body(ApiResponse.success("Tidak ada lead yang perlu dianalisa", 
                        new BatchAnalysisResult(0, 0, 0, pendingLeads.size())));
            }

            int success = 0;
            int failed = 0;
            int rateLimited = 0;

            for (RequestLayanan lead : pendingLeads) {
                try {
                    // Check rate limit for each iteration
                    checkRateLimit(adminId);

                    geminiService.reAnalyzeLead(lead.getIdRequest());
                    success++;

                } catch (RateLimitExceededException e) {
                    // Stop if rate limit exceeded
                    rateLimited = pendingLeads.size() - success - failed;

                    String message = String.format(
                        "Batch analysis dihentikan karena rate limit. " +
                        "Berhasil: %d, Gagal: %d, Belum diproses: %d. %s",
                        success, failed, rateLimited, e.getMessage()
                    );

                    return ResponseEntity.ok()
                        .body(ApiResponse.success(message, 
                            new BatchAnalysisResult(success, failed, rateLimited, pendingLeads.size())));

                } catch (Exception e) {
                    failed++;
                    System.err.println("Failed to analyze lead " + lead.getIdRequest() + ": " + e.getMessage());
                }
            }

            // All done
            String message = String.format(
                "Batch analysis selesai! Berhasil: %d, Gagal: %d dari %d lead",
                success, failed, pendingLeads.size()
            );

            return ResponseEntity.ok()
                .body(ApiResponse.success(message, 
                    new BatchAnalysisResult(success, failed, 0, pendingLeads.size())));

        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal batch analyze: " + e.getMessage()));
        }
    }

    /**
     * Get all leads with analysis results (for dashboard admin)
     * CACHED: 1 hour
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<LeadAnalysisDTO>>> getAllLeadAnalysis() {
        try {
            List<LeadAnalysisDTO> results = getCachedLeadAnalysis();
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat data leads: " + e.getMessage()));
        }
    }

    @Cacheable(value = "leadScoring", key = "'all_results'")
    public List<LeadAnalysisDTO> getCachedLeadAnalysis() {
        System.out.println("=== CACHE MISS: Fetching all lead analysis from DB ===");

        return requestLayananRepository.findAll()
            .stream()
            .map(this::mapToLeadAnalysisDTO)
            .sorted((a, b) -> {
                if (a.getAiAnalyzed() != b.getAiAnalyzed()) {
                    return a.getAiAnalyzed() ? 1 : -1;
                }
                if (a.getSkorPrioritas() != null && b.getSkorPrioritas() != null) {
                    int priorityCompare = getPriorityOrder(a.getSkorPrioritas()) - 
                                        getPriorityOrder(b.getSkorPrioritas());
                    if (priorityCompare != 0) return priorityCompare;
                }
                return b.getTglRequest().compareTo(a.getTglRequest());
            })
            .collect(Collectors.toList());
    }

    /**
     * Get leads by priority (HOT, WARM, COLD)
     * CACHED per priority
     */
    @GetMapping("/results/priority/{priority}")
    public ResponseEntity<ApiResponse<List<LeadAnalysisDTO>>> getLeadsByPriority(
            @PathVariable String priority) {
        try {
            // Validate priority
            if (!priority.equals("HOT") && !priority.equals("WARM") && !priority.equals("COLD")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Priority harus HOT, WARM, atau COLD"));
            }

            List<LeadAnalysisDTO> results = getCachedLeadsByPriority(priority);
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat data: " + e.getMessage()));
        }
    }

    @Cacheable(value = "leadScoring", key = "'priority_' + #priority")
    public List<LeadAnalysisDTO> getCachedLeadsByPriority(String priority) {
        System.out.println("=== CACHE MISS: Fetching leads with priority " + priority + " ===");

        return requestLayananRepository
            .findAll()
            .stream()
            .filter(r -> priority.equalsIgnoreCase(r.getSkorPrioritas()))
            .map(this::mapToLeadAnalysisDTO)
            .sorted((a, b) -> b.getTglRequest().compareTo(a.getTglRequest()))
            .collect(Collectors.toList());
    }

    /**
     * Get statistics for dashboard
     * CACHED: 5 minutes
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<LeadStatistics>> getLeadStatistics() {
        try {
            LeadStatistics stats = getCachedLeadStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat statistik: " + e.getMessage()));
        }
    }

    @Cacheable(value = "leadStatistics", key = "'stats'")
    public LeadStatistics getCachedLeadStatistics() {
        System.out.println("=== CACHE MISS: Calculating lead statistics ===");

        List<RequestLayanan> allLeads = requestLayananRepository.findAll();

        long totalLeads = allLeads.size();
        long analyzedLeads = allLeads.stream()
            .filter(r -> r.getAiAnalyzed() != null && r.getAiAnalyzed())
            .count();
        long hotLeads = allLeads.stream()
            .filter(r -> "HOT".equals(r.getSkorPrioritas()))
            .count();
        long warmLeads = allLeads.stream()
            .filter(r -> "WARM".equals(r.getSkorPrioritas()))
            .count();
        long coldLeads = allLeads.stream()
            .filter(r -> "COLD".equals(r.getSkorPrioritas()))
            .count();

        return new LeadStatistics(
            totalLeads,
            analyzedLeads,
            hotLeads,
            warmLeads,
            coldLeads
        );
    }

    /**
     * Get rate limit info for current admin
     */
    @GetMapping("/rate-limit-info")
    public ResponseEntity<ApiResponse<RateLimitInfo>> getRateLimitInfo(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Integer adminId = jwtUtil.getAdminIdFromToken(token);

            long remaining = rateLimiterRedisConfig.getAiRemainingTokens(adminId);
            int maxRequests = rateLimiterRedisConfig.getAiMaxRequests();
            long windowMinutes = rateLimiterRedisConfig.getAiWindowSeconds() / 60;

            RateLimitInfo info = new RateLimitInfo(maxRequests, remaining, (int) windowMinutes);
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Gagal memuat rate limit info: " + e.getMessage()));
        }
    }

    // Helper Methods

    /**
     * Map RequestLayanan entity to LeadAnalysisDTO
     */
    private LeadAnalysisDTO mapToLeadAnalysisDTO(RequestLayanan r) {
        LeadAnalysisDTO dto = new LeadAnalysisDTO();
        dto.setIdRequest(r.getIdRequest());
        dto.setNamaKlien(r.getKlien().getNamaKlien());
        dto.setEmailKlien(r.getKlien().getEmailKlien());
        dto.setPerusahaan(r.getPerusahaan());
        dto.setLayanan(r.getLayanan().getNamaLayanan());
        dto.setSkorPrioritas(r.getSkorPrioritas());
        dto.setKategoriLead(r.getKategoriLead());
        dto.setAlasanSkor(r.getAlasanSkor());
        dto.setStatusRequest(r.getStatus().toString());
        dto.setTglRequest(r.getTglRequest());
        dto.setTglAnalisaAi(r.getTglAnalisaAi());
        dto.setAiAnalyzed(r.getAiAnalyzed() != null ? r.getAiAnalyzed() : false);
        return dto;
    }

    /**
     * Get priority order for sorting (HOT=1, WARM=2, COLD=3)
     */
    private int getPriorityOrder(String priority) {
        switch (priority) {
            case "HOT": return 1;
            case "WARM": return 2;
            case "COLD": return 3;
            default: return 4;
        }
    }

    // Inner Classes for Responses

    public static class LeadStatistics {
        public long totalLeads;
        public long analyzedLeads;
        public long hotLeads;
        public long warmLeads;
        public long coldLeads;

        public LeadStatistics(long total, long analyzed, long hot, long warm, long cold) {
            this.totalLeads = total;
            this.analyzedLeads = analyzed;
            this.hotLeads = hot;
            this.warmLeads = warm;
            this.coldLeads = cold;
        }
    }

    public static class RateLimitInfo {
        public int maxRequests;
        public long remainingRequests;
        public int windowMinutes;

        public RateLimitInfo(int max, long remaining, int window) {
            this.maxRequests = max;
            this.remainingRequests = remaining;
            this.windowMinutes = window;
        }
    }

    public static class BatchAnalysisResult {
        public int successCount;
        public int failedCount;
        public int rateLimitedCount;
        public int totalPending;

        public BatchAnalysisResult(int success, int failed, int rateLimited, int total) {
            this.successCount = success;
            this.failedCount = failed;
            this.rateLimitedCount = rateLimited;
            this.totalPending = total;
        }
    }
}