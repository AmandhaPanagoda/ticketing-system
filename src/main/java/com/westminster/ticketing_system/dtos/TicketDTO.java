package com.westminster.ticketing_system.dtos;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

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
}
