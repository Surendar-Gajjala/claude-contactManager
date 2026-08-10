package com.contactmanager.service;

import com.contactmanager.dto.excel.ExcelImportResponse;
import com.contactmanager.entity.Contact;
import com.contactmanager.entity.ContactType;
import com.contactmanager.entity.Gender;
import com.contactmanager.entity.Person;
import com.contactmanager.exception.ExcelImportException;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Excel import scenarios from cmsPrompt.txt section 33. Every "rejected" scenario also asserts
 * nothing was persisted, since section 26/27 require an all-or-nothing import.
 */
@SpringBootTest
@Transactional
class ExcelImportServiceTest {

    private static final String[] HEADERS =
            {"FirstName", "LastName", "Email", "Gender", "Address", "PhoneNumbers", "ContactType"};

    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Test
    void importExcel_createsPersonAndContact_forSinglePhoneNumberRow() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "MALE", "Hyderabad", "9618443676", "HOME")));

        ExcelImportResponse response = excelImportService.importExcel(file);

        assertThat(response.getPersonsCreated()).isEqualTo(1);
        assertThat(response.getContactsCreated()).isEqualTo(1);
        assertThat(personRepository.count()).isEqualTo(1);
        Contact contact = contactRepository.findAll().get(0);
        assertThat(contact.getPhoneNumber()).isEqualTo("9618443676");
        assertThat(contact.getContactType()).isEqualTo(ContactType.HOME);
    }

    @Test
    void importExcel_mapsMultiplePhoneNumbersToContactTypesPositionally() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "MALE", "Hyderabad",
                        "9618443676, 7893097820", "HOME, PERSONAL")));

        ExcelImportResponse response = excelImportService.importExcel(file);

        assertThat(response.getPersonsCreated()).isEqualTo(1);
        assertThat(response.getContactsCreated()).isEqualTo(2);

        List<Contact> contacts = contactRepository.findAll();
        Contact home = contacts.stream().filter(c -> c.getContactType() == ContactType.HOME).findFirst().orElseThrow();
        Contact personal = contacts.stream().filter(c -> c.getContactType() == ContactType.PERSONAL).findFirst().orElseThrow();
        assertThat(home.getPhoneNumber()).isEqualTo("9618443676");
        assertThat(personal.getPhoneNumber()).isEqualTo("7893097820");
        assertThat(home.getPerson().getId()).isEqualTo(personal.getPerson().getId());
    }

    @Test
    void importExcel_rejectsRow_whenPhoneAndContactTypeCountsMismatch() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "MALE", "Hyderabad",
                        "9618443676, 7893097820", "HOME")));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class)
                .hasMessageContaining("validation");
        assertThat(personRepository.count()).isEqualTo(0);
        assertThat(contactRepository.count()).isEqualTo(0);
    }

    @Test
    void importExcel_rejectsRow_whenEmailInvalid() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "not-an-email", "MALE", "Hyderabad", "9618443676", "HOME")));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class);
        assertThat(personRepository.count()).isEqualTo(0);
    }

    @Test
    void importExcel_rejectsRow_whenEmailAlreadyExists() throws Exception {
        personRepository.save(Person.builder()
                .firstName("Existing").lastName("Person").email("surenda@gmail.com").gender(Gender.MALE).build());

        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "MALE", "Hyderabad", "9618443676", "HOME")));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class);
        assertThat(personRepository.count()).isEqualTo(1);
        assertThat(contactRepository.count()).isEqualTo(0);
    }

    @Test
    void importExcel_rejectsRow_whenGenderInvalid() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "UNKNOWN", "Hyderabad", "9618443676", "HOME")));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class);
        assertThat(personRepository.count()).isEqualTo(0);
    }

    @Test
    void importExcel_rejectsRow_whenContactTypeInvalid() throws Exception {
        MockMultipartFile file = workbook(List.<String[]>of(
                row("Surendar", "Reddy", "surenda@gmail.com", "MALE", "Hyderabad", "9618443676", "MOBILE")));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class);
        assertThat(personRepository.count()).isEqualTo(0);
    }

    @Test
    void importExcel_rejectsFile_whenRequiredColumnMissing() throws Exception {
        MockMultipartFile file = workbookWithHeaders(
                new String[]{"FirstName", "LastName", "Email", "Gender", "PhoneNumbers", "ContactType"},
                List.<String[]>of(new String[]{"Surendar", "Reddy", "surenda@gmail.com", "MALE", "9618443676", "HOME"}));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class)
                .hasMessageContaining("Address");
    }

    @Test
    void importExcel_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(ExcelImportException.class);
    }

    private static String[] row(String firstName, String lastName, String email, String gender,
                                 String address, String phoneNumbers, String contactType) {
        return new String[]{firstName, lastName, email, gender, address, phoneNumbers, contactType};
    }

    private static MockMultipartFile workbook(List<String[]> rows) throws IOException {
        return workbookWithHeaders(HEADERS, rows);
    }

    private static MockMultipartFile workbookWithHeaders(String[] headers, List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Persons");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (String[] rowValues : rows) {
                Row dataRow = sheet.createRow(rowIndex++);
                for (int i = 0; i < rowValues.length; i++) {
                    dataRow.createCell(i).setCellValue(rowValues[i]);
                }
            }

            workbook.write(out);
            return new MockMultipartFile("file", "import.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }
}
