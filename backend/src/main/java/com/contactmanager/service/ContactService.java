package com.contactmanager.service;

import com.contactmanager.dto.common.PageResponse;
import com.contactmanager.dto.contact.ContactCreateRequest;
import com.contactmanager.dto.contact.ContactResponse;
import com.contactmanager.dto.contact.ContactUpdateRequest;
import com.contactmanager.entity.Contact;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.ResourceNotFoundException;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final PersonRepository personRepository;

    @Transactional
    public ContactResponse createContact(ContactCreateRequest request) {
        Person person = findPersonOrThrow(request.getPersonId());
        Contact contact = Contact.builder()
                .person(person)
                .phoneNumber(request.getPhoneNumber())
                .contactType(request.getContactType())
                .build();
        return ContactResponse.from(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactResponse> getContacts(String search, Pageable pageable) {
        Page<Contact> page = (search == null || search.isBlank())
                ? contactRepository.findAll(pageable)
                : contactRepository.searchByPersonName(search, pageable);
        return PageResponse.of(page.map(ContactResponse::from));
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id) {
        return ContactResponse.from(findContactOrThrow(id));
    }

    @Transactional
    public ContactResponse updateContact(Long id, ContactUpdateRequest request) {
        Contact contact = findContactOrThrow(id);
        Person person = findPersonOrThrow(request.getPersonId());
        contact.setPerson(person);
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setContactType(request.getContactType());
        return ContactResponse.from(contact);
    }

    @Transactional
    public void deleteContact(Long id) {
        Contact contact = findContactOrThrow(id);
        contactRepository.delete(contact);
    }

    private Contact findContactOrThrow(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));
    }

    private Person findPersonOrThrow(Long personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id " + personId));
    }
}
