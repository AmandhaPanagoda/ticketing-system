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
    /** Ticket's unique identifier */
    private int id;

    /** Title of the ticket */
    private String title;

    /** Price of the ticket */
    private BigDecimal price;

    /** Detailed description of the ticket */
    private String description;

    /** Uploaded image file for the ticket */
    private MultipartFile image;

    /** Image data for displaying the ticket */
    private byte[] returnedImage;

    /** ID of the user who owns the ticket */
    private int userId;

    /** Username of the ticket owner */
    private String username;

    /** Flag indicating if the ticket has been deleted */
    private boolean deletedInd;
}
