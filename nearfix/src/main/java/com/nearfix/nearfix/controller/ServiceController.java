package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.dto.ServiceDTO;
import com.nearfix.nearfix.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174")
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices(){
        return ResponseEntity.ok(serviceService.getAllActiveServices());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ServiceDTO>> getServiceByCategory(String category){
        return ResponseEntity.ok(serviceService.getServicesByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id){
        return  ResponseEntity.ok(serviceService.getServiceById(id));
    }
}
