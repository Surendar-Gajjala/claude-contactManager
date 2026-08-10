package com.contactmanager.dto.contact;

import com.contactmanager.entity.Contact;
import com.contactmanager.entity.ContactType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContactResponse {

    private Long id;
    private Long personId;
    private String personName;
    private String phoneNumber;
    private ContactType contactType;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static ContactResponse from(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .personId(contact.getPerson().getId())
                .personName(contact.getPerson().getFirstName() + " " + contact.getPerson().getLastName())
                .phoneNumber(contact.getPhoneNumber())
                .contactType(contact.getContactType())
                .createdDate(contact.getCreatedDate())
                .updatedDate(contact.getUpdatedDate())
                .build();
    }
}
