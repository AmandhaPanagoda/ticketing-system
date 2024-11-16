package com.westminster.ticketing_system.dtos;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO for transferring summary information about tickets between layers.
 */
@Data
public class TicketSummaryDTO {
    private int ticketId;
    private String purchaserUsername;
    private String vendorUsername;
    private LocalDateTime createdDateTime;
    private LocalDateTime purchasedDateTime;
}