package com.contactmanager.repository;

import com.contactmanager.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("SELECT c FROM Contact c WHERE "
            + "LOWER(c.person.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(c.person.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Contact> searchByPersonName(@Param("search") String search, Pageable pageable);
}
