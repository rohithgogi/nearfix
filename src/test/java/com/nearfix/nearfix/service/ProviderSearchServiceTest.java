package com.nearfix.nearfix.service;

import com.nearfix.nearfix.dto.ProviderDetailDTO;
import com.nearfix.nearfix.dto.ProviderSearchRequest;
import com.nearfix.nearfix.dto.ProviderSearchResultDTO;
import com.nearfix.nearfix.entity.Provider;
import com.nearfix.nearfix.entity.ProviderService;
import com.nearfix.nearfix.repository.ProviderRepository;
import com.nearfix.nearfix.repository.ProviderServiceRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderSearchServiceTest {

    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private ProviderServiceRepository providerServiceRepository;

    @InjectMocks
    private ProviderSearchService providerSearchService;

    private Provider providerA;
    private Provider providerB;
    private ProviderService providerAService;
    private ProviderService providerBService;

    @BeforeEach
    void setUp() {
        com.nearfix.nearfix.entity.Service service = new com.nearfix.nearfix.entity.Service();
        service.setId(11L);
        service.setName("Plumbing");
        service.setIconEmoji("🔧");

        providerA = new Provider();
        providerA.setId(1L);
        providerA.setBusinessName("A Fix");
        providerA.setLatitude(new BigDecimal("28.6139"));
        providerA.setLongitude(new BigDecimal("77.2090"));
        providerA.setRating(new BigDecimal("4.5"));
        providerA.setTotalReviews(12);
        providerA.setAddress("Addr A");
        providerA.setCity("Delhi");
        providerA.setExperienceYears(8);
        providerA.setVerified(true);

        providerB = new Provider();
        providerB.setId(2L);
        providerB.setBusinessName("B Fix");
        providerB.setLatitude(new BigDecimal("28.7041"));
        providerB.setLongitude(new BigDecimal("77.1025"));
        providerB.setRating(new BigDecimal("4.9"));
        providerB.setTotalReviews(35);
        providerB.setAddress("Addr B");
        providerB.setCity("Delhi");
        providerB.setExperienceYears(10);
        providerB.setVerified(true);

        providerAService = new ProviderService();
        providerAService.setProvider(providerA);
        providerAService.setService(service);
        providerAService.setBasePrice(new BigDecimal("300"));
        providerAService.setAvailable(true);

        providerBService = new ProviderService();
        providerBService.setProvider(providerB);
        providerBService.setService(service);
        providerBService.setBasePrice(new BigDecimal("500"));
        providerBService.setAvailable(true);
    }

    @Test
    void searchProviders_filtersByMinRatingAndSortsByPriceDesc() {
        ProviderSearchRequest request = new ProviderSearchRequest();
        request.setServiceId(11L);
        request.setLatitude(28.60);
        request.setLongitude(77.20);
        request.setMinRating(4.6);
        request.setSortBy("price");
        request.setSortOrder("desc");

        when(providerRepository.findProvidersByService(28.60, 77.20, 11L))
                .thenReturn(List.of(providerA, providerB));
        when(providerServiceRepository.findByProviderIdAndAvailableTrue(providerA.getId()))
                .thenReturn(List.of(providerAService));
        when(providerServiceRepository.findByProviderIdAndAvailableTrue(providerB.getId()))
                .thenReturn(List.of(providerBService));

        List<ProviderSearchResultDTO> result = providerSearchService.searchProviders(request);

        assertEquals(1, result.size());
        assertEquals(providerB.getId(), result.get(0).getProviderId());
        assertEquals(new BigDecimal("500"), result.get(0).getStartingPrice());
    }

    @Test
    void searchProviders_throwsWhenServiceIdMissing() {
        ProviderSearchRequest request = new ProviderSearchRequest();
        request.setLatitude(28.60);
        request.setLongitude(77.20);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> providerSearchService.searchProviders(request)
        );

        assertTrue(ex.getMessage().contains("Service ID is required"));
    }

    @Test
    void getProviderDetail_returnsMappedProviderDetails() {
        providerA.setPincode("110001");
        providerA.setBio("Experienced provider");
        providerA.setWorkingHours("{\"mon\":\"9-6\"}");

        when(providerRepository.findById(providerA.getId())).thenReturn(Optional.of(providerA));
        when(providerServiceRepository.findByProviderIdAndAvailableTrue(providerA.getId()))
                .thenReturn(List.of(providerAService));

        ProviderDetailDTO result = providerSearchService.getProviderDetail(providerA.getId());

        assertEquals(providerA.getId(), result.getProviderId());
        assertEquals("A Fix", result.getBusinessName());
        assertEquals(1, result.getServices().size());
        assertEquals("Plumbing", result.getServices().get(0).getServiceName());
    }

    @Test
    void getProviderDetail_throwsWhenProviderMissing() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> providerSearchService.getProviderDetail(999L)
        );

        assertEquals("Provider not found", ex.getMessage());
    }
}