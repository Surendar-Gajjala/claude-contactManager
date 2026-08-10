package com.contactmanager.service;

import com.contactmanager.entity.Contact;
import com.contactmanager.repository.ContactRepository;
import com.contactmanager.repository.PersonRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Excel test #10 (cmsPrompt.txt section 33): the whole import must roll back if persistence
 * fails partway through, even when every row passed validation. Isolated in its own test class
 * because @SpyBean changes the cached Spring context signature.
 *
 * <p>Deliberately NOT class-level {@code @Transactional}: {@code importExcel()}'s own
 * {@code @Transactional} needs to be the top-level transaction so it physically commits or
 * rolls back. If the test wrapped it in an outer transaction too, importExcel's transaction
 * would just participate in it (propagation REQUIRED) and "rollback-only" wouldn't take
 * physical effect until the outer transaction ends — so a post-call count() would still see
 * the uncommitted insert. Cleans up manually in {@link #cleanUp()} instead.
 *
 * <p>The failure is forced on {@code ContactRepository.save} (not {@code PersonRepository.save})
 * because Spring Data repositories are JDK dynamic proxies: Mockito's spy can stub or throw on
 * them, but {@code callRealMethod()} isn't available for a conditional "let the first call
 * through" stub. An unconditional {@code doThrow} avoids needing that.
 */
@SpringBootTest
class ExcelImportRollbackTest {

    private static final String[] HEADERS =
            {"FirstName", "LastName", "Email", "Gender", "Address", "PhoneNumbers", "ContactType"};

    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    private PersonRepository personRepository;

    @SpyBean
    private ContactRepository contactRepository;

    @Test
    void importExcel_rollsBackEverything_whenPersistenceFailsPartway() throws Exception {
        doThrow(new RuntimeException("Simulated failure while saving the contact"))
                .when(contactRepository).save(any(Contact.class));

        MockMultipartFile file = workbook(List.<String[]>of(
                new String[]{"Row1", "Test", "row1@example.com", "MALE", "Addr1", "9618443676", "HOME"}
        ));

        assertThatThrownBy(() -> excelImportService.importExcel(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated failure");

        assertThat(personRepository.count()).isEqualTo(0);
        assertThat(contactRepository.count()).isEqualTo(0);
    }

    @AfterEach
    void cleanUp() {
        contactRepository.deleteAll();
        personRepository.deleteAll();
    }

    private static MockMultipartFile workbook(List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Persons");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
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
