package com.babkamen.ratechange.service;

import com.babkamen.ratechange.domain.Rate;
import com.babkamen.ratechange.domain.RateRepository;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.util.UriTemplate;

import java.time.LocalDate;
import java.util.List;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RateServiceTest {

    @Mock
    RateRepository repository;

    @Captor
    ArgumentCaptor<List<Rate>> captor;

    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().run();
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void downloadRateSuccess() throws Exception {
        String startDate = "2011-08-09";
        String endDate = "2011-08-10";
        String uriTemplate = "http://localhost:"+server.getPort()+"/api/{startDate}/{endDate}";
        String url = new UriTemplate(uriTemplate).expand(startDate, endDate).toString();
        System.out.println("URl="+url);

        whenHttp(server).
                match(get("/api/"+startDate+"/"+endDate)).
                then(resourceContent("sample.xml"), status(HttpStatus.OK_200));

        RateService service = new RateService();
        service.setRepository(repository);

        service.downloadRates(startDate, endDate, uriTemplate);

        Mockito.verify(repository).save(captor.capture());
        List<Rate> value = captor.getValue();

        Rate expectedRate = Rate.builder()
                .date(LocalDate.parse("2011-07-08"))
                .rate(2.7505D)
                .code("USD")
                .build();
        System.out.println("Actual="+value);

        assertEquals(value.size(),1);
        assertEquals(expectedRate,value.get(0));

    }

}