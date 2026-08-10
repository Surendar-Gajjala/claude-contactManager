package com.contactmanager.service;

import com.contactmanager.dto.excel.ExcelImportResponse;
import com.contactmanager.entity.Contact;
import com.contactmanager.entity.ContactType;
import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.ExcelImportException;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Bulk Person + Contact import from an Excel file (cmsPrompt.txt sections 22-27).
 * Every row is validated before anything is persisted; if any row is invalid, nothing is
 * written. Persistence itself runs in one transaction, so any failure there rolls back the
 * whole import.
 */
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private static final List<String> REQUIRED_COLUMNS = List.of(
            "FirstName", "LastName", "Email", "Gender", "Address", "PhoneNumbers", "ContactType");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private final PersonRepository personRepository;
    private final ContactRepository contactRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public ExcelImportResponse importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExcelImportException("Excel file is required");
        }

        Map<String, Integer> columnIndex;
        List<Map<String, String>> rawRows;
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ExcelImportException("Excel file has no header row");
            }

            columnIndex = readHeader(headerRow);
            List<String> missingColumns = REQUIRED_COLUMNS.stream()
                    .filter(column -> !columnIndex.containsKey(column))
                    .collect(Collectors.toList());
            if (!missingColumns.isEmpty()) {
                throw new ExcelImportException("Missing required column(s): " + String.join(", ", missingColumns));
            }

            rawRows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row, columnIndex)) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (String column : REQUIRED_COLUMNS) {
                    values.put(column, cellValue(row, columnIndex.get(column)));
                }
                rawRows.add(values);
            }
        } catch (IOException e) {
            throw new ExcelImportException("Unable to read the Excel file: " + e.getMessage());
        }

        if (rawRows.isEmpty()) {
            throw new ExcelImportException("Excel file has no data rows");
        }

        List<String> errors = new ArrayList<>();
        List<ParsedRow> parsedRows = new ArrayList<>();
        Set<String> emailsInFile = new HashSet<>();

        for (int i = 0; i < rawRows.size(); i++) {
            int excelRowNumber = i + 2; // row 1 is the header
            List<String> rowErrors = new ArrayList<>();
            ParsedRow parsed = validateRow(rawRows.get(i), emailsInFile, rowErrors);

            if (rowErrors.isEmpty()) {
                parsedRows.add(parsed);
            } else {
                errors.add("Row " + excelRowNumber + ": " + String.join("; ", rowErrors));
            }
        }

        if (!errors.isEmpty()) {
            throw new ExcelImportException("Excel import failed validation", errors);
        }

        int personsCreated = 0;
        int contactsCreated = 0;
        for (ParsedRow row : parsedRows) {
            Person person = Person.builder()
                    .firstName(row.firstName())
                    .lastName(row.lastName())
                    .email(row.email())
                    .gender(row.gender())
                    .address(row.address())
                    .build();
            person = personRepository.save(person);
            personsCreated++;

            for (int i = 0; i < row.phoneNumbers().size(); i++) {
                Contact contact = Contact.builder()
                        .person(person)
                        .phoneNumber(row.phoneNumbers().get(i))
                        .contactType(row.contactTypes().get(i))
                        .build();
                contactRepository.save(contact);
                contactsCreated++;
            }
        }

        return new ExcelImportResponse(personsCreated, contactsCreated);
    }

    private ParsedRow validateRow(Map<String, String> values, Set<String> emailsInFile, List<String> rowErrors) {
        String firstName = values.get("FirstName").trim();
        String lastName = values.get("LastName").trim();
        String email = values.get("Email").trim();
        String genderRaw = values.get("Gender").trim();
        String address = values.get("Address").trim();

        if (firstName.isEmpty()) {
            rowErrors.add("FirstName is required");
        }
        if (lastName.isEmpty()) {
            rowErrors.add("LastName is required");
        }

        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            rowErrors.add("Email is missing or invalid: '" + email + "'");
        } else if (!emailsInFile.add(email.toLowerCase())) {
            rowErrors.add("Duplicate email within the file: '" + email + "'");
        } else if (personRepository.existsByEmailIgnoreCase(email)) {
            rowErrors.add("Email already exists: '" + email + "'");
        }

        Gender gender = null;
        try {
            gender = Gender.valueOf(genderRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            rowErrors.add("Invalid gender: '" + genderRaw + "'");
        }

        List<String> phoneNumbers = splitAndTrim(values.get("PhoneNumbers"));
        List<String> contactTypeTokens = splitAndTrim(values.get("ContactType"));
        List<ContactType> contactTypes = new ArrayList<>();

        if (phoneNumbers.isEmpty()) {
            rowErrors.add("PhoneNumbers is required");
        }
        if (contactTypeTokens.isEmpty()) {
            rowErrors.add("ContactType is required");
        }

        if (!phoneNumbers.isEmpty() && !contactTypeTokens.isEmpty()) {
            if (phoneNumbers.size() != contactTypeTokens.size()) {
                rowErrors.add(String.format(
                        "PhoneNumbers count (%d) does not match ContactType count (%d)",
                        phoneNumbers.size(), contactTypeTokens.size()));
            } else {
                for (String phone : phoneNumbers) {
                    if (!PHONE_PATTERN.matcher(phone).matches()) {
                        rowErrors.add("Invalid phone number: '" + phone + "'");
                    }
                }
                for (String contactTypeToken : contactTypeTokens) {
                    try {
                        contactTypes.add(ContactType.valueOf(contactTypeToken.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        rowErrors.add("Invalid contact type: '" + contactTypeToken + "'");
                    }
                }
            }
        }

        return new ParsedRow(firstName, lastName, email, gender, address.isEmpty() ? null : address,
                phoneNumbers, contactTypes);
    }

    private List<String> splitAndTrim(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> readHeader(Row headerRow) {
        Map<String, Integer> index = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String name = dataFormatter.formatCellValue(cell).trim();
            if (!name.isEmpty()) {
                index.put(name, cell.getColumnIndex());
            }
        }
        return index;
    }

    private boolean isRowBlank(Row row, Map<String, Integer> columnIndex) {
        for (String column : REQUIRED_COLUMNS) {
            if (!cellValue(row, columnIndex.get(column)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String cellValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
    }

    private record ParsedRow(
            String firstName,
            String lastName,
            String email,
            Gender gender,
            String address,
            List<String> phoneNumbers,
            List<ContactType> contactTypes) {
    }
}
