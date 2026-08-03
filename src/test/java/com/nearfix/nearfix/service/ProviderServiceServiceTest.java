package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.AddProviderServiceRequest;
import com.nearfix.nearfix.dto.ProviderServiceDTO;
import com.nearfix.nearfix.dto.UpdateProviderServiceRequest;
import com.nearfix.nearfix.entity.AvailabilityStatus;
import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.ProviderService;
import com.nearfix.nearfix.entity.User;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ProviderServiceRepository;
import com.nearfix.nearfix.repository.ServiceRepository;
import com.nearfix.nearfix.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceServiceTest {

    @Mock
    private ProviderServiceRepository providerServiceRepository;
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProviderServiceService providerServiceService;

    private User user;
    private Provider provider;
    private com.nearfix.nearfix.entity.Service service;
    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPhoneNumber("9999999999");

        provider = new Provider();
        provider.setId(2L);
        provider.setUser(user);
        provider.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        provider.setVerified(false);

        service = new com.nearfix.nearfix.entity.Service();
        service.setId(3L);
        service.setName("Plumbing");
        service.setIconEmoji("🔧");

        providerService = new ProviderService();
        providerService.setId(4L);
        providerService.setProvider(provider);
        providerService.setService(service);
        providerService.setBasePrice(new BigDecimal("250.00"));
        providerService.setExperienceYears(5);
        providerService.setDescription("General plumbing");
        providerService.setAvailable(true);
    }

    @Test
    void getProviderServices_returnsMappedDtos() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(providerServiceRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId()))
                .thenReturn(List.of(providerService));

        List<ProviderServiceDTO> result = providerServiceService.getProviderServices(user.getPhoneNumber());

        assertEquals(1, result.size());
        assertEquals("Plumbing", result.get(0).getServiceName());
        assertEquals(new BigDecimal("250.00"), result.get(0).getBasePrice());
    }

    @Test
    void addProviderService_createsNewServiceForProvider() {
        AddProviderServiceRequest request = new AddProviderServiceRequest();
        request.setServiceId(3L);
        request.setBasePrice(new BigDecimal("300.00"));
        request.setExperienceYears(6);
        request.setDescription("Emergency plumbing");

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(serviceRepository.findById(request.getServiceId())).thenReturn(Optional.of(service));
        when(providerServiceRepository.existsByProviderIdAndServiceId(provider.getId(), request.getServiceId()))
                .thenReturn(false);
        when(providerServiceRepository.save(any(ProviderService.class))).thenAnswer(invocation -> {
            ProviderService saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ProviderServiceDTO result = providerServiceService.addProviderService(user.getPhoneNumber(), request);

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("300.00"), result.getBasePrice());
        assertTrue(result.getAvailable());
    }

    @Test
    void addProviderService_throwsWhenDuplicate() {
        AddProviderServiceRequest request = new AddProviderServiceRequest();
        request.setServiceId(3L);
        request.setBasePrice(new BigDecimal("300.00"));

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(serviceRepository.findById(request.getServiceId())).thenReturn(Optional.of(service));
        when(providerServiceRepository.existsByProviderIdAndServiceId(provider.getId(), request.getServiceId()))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> providerServiceService.addProviderService(user.getPhoneNumber(), request)
        );

        assertEquals("You already provide this service. Please update instead.", ex.getMessage());
    }

    @Test
    void updateProviderService_updatesProvidedFieldsOnly() {
        UpdateProviderServiceRequest request = new UpdateProviderServiceRequest();
        request.setBasePrice(new BigDecimal("350.00"));
        request.setDescription("Updated description");
        request.setAvailable(false);

        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(providerServiceRepository.findByProviderIdAndId(provider.getId(), providerService.getId()))
                .thenReturn(Optional.of(providerService));
        when(providerServiceRepository.save(any(ProviderService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderServiceDTO result = providerServiceService.updateProviderService(
                user.getPhoneNumber(),
                providerService.getId(),
                request
        );

        assertEquals(new BigDecimal("350.00"), result.getBasePrice());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(5, result.getExperienceYears());
    }

    @Test
    void removeProviderService_deletesOwnedProviderService() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.of(provider));
        when(providerServiceRepository.findByProviderIdAndId(provider.getId(), providerService.getId()))
                .thenReturn(Optional.of(providerService));

        providerServiceService.removeProviderService(user.getPhoneNumber(), providerService.getId());

        verify(providerServiceRepository).delete(providerService);
    }

    @Test
    void findProviderByPhone_createsProviderWhenMissing() {
        when(userRepository.findByPhoneNumber(user.getPhoneNumber())).thenReturn(Optional.of(user));
        when(providerRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider created = invocation.getArgument(0);
            created.setId(99L);
            return created;
        });

        Provider result = providerServiceService.findProviderByPhone(user.getPhoneNumber());

        assertEquals(99L, result.getId());
        assertEquals(AvailabilityStatus.OFFLINE, result.getAvailabilityStatus());
        assertFalse(result.getVerified());
    }
}
