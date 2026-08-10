package com.contactmanager.service;

import com.contactmanager.dto.common.PageResponse;
import com.contactmanager.dto.person.PersonCreateRequest;
import com.contactmanager.dto.person.PersonResponse;
import com.contactmanager.dto.person.PersonUpdateRequest;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.DuplicateEmailException;
import com.contactmanager.exception.ResourceNotFoundException;
import com.contactmanager.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional
    public PersonResponse createPerson(PersonCreateRequest request) {
        if (personRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException("A person with email '" + request.getEmail() + "' already exists");
        }
        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();
        return PersonResponse.from(personRepository.save(person));
    }

    @Transactional(readOnly = true)
    public PageResponse<PersonResponse> getPersons(String search, Pageable pageable) {
        Page<Person> page = (search == null || search.isBlank())
                ? personRepository.findAll(pageable)
                : personRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        search, search, pageable);
        return PageResponse.of(page.map(PersonResponse::from));
    }

    @Transactional(readOnly = true)
    public PersonResponse getPersonById(Long id) {
        return PersonResponse.from(findPersonOrThrow(id));
    }

    @Transactional
    public PersonResponse updatePerson(Long id, PersonUpdateRequest request) {
        Person person = findPersonOrThrow(id);
        if (personRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException("A person with email '" + request.getEmail() + "' already exists");
        }
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setGender(request.getGender());
        person.setAddress(request.getAddress());
        return PersonResponse.from(person);
    }

    @Transactional
    public void deletePerson(Long id) {
        Person person = findPersonOrThrow(id);
        personRepository.delete(person);
    }

    private Person findPersonOrThrow(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id " + id));
    }
}
