package com.westminster.ticketing_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for SystemConfiguration entity.
 * Used for transferring system configuration settings between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigurationDTO {
    /** Total number of tickets available in the pool */
    private int totalTickets;

    /** Rate at which tickets are released into the pool */
    private int ticketReleaseRate;

    /** Rate at which customers can retrieve tickets */
    private int customerRetrievalRate;

    /** Maximum capacity of tickets allowed in the pool */
    private int maxTicketCapacity;
}
