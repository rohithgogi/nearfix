package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.ProviderDetailDTO;
import com.nearfix.nearfix.dto.ProviderSearchRequest;
import com.nearfix.nearfix.dto.ProviderSearchResultDTO;
import com.nearfix.nearfix.service.ProviderSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5175")
public class SearchController {

    private final ProviderSearchService providerSearchService;

    @PostMapping("/providers")
    public ResponseEntity<List<ProviderSearchResultDTO>> searchProviders(
            @Valid @RequestBody ProviderSearchRequest request) {
        try {
            log.info("Search request received: {}", request);
            List<ProviderSearchResultDTO> results = providerSearchService.searchProviders(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching providers: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching providers: " + e.getMessage());
        }
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<ProviderDetailDTO> getProviderDetail(@PathVariable Long id) {
        try {
            log.info("Fetching provider detail for ID: {}", id);
            ProviderDetailDTO detail = providerSearchService.getProviderDetail(id);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("Error fetching provider detail: {}", e.getMessage(), e);
            throw new RuntimeException("Provider not found");
        }
    }
}