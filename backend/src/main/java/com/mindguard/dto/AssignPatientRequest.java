package com.mindguard.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignPatientRequest {
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
