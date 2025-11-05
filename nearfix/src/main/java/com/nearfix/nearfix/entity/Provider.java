package com.nearfix.nearfix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String serviceType;

    private int experienceYears;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus=AvailabilityStatus.OFFLINE;

    private  Boolean verified=false;

}
