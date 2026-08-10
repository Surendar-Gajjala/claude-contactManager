package com.contactmanager.dto.contact;

import com.contactmanager.entity.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactCreateRequest {

    @NotNull
    private Long personId;

    @NotBlank
    @Pattern(
            regexp = "^[0-9+()\\-\\s]{7,20}$",
            message = "phoneNumber must be 7-20 characters long and contain only digits, spaces, and + ( ) -"
    )
    private String phoneNumber;

    @NotNull
    private ContactType contactType;
}
