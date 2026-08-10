package com.contactmanager.controller;

import com.contactmanager.dto.common.PageResponse;
import com.contactmanager.dto.contact.ContactCreateRequest;
import com.contactmanager.dto.contact.ContactResponse;
import com.contactmanager.dto.contact.ContactUpdateRequest;
import com.contactmanager.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createContact(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ContactResponse>> getContacts(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 6, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(contactService.getContacts(search, pageable));
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable Long contactId) {
        return ResponseEntity.ok(contactService.getContactById(contactId));
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<ContactResponse> updateContact(
            @PathVariable Long contactId, @Valid @RequestBody ContactUpdateRequest request) {
        return ResponseEntity.ok(contactService.updateContact(contactId, request));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
