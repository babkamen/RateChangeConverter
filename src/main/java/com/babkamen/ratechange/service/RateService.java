package com.babkamen.ratechange.service;

import com.babkamen.ratechange.domain.Rate;
import com.babkamen.ratechange.domain.RateRepository;
import com.babkamen.ratechange.domain.StaxResponseReader;
import com.babkamen.ratechange.util.LoggingRequestInterceptor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Finds USD rates and downloads data from 3 party API
 */
@Slf4j
@Service
@Setter
public class RateService {
    public static final String EXCHANGE_PRICE_URL_TEMPLATE =
            "http://api.nbp.pl/api/exchangerates/tables/A/{startDate}/{endDate}/?format=xml";
    private static final String CURRENCY_CODE = "USD";
    public static final int BATCH_SIZE = 1000;

    private RestTemplate restTemplate = new RestTemplate();
    private StopWatch stopWatch = new StopWatch();

    @Autowired
    private RateRepository repository;

    @PostConstruct
    public void init() {
        restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
    }


    public Page<Rate> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Rate> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return repository.findByDateBetween(startDate, endDate, pageable);
    }

    public void downloadRates(LocalDate startDate, LocalDate endDate) {
        downloadRates(startDate.toString(), endDate.toString(), EXCHANGE_PRICE_URL_TEMPLATE);
    }


    public void downloadRates(String startDate, String endDate, String uriTemplate) {
        restTemplate.execute(uriTemplate, HttpMethod.GET, null, this::processResponse, startDate, endDate);
    }

    private Void processResponse(ClientHttpResponse response) {

        File tempFile = createAndCopyToTempFile(response);
        try {
            processFile(tempFile);
        }finally {
            tempFile.delete();
        }
        return null;
    }

    private void processFile(File tempFile) {
        stopWatch.start();
        log.debug("Started parsing file");

        try (StaxResponseReader reader =
                     new StaxResponseReader(
                             new FileInputStream(tempFile.getAbsolutePath()))) {
            reader.filterByCurrencyCode(CURRENCY_CODE);

            long count1 = repository.count();
            int ratesCount = processResponse(reader);
            long count2 = repository.count();
            logStats(count1, count2, ratesCount);

        } catch (XMLStreamException e) {
            log.error("Exception while reading xml.{}",e);
        } catch (Exception e) {
            log.error("{}",e);
        }

        tempFile.delete();

        stopWatch.stop();
        log.debug("Finished processing response.It took {}s", stopWatch.getTotalTimeSeconds());
    }

    /**
     * Reads from reader and saves to db.
     * Returns number of read records
     */
    private int processResponse(StaxResponseReader reader) throws XMLStreamException {
        int i=0;
        Rate r;
        List<Rate> rates = new ArrayList<>();
        while ((r = reader.read()) != null) {
            rates.add(r);
            i++;
            if (rates.size() > BATCH_SIZE) {
                repository.save(rates);
                rates.clear();
            }
        }
        repository.save(rates);
        return i;
    }

    private File createAndCopyToTempFile(ClientHttpResponse response) {
        File tempFile;
        try {
            tempFile = File.createTempFile("Rates-", UUID.randomUUID() + ".xml");
            tempFile.deleteOnExit();
            log.debug("Temp file location {}",tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Cannot create temp file.{}", e);
            return null;
        }
        log.debug("Started copying response to file");
        Path path = Paths.get(tempFile.getAbsolutePath());
        try {
            Files.copy(response.getBody(), path, REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Cannot copy file.{}",e);
            tempFile.delete();
            return null;
        }
        return tempFile;
    }

    private void logStats(long countBefore, long countAfter, long resultCount) {
        long diff = countAfter - countBefore;
        log.debug("Inserted {} new values", diff);
        log.debug("Updated {} values", resultCount - diff);
    }

}

