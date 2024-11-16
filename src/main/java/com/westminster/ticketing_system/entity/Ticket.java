package com.westminster.ticketing_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.westminster.ticketing_system.dtos.TicketDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity class representing a ticket in the system.
 * Contains all ticket-related information including pricing, ownership, and
 * status.
 */
@Entity
@Table(name = "ticket")
@Data
public class Ticket {
    /** Unique identifier for the ticket */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Title of the ticket */
    private String title;

    /** Price of the ticket */
    private BigDecimal price;

    /** Detailed description of the ticket */
    private String description;

    /** Image associated with the ticket */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    /** User who created/owns the ticket */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /** User who purchased the ticket */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    /** Flag indicating if the ticket has been deleted */
    @Column(name = "deleted_ind", nullable = false)
    private boolean deletedInd = false;

    /** Timestamp when the ticket was purchased */
    @Column(name = "purchased_date_time")
    private LocalDateTime purchasedDateTime;

    /** Timestamp when the ticket was last updated */
    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime;

    /** Timestamp when the ticket was created */
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    /**
     * Converts the entity to its DTO representation.
     * 
     * @return TicketDTO containing the ticket's data
     */
    public TicketDTO getDto() {
        TicketDTO ticketDTO = new TicketDTO();

        ticketDTO.setId(id);
        ticketDTO.setTitle(title);
        ticketDTO.setPrice(price);
        ticketDTO.setDescription(description);
        ticketDTO.setReturnedImage(image);
        ticketDTO.setUserId(user.getId());
        ticketDTO.setUsername(user.getUsername());
        ticketDTO.setDeletedInd(deletedInd);

        return ticketDTO;
    }
}
