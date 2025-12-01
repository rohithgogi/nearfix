package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.AddProviderServiceRequest;
import com.nearfix.nearfix.dto.ProviderServiceDTO;
import com.nearfix.nearfix.dto.UpdateProviderServiceRequest;
import com.nearfix.nearfix.security.JwtTokenProvider;
import com.nearfix.nearfix.service.ProviderServiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider/services")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("http://localhost:5173")
public class ProviderServiceController {
    private final ProviderServiceService providerServiceService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<ProviderServiceDTO>> getProviderServices(HttpServletRequest request){

        String phoneNumber=getPhoneNumberFromToken(request);
        return ResponseEntity.ok(providerServiceService.getProviderServices(phoneNumber));
    }
    @PostMapping
    public ResponseEntity<ProviderServiceDTO> addProviderService(HttpServletRequest request,
                                                                 @RequestBody @Valid AddProviderServiceRequest serviceRequest){
        try{
            String phoneNumber=getPhoneNumberFromToken(request);
            ProviderServiceDTO result=providerServiceService.addProviderService(phoneNumber,serviceRequest);
            return ResponseEntity.ok(result);
        }catch (Exception e){
            log.error("Error adding service: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProviderServiceDTO> updateProviderService(HttpServletRequest request,
                                                                    @PathVariable Long id,
                                                                    @RequestBody @Valid UpdateProviderServiceRequest serviceRequest){
        try{
            String phoneNumber=getPhoneNumberFromToken(request);
            ProviderServiceDTO result=providerServiceService.updateProviderService(phoneNumber,id,serviceRequest);
            return ResponseEntity.ok(result);
        } catch(Exception e){
            log.error("Error updating service: {}",e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeService(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            String phoneNumber = getPhoneNumberFromToken(request);
            providerServiceService.removeProviderService(phoneNumber, id);
            return ResponseEntity.ok(Map.of("message", "Service removed successfully"));
        } catch (Exception e) {
            log.error("Error removing service: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getPhoneNumberFromToken(HttpServletRequest request){
        String header= request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")){
            String token=header.substring(7);
            return jwtTokenProvider.getPhoneNumberFromToken(token);
        }
        throw new RuntimeException("No authentication token found");
    }

}
