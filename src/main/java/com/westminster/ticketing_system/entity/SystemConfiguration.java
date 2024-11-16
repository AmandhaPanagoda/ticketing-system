package com.westminster.ticketing_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing system configuration settings.
 * Stores various system-wide parameters and settings for ticket management.
 */
@Entity
@Table(name = "system_configuration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
    /** Unique identifier for the configuration */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Total number of tickets available in the pool */
    private int totalTickets;

    /** Rate at which tickets are released into the pool */
    private int ticketReleaseRate;

    /** Rate at which customers can retrieve pool */
    private int customerRetrievalRate;

    /** Maximum capacity of tickets allowed in the pool */
    private int maxTicketCapacity;

    /** Timestamp when the configuration was created */
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    /** Timestamp when the configuration was last updated */
    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime;

    /** User who last updated the configuration */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
