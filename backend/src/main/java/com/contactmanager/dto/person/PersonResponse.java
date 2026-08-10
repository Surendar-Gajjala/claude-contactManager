package com.contactmanager.dto.person;

import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PersonResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String address;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static PersonResponse from(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .gender(person.getGender())
                .address(person.getAddress())
                .createdDate(person.getCreatedDate())
                .updatedDate(person.getUpdatedDate())
                .build();
    }
}
