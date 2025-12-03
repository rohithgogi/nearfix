package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.AddProviderServiceRequest;
import com.nearfix.nearfix.dto.ProviderServiceDTO;
import com.nearfix.nearfix.dto.UpdateProviderServiceRequest;
import com.nearfix.nearfix.entity.*;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ProviderServiceRepository;
import com.nearfix.nearfix.repository.ServiceRepository;
import com.nearfix.nearfix.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Builder
public class ProviderServiceService {
    private final ProviderServiceRepository providerServiceRepository;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public List<ProviderServiceDTO> getProviderServices(String phoneNumber){
        log.info("Fetching services for provider: {}",phoneNumber);
        Provider provider=findProviderByPhone(phoneNumber);
        return providerServiceRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public  ProviderServiceDTO addProviderService(String phoneNumber, AddProviderServiceRequest request){
        log.info("Adding service {} for provider: {}",request.getServiceId(),phoneNumber);
        Provider provider=findProviderByPhone(phoneNumber);

        Service service=serviceRepository.findById(request.getServiceId())
                .orElseThrow(()-> new RuntimeException("Service not found"));

        if(providerServiceRepository.existsByProviderIdAndServiceId(provider.getId(),request.getServiceId())){
            throw new RuntimeException("You already provide this service. Please update instead.");

        }

        ProviderService providerService=new ProviderService();
        providerService.setProvider(provider);
        providerService.setService(service);
        providerService.setBasePrice(request.getBasePrice());
        providerService.setDescription(request.getDescription());
        providerService.setExperienceYears(request.getExperienceYears());
        providerService.setAvailable(true);

        providerService=providerServiceRepository.save(providerService);
        log.info("Service added successfully with id: {}", providerService.getId());

        return convertToDTO(providerService);

    }

    @Transactional
    public ProviderServiceDTO updateProviderService(String phoneNumber, Long providerServiceId,
                                                    UpdateProviderServiceRequest request) {
        log.info("Updating provider service {} for provider: {}", providerServiceId, phoneNumber);

        Provider provider = findProviderByPhone(phoneNumber);

        // Find provider service and validate ownership
        ProviderService providerService = providerServiceRepository
                .findByProviderIdAndId(provider.getId(), providerServiceId)
                .orElseThrow(() -> new RuntimeException("Provider service not found or you don't have permission"));

        // Update fields
        if (request.getBasePrice() != null) {
            providerService.setBasePrice(request.getBasePrice());
        }
        if (request.getExperienceYears() != null) {
            providerService.setExperienceYears(request.getExperienceYears());
        }
        if (request.getDescription() != null) {
            providerService.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            providerService.setAvailable(request.getAvailable());
        }

        providerService = providerServiceRepository.save(providerService);
        log.info("Provider service updated successfully");

        return convertToDTO(providerService);
    }

    @Transactional
    public void removeProviderService(String phoneNumber, Long providerServiceId) {
        log.info("Removing provider service {} for provider: {}", providerServiceId, phoneNumber);

        Provider provider = findProviderByPhone(phoneNumber);

        // Find provider service and validate ownership
        ProviderService providerService = providerServiceRepository
                .findByProviderIdAndId(provider.getId(), providerServiceId)
                .orElseThrow(() -> new RuntimeException("Provider service not found or you don't have permission"));

        providerServiceRepository.delete(providerService);
        log.info("Provider service removed successfully");
    }


    public Provider findProviderByPhone(String phoneNumber){
        User user=userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()-> new RuntimeException("User not found"));

        return providerRepository.findByUserId(user.getId())
                .orElseGet(()->{
                    log.info("Creating new Provider record for user: {}", phoneNumber);
                    Provider newProvider=new Provider();
                    newProvider.setUser(user);
                    newProvider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
                    newProvider.setVerified(false);
                    Provider saved = providerRepository.save(newProvider);
                    log.info("Provider created with ID: {}", saved.getId());
                    return saved;
                });
    }

    private ProviderServiceDTO convertToDTO(ProviderService ps){
        return new ProviderServiceDTO(
                ps.getId(),
                ps.getService().getId(),
                ps.getService().getName(),
                ps.getService().getIconEmoji(),
                ps.getBasePrice(),
                ps.getExperienceYears(),
                ps.getDescription(),
                ps.getAvailable(),
                ps.getCreatedAt()
        );
    }

}
