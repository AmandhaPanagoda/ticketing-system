package com.westminster.ticketing_system.dtos;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO for transferring summary information about tickets between layers.
 * This was created forthe version 1 where users can add multiple tickets for
 * mulitple events.
 * Now, it is used for version 2 where users can add a single ticket for a
 * single event.
 * Many of the fields are not used in version 2, but they are kept for future
 * use.
 */
@Data
public class TicketSummaryDTO {
    private int ticketId;
    private String purchaserUsername;
    private String vendorUsername;
    private LocalDateTime createdDateTime;
    private LocalDateTime purchasedDateTime;
}