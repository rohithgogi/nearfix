package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ServiceDTO;
import com.nearfix.nearfix.entity.Service;
import com.nearfix.nearfix.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public List<ServiceDTO> getAllActiveServices(){
        log.info("Fetching all active service");
        return serviceRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<ServiceDTO> getServicesByCategory(String category){
        log.info("Fetching services by category");
        return serviceRepository.findByCategory(category)
                .stream()
                .filter(Service::getActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public ServiceDTO getServiceById(Long id){
        log.info("Fetching service by ID: {}",id);
        Service service=serviceRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Service not found with ID:"+id));
        return convertToDTO(service);

    }


    private ServiceDTO convertToDTO(Service service) {
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getCategory(),
                service.getDescription(),
                service.getIconEmoji(),
                service.getIconUrl()
        );
    }

}
