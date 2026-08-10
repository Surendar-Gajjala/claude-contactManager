package com.contactmanager.service;

import com.contactmanager.dto.contact.ContactCreateRequest;
import com.contactmanager.dto.contact.ContactResponse;
import com.contactmanager.dto.contact.ContactUpdateRequest;
import com.contactmanager.entity.Contact;
import com.contactmanager.entity.ContactType;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.ResourceNotFoundException;
import com.contactmanager.repository.ContactRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    void createContact_succeeds_whenPersonExists() {
        Person person = Person.builder().id(1L).firstName("Surendar").lastName("Reddy").build();
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(10L);
            return contact;
        });

        ContactCreateRequest request = ContactCreateRequest.builder()
                .personId(1L).phoneNumber("9618443676").contactType(ContactType.PERSONAL).build();

        ContactResponse response = contactService.createContact(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getPersonName()).isEqualTo("Surendar Reddy");
        assertThat(response.getContactType()).isEqualTo(ContactType.PERSONAL);
    }

    @Test
    void createContact_throwsResourceNotFoundException_whenPersonMissing() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());
        ContactCreateRequest request = ContactCreateRequest.builder()
                .personId(1L).phoneNumber("9618443676").contactType(ContactType.PERSONAL).build();

        assertThatThrownBy(() -> contactService.createContact(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getContactById_throwsResourceNotFoundException_whenMissing() {
        when(contactRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getContactById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateContact_throwsResourceNotFoundException_whenContactMissing() {
        when(contactRepository.findById(5L)).thenReturn(Optional.empty());
        ContactUpdateRequest request = ContactUpdateRequest.builder()
                .personId(1L).phoneNumber("9618443676").contactType(ContactType.WORK).build();

        assertThatThrownBy(() -> contactService.updateContact(5L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteContact_deletesWhenFound() {
        Contact contact = Contact.builder().id(5L).build();
        when(contactRepository.findById(5L)).thenReturn(Optional.of(contact));

        contactService.deleteContact(5L);

        verify(contactRepository).delete(contact);
    }
}
