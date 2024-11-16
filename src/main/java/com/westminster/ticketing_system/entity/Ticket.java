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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private BigDecimal price;
    private String description;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    @Column(name = "deleted_ind", nullable = false)
    private boolean deletedInd = false;

    @Column(name = "purchased_date_time")
    private LocalDateTime purchasedDateTime;

    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime;

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
