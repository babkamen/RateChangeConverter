package com.babkamen.ratechange.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;


public interface RateRepository extends PagingAndSortingRepository<Rate,LocalDate> {

    Page<Rate> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
