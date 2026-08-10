package com.contactmanager.service;

import com.contactmanager.dto.person.PersonCreateRequest;
import com.contactmanager.dto.person.PersonResponse;
import com.contactmanager.dto.person.PersonUpdateRequest;
import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.DuplicateEmailException;
import com.contactmanager.exception.ResourceNotFoundException;
import com.contactmanager.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    void createPerson_succeeds_whenEmailIsUnique() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("Surendar").lastName("Reddy").email("surenda@gmail.com")
                .gender(Gender.MALE).address("Hyderabad").build();
        when(personRepository.existsByEmailIgnoreCase("surenda@gmail.com")).thenReturn(false);
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person person = invocation.getArgument(0);
            person.setId(1L);
            return person;
        });

        PersonResponse response = personService.createPerson(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Surendar");
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void createPerson_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("Surendar").lastName("Reddy").email("surenda@gmail.com")
                .gender(Gender.MALE).build();
        when(personRepository.existsByEmailIgnoreCase("surenda@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> personService.createPerson(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(personRepository, never()).save(any());
    }

    @Test
    void getPersonById_throwsResourceNotFoundException_whenMissing() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.getPersonById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePerson_throwsDuplicateEmailException_whenEmailUsedByAnotherPerson() {
        Person existing = Person.builder().id(1L).firstName("A").lastName("B")
                .email("a@example.com").gender(Gender.MALE).build();
        when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personRepository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 1L)).thenReturn(true);

        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("A").lastName("B").email("taken@example.com").gender(Gender.MALE).build();

        assertThatThrownBy(() -> personService.updatePerson(1L, request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void updatePerson_updatesFields_whenEmailIsUnchangedOrFree() {
        Person existing = Person.builder().id(1L).firstName("Old").lastName("Name")
                .email("old@example.com").gender(Gender.MALE).build();
        when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personRepository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 1L)).thenReturn(false);

        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("New").lastName("Name").email("new@example.com")
                .gender(Gender.FEMALE).address("Addr").build();

        PersonResponse response = personService.updatePerson(1L, request);

        assertThat(response.getFirstName()).isEqualTo("New");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void deletePerson_deletesWhenFound() {
        Person existing = Person.builder().id(1L).build();
        when(personRepository.findById(1L)).thenReturn(Optional.of(existing));

        personService.deletePerson(1L);

        verify(personRepository).delete(existing);
    }

    @Test
    void deletePerson_throwsResourceNotFoundException_whenMissing() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.deletePerson(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(personRepository, never()).delete(any());
    }
}
