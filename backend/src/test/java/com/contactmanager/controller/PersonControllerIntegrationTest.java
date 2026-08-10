package com.contactmanager.controller;

import com.contactmanager.dto.contact.ContactCreateRequest;
import com.contactmanager.dto.person.PersonCreateRequest;
import com.contactmanager.dto.person.PersonUpdateRequest;
import com.contactmanager.entity.ContactType;
import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Test
    void createPerson_returns201_andPersistsPerson() throws Exception {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("Surendar").lastName("Reddy").email("surenda@gmail.com")
                .gender(Gender.MALE).address("Hyderabad").build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Surendar"));

        assertThat(personRepository.count()).isEqualTo(1);
    }

    @Test
    void createPerson_returns409_whenEmailAlreadyExists() throws Exception {
        personRepository.save(Person.builder()
                .firstName("A").lastName("B").email("dup@example.com").gender(Gender.MALE).build());

        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("C").lastName("D").email("dup@example.com").gender(Gender.FEMALE).build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createPerson_returns400_whenValidationFails() throws Exception {
        String invalidJson = "{\"firstName\":\"\",\"lastName\":\"Reddy\",\"email\":\"not-an-email\"}";

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getPersons_supportsPaginationSortingAndSearch() throws Exception {
        personRepository.save(Person.builder().firstName("Surendar").lastName("Reddy").email("s1@example.com").gender(Gender.MALE).build());
        personRepository.save(Person.builder().firstName("Anith").lastName("Reddy").email("s2@example.com").gender(Gender.MALE).build());
        personRepository.save(Person.builder().firstName("GSR").lastName("Rdyy").email("s3@example.com").gender(Gender.MALE).build());

        mockMvc.perform(get("/api/persons").param("page", "0").param("size", "2").param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].firstName").value("Anith"));

        mockMvc.perform(get("/api/persons").param("search", "Surendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Surendar"));
    }

    @Test
    void getPersonById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePerson_updatesFields() throws Exception {
        Person saved = personRepository.save(Person.builder()
                .firstName("Old").lastName("Name").email("old@example.com").gender(Gender.MALE).build());

        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("New").lastName("Name").email("new@example.com")
                .gender(Gender.FEMALE).address("Addr").build();

        mockMvc.perform(put("/api/persons/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));
    }

    @Test
    void deletePerson_returns404OnRefetch_andCascadesToContacts() throws Exception {
        Person saved = personRepository.save(Person.builder()
                .firstName("Surendar").lastName("Reddy").email("cascade@example.com").gender(Gender.MALE).build());

        ContactCreateRequest contactRequest = ContactCreateRequest.builder()
                .personId(saved.getId()).phoneNumber("9618443676").contactType(ContactType.HOME).build();
        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactRequest)))
                .andExpect(status().isCreated());

        assertThat(contactRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/api/persons/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/persons/{id}", saved.getId()))
                .andExpect(status().isNotFound());
        assertThat(contactRepository.count()).isEqualTo(0);
    }
}
