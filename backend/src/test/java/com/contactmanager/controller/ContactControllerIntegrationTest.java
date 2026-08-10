package com.contactmanager.controller;

import com.contactmanager.dto.contact.ContactCreateRequest;
import com.contactmanager.dto.contact.ContactUpdateRequest;
import com.contactmanager.entity.Contact;
import com.contactmanager.entity.ContactType;
import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContactControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ContactRepository contactRepository;

    private Person person;

    @BeforeEach
    void setUp() {
        person = personRepository.save(Person.builder()
                .firstName("Surendar").lastName("Reddy").email("contact-test@example.com").gender(Gender.MALE).build());
    }

    @Test
    void createContact_returns201() throws Exception {
        ContactCreateRequest request = ContactCreateRequest.builder()
                .personId(person.getId()).phoneNumber("9618443676").contactType(ContactType.PERSONAL).build();

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personName").value("Surendar Reddy"))
                .andExpect(jsonPath("$.contactType").value("PERSONAL"));
    }

    @Test
    void createContact_returns400_whenPhoneNumberInvalid() throws Exception {
        ContactCreateRequest request = ContactCreateRequest.builder()
                .personId(person.getId()).phoneNumber("abc").contactType(ContactType.PERSONAL).build();

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createContact_returns404_whenPersonMissing() throws Exception {
        ContactCreateRequest request = ContactCreateRequest.builder()
                .personId(999L).phoneNumber("9618443676").contactType(ContactType.PERSONAL).build();

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getContacts_searchesByPersonNameAndSupportsPagination() throws Exception {
        contactRepository.save(Contact.builder().person(person).phoneNumber("9618443676").contactType(ContactType.HOME).build());
        contactRepository.save(Contact.builder().person(person).phoneNumber("7893097820").contactType(ContactType.WORK).build());

        mockMvc.perform(get("/api/contacts").param("search", "Surendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].personName").value("Surendar Reddy"));

        mockMvc.perform(get("/api/contacts").param("search", "NoSuchPerson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void updateContact_updatesFields() throws Exception {
        Contact saved = contactRepository.save(
                Contact.builder().person(person).phoneNumber("9618443676").contactType(ContactType.HOME).build());

        ContactUpdateRequest request = ContactUpdateRequest.builder()
                .personId(person.getId()).phoneNumber("7893097820").contactType(ContactType.WORK).build();

        mockMvc.perform(put("/api/contacts/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("7893097820"))
                .andExpect(jsonPath("$.contactType").value("WORK"));
    }

    @Test
    void deleteContact_removesRecord() throws Exception {
        Contact saved = contactRepository.save(
                Contact.builder().person(person).phoneNumber("9618443676").contactType(ContactType.HOME).build());

        mockMvc.perform(delete("/api/contacts/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contacts/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getContactById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/contacts/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
