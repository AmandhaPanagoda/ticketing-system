package com.westminster.ticketing_system.dtos;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * Data Transfer Object for Ticket entity.
 * Used for transferring ticket data between layers and handling file uploads.
 */
@Data
public class TicketDTO {
    private int id;
    private String title;
    private BigDecimal price;
    private String description;
    private MultipartFile image;
    private byte[] returnedImage;
    private int userId;
    private String username;
    private boolean deletedInd;
}
