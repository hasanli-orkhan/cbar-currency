# Currency Rates & Currency Converter for CBAR

## Installation

### Maven
For Maven declare a dependency in the <dependencies> section of your POM file.
Dependency declaration in pom.xml
```xml
<dependency>
    <groupId>info.md7</groupId>
    <artifactId>cbar-currency</artifactId>
    <version>1.0</version>
</dependency>
```
### Gradle / Grails
```groovy
compile 'info.md7:cbar-currency:1.0'
```

## Features
* Currency Conversion
    * From AZN
    * To AZN
* Getting actual currency rates from CBAR
    * Actual currency rate for specified currency
    * Actual currency rates for all currencies
    * Currency rate for specified date
    * Currency rates for specified dates    

## Getting started
* Add this library into dependencies
* Use static methods from CurrencyConverter to currency conversion functionality
* Use static methods from CurrencyRate to getting currency rates functionality 

## Code examples

```java
import info.md7.cbar_currency.model.CurrencyCode;
import info.md7.cbar_currency.util.CurrencyRate;
import java.time.LocalDate;

public class Main {

  public static void main(String ... args) {
    CurrencyConverter.convertFromAzn(100d, CurrencyCode.BYN);
    CurrencyConverter.convertToAzn(100d, CurrencyCode.BYN);

    CurrencyRate.getActualCurrencyRates();
    CurrencyRate.getActualCurrencyRate(CurrencyCode.BYN);
    CurrencyRate.getCurrencyRateForDate(CurrencyCode.BYN, LocalDate.of(2020, 11, 5));
    CurrencyRate.getCurrencyRatesForDate(LocalDate.of(2020, 11, 5));
  }

}
``` 