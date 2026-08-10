package com.contactmanager.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExcelImportResponse {

    private int personsCreated;
    private int contactsCreated;
}
