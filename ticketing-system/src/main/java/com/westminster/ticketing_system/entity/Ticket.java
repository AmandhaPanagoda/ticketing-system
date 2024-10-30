package com.westminster.ticketing_system.entity;

import java.math.BigDecimal;

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

    public TicketDTO getDto() {
        TicketDTO ticketDTO = new TicketDTO();

        ticketDTO.setId(id);
        ticketDTO.setTitle(title);
        ticketDTO.setPrice(price);
        ticketDTO.setDescription(description);
        ticketDTO.setReturnedImage(image);
        ticketDTO.setUserId(user.getId());
        ticketDTO.setUsername(user.getUsername());

        return ticketDTO;
    }
}
