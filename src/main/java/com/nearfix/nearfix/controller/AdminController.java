package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.AdminDashboardStatsDTO;
import com.nearfix.nearfix.dto.ProviderVerificationDTO;
import com.nearfix.nearfix.dto.UserManagementDTO;
import com.nearfix.nearfix.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats(){
        try{
            log.info("Admin requesting for dashboard stats");
            AdminDashboardStatsDTO stats=adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        }catch(Exception e){
            log.error("Error fetching admin statistics: "+e.getMessage());
            throw new RuntimeException("Failed to fetch statistics: "+e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<ProviderVerificationDTO>> getPendingVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        try{
            log.info("Fetching pending verifications");
            Page<ProviderVerificationDTO> verifications=adminService.getPendingVerifications(page, size);
            return ResponseEntity.ok(verifications);
        }catch(Exception e){
            log.error("Error fetching pending verifications: "+ e.getMessage());
            throw new RuntimeException("Failed to fetch verifications: "+e.getMessage());
        }
    }

    @PutMapping("/providers/{id}/verify")
    public ResponseEntity<ProviderVerificationDTO> verifyProvider(@PathVariable Long id,
                                                                  @RequestBody Map<String,String> body){
        try{
            String adminNotes=body!=null?body.get("notes") : null;
            log.info("Admin verifying provider {} with notes {}",id,adminNotes);
            ProviderVerificationDTO provider = adminService.verifyProvider(id,adminNotes);
            return ResponseEntity.ok(provider);
        }catch(Exception e){
            log.error("Error verifying provider {}: {}",id,e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/providers/{id}/reject")
    public ResponseEntity<ProviderVerificationDTO> rejectProvider(@PathVariable Long id,
                                                                  @RequestBody Map<String,String> body){
        try{
            String reason=body.get("reason");
            if(reason==null || reason.trim().isEmpty()){
                throw new RuntimeException("Rejection reason is required");
            }
            log.info("Admin rejecting provider {} for reason {}",id,reason);
            ProviderVerificationDTO provider=adminService.rejectProvider(id,reason);
            return ResponseEntity.ok(provider);
        }catch(Exception e){
            log.error("Error rejecting provider {}: {}",id,e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserManagementDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {
        try {
            log.info("Admin requesting users - page: {}, size: {}, role: {}, search: {}",
                    page, size, role, search);

            Page<UserManagementDTO> users = adminService.getAllUsers(page, size, role, search);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch users: " + e.getMessage());
        }
    }


    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long id) {
        try {
            log.info("Admin deactivating user: {}", id);
            adminService.deactivateUser(id);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating user {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
