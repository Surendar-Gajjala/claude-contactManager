package com.contactmanager.controller;

import com.contactmanager.dto.common.PageResponse;
import com.contactmanager.dto.excel.ExcelImportResponse;
import com.contactmanager.dto.person.PersonCreateRequest;
import com.contactmanager.dto.person.PersonResponse;
import com.contactmanager.dto.person.PersonUpdateRequest;
import com.contactmanager.service.ExcelImportService;
import com.contactmanager.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final ExcelImportService excelImportService;

    @PostMapping
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PersonResponse>> getPersons(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 6, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(personService.getPersons(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getPersonById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> updatePerson(
            @PathVariable Long id, @Valid @RequestBody PersonUpdateRequest request) {
        return ResponseEntity.ok(personService.updatePerson(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ExcelImportResponse> uploadExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelImportService.importExcel(file));
    }
}
