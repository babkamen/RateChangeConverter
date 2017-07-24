RateChangeObserver
------------
Observes USD exchange rates on timeline

Uses [Public NBP API](http://api.nbp.pl/en.html "Public NBP API") to get data.
It took about 0.5hr to process 20gb xml.

[<img src="http://i.imgur.com/IquXRiR.png">](#)


Technologies:
---
**Backend:**
- Java 8
- Lombok
- Maven
- Spring 
- Hibernate
- Jackson
- Thymeleaf
- Apache POI
- Mockito, Restito
- HsqlDb

**Frontend:**
- Bootstrap 3
- Jquery
- DateRangePicker
- Chart js
- Moment js
- Pace js

### How do I run project ###
    mvn clean package
    
And deploy to tomcat
