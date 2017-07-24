package com.babkamen.ratechange.domain;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.time.LocalDate;

/**
 * Reads Rate from provided input. Can handle GBs of data =)
 */
public class StaxResponseReader implements AutoCloseable {
    public static final String CODE = "Code";
    public static final String DATE = "EffectiveDate";
    public static final String RATE_VALUE = "Mid";
    private final XMLStreamReader reader;
    private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private LocalDate date;
    private String code;
    private String filterCurrencyCode;

    public StaxResponseReader(InputStream is) throws XMLStreamException {
        reader = inputFactory.createXMLStreamReader(is);
    }

    public void filterByCurrencyCode(String filterCurrencyCode) {
        this.filterCurrencyCode = filterCurrencyCode;
    }

    public Rate read() throws XMLStreamException {
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                String element = reader.getLocalName();
                if (element.equals(DATE)) {
                    date = LocalDate.parse(readCharacters(reader));
                }
                if (element.equals(CODE)) {
                    code = readCharacters(reader).trim();
                }

                if (element.equals(RATE_VALUE)) {

                    if (filterCurrencyCode!=null) {

                        if(filterCurrencyCode.equals(code)) {
                            return Rate.builder()
                                    .date(date)
                                    .rate(readDouble(reader))
                                    .code(code)
                                    .build();
                        }

                    }else {
                        return Rate.builder()
                                .date(date)
                                .rate(readDouble(reader))
                                .code(code)
                                .build();
                    }
                }
            }
        }
        return null;
    }


    private String readCharacters(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder result = new StringBuilder();
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    result.append(reader.getText());
                    break;
                case XMLStreamReader.END_ELEMENT:
                    return result.toString();
            }
        }
        throw new XMLStreamException("Premature end of file");
    }


    private Double readDouble(XMLStreamReader reader) throws XMLStreamException {
        String characters = readCharacters(reader);
        try {
            return Double.parseDouble(characters);
        } catch (NumberFormatException e) {
            throw new XMLStreamException("Invalid double " + characters);
        }
    }

    @Override
    public void close() throws Exception {
        reader.close();

    }
}