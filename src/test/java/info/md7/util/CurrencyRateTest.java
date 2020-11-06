package info.md7.util;

import info.md7.cbar_currency.exceptions.CurrencyCodeNotFoundException;
import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.exceptions.SpecifiedDateIsAfterException;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import info.md7.cbar_currency.util.CurrencyRate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CurrencyRateTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testParseCurrencies()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method parseCurrencies = CurrencyRate.class
        .getDeclaredMethod("parseCurrencies", LocalDate.class);
    parseCurrencies.setAccessible(true);
    List<Currency> currencies = (List<Currency>) parseCurrencies.invoke(null, LocalDate.now());
    Assert.assertNotNull(currencies);
  }

  @Test
  public void testGetActualCurrencyRate()
      throws CurrencyNotFoundException, CurrencyCodeNotFoundException, IncorrectContentTypeException {
    Assert.assertNotNull(CurrencyRate.getActualCurrencyRate(CurrencyCode.RUB));
  }

  @Test
  public void testGetActualCurrencyRates() throws IncorrectContentTypeException {
    Assert.assertNotNull(CurrencyRate.getActualCurrencyRates());
  }

  @Test
  public void testGetCurrencyRateForDate()
      throws CurrencyCodeNotFoundException, CurrencyNotFoundException, IncorrectContentTypeException, SpecifiedDateIsAfterException {
    Assert
        .assertNotNull(CurrencyRate.getCurrencyRateForDate(CurrencyCode.RUB, LocalDate.of(2020, 11,
            5)));
  }

  @Test
  public void testGetCurrencyRatesForDate()
      throws IncorrectContentTypeException, SpecifiedDateIsAfterException {
    Assert.assertNotNull(CurrencyRate.getCurrencyRatesForDate(LocalDate.of(2020, 11, 5)));
  }

  @Test
  public void testCheckIfContentTypeIsXml()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method checkIfContentTypeIsXml = CurrencyRate.class
        .getDeclaredMethod("checkIfContentTypeIsXml", String.class);
    checkIfContentTypeIsXml.setAccessible(true);
    Assert.assertFalse((boolean) checkIfContentTypeIsXml
        .invoke(null, "https://www.cbar.az/currencies/06.11.1970.xml"));
    Assert.assertTrue((boolean) checkIfContentTypeIsXml
        .invoke(null, "https://www.cbar.az/currencies/05.11.2020.xml"));
  }

  @Test
  public void testCheckIfActualCurrenciesExist()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method checkIfActualCurrenciesExist = CurrencyRate.class
        .getDeclaredMethod("checkIfActualCurrenciesExist", String.class);
    checkIfActualCurrenciesExist.setAccessible(true);
    Assert.assertFalse((boolean) checkIfActualCurrenciesExist
        .invoke(null, "https://www.cbar.az/currencies/05.11.2020.xml"));
  }
}
