package info.md7.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.exceptions.SpecifiedDateIsAfterException;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import info.md7.cbar_currency.util.CurrencyRate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CurrencyRateTest {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.y");

  @BeforeClass
  public static void setUpFixture() throws IOException {
    Path fixtureDirectory = Files.createTempDirectory("cbar-currency-test-fixtures");
    System.setProperty("cbar.currency.baseUrl", fixtureDirectory.toUri().toString());

    writeRatesFixture(fixtureDirectory, LocalDate.now(), "2.2257");
    writeRatesFixture(fixtureDirectory, LocalDate.of(2020, 11, 5), "2.2000");
    writeInvalidFixture(fixtureDirectory, LocalDate.of(1970, 11, 6));
  }

  @Test
  public void testGetActualCurrencyRate()
      throws CurrencyNotFoundException, IncorrectContentTypeException {
    Currency currency = CurrencyRate.getActualCurrencyRate(CurrencyCode.RUB);
    Assert.assertNotNull(currency);
    Assert.assertEquals(CurrencyCode.RUB, currency.getCode());
    Assert.assertEquals("2.2257", currency.getValue().toPlainString());
  }

  @Test
  public void testGetActualCurrencyRates() throws IncorrectContentTypeException {
    List<Currency> currencies = CurrencyRate.getActualCurrencyRates();
    Assert.assertNotNull(currencies);
    Assert.assertFalse(currencies.isEmpty());
  }

  @Test
  public void testGetCurrencyRateForDate()
      throws CurrencyNotFoundException, IncorrectContentTypeException, SpecifiedDateIsAfterException {
    Currency currency = CurrencyRate.getCurrencyRateForDate(
        CurrencyCode.RUB, LocalDate.of(2020, 11, 5));
    Assert.assertNotNull(currency);
    Assert.assertEquals("2.2000", currency.getValue().toPlainString());
  }

  @Test
  public void testGetCurrencyRatesForDate()
      throws IncorrectContentTypeException, SpecifiedDateIsAfterException {
    List<Currency> currencies = CurrencyRate.getCurrencyRatesForDate(LocalDate.of(2020, 11, 5));
    Assert.assertNotNull(currencies);
    Assert.assertFalse(currencies.isEmpty());
  }

  @Test(expected = SpecifiedDateIsAfterException.class)
  public void testGetCurrencyRatesForFutureDate()
      throws IncorrectContentTypeException, SpecifiedDateIsAfterException {
    CurrencyRate.getCurrencyRatesForDate(LocalDate.now().plusDays(1));
  }

  @Test(expected = IncorrectContentTypeException.class)
  public void testInvalidXmlResponse() throws IncorrectContentTypeException, SpecifiedDateIsAfterException {
    CurrencyRate.getCurrencyRatesForDate(LocalDate.of(1970, 11, 6));
  }

  private static void writeRatesFixture(Path directory, LocalDate date, String rubValue)
      throws IOException {
    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<ValCurs Date=\"" + date.format(DATE_FORMATTER) + "\" Name=\"Foreign Currency Rates\">\n"
        + "  <ValType Type=\"Xarici valyutalar\">\n"
        + "    <Valute Code=\"USD\"><Nominal>1</Nominal><Name>US Dollar</Name><Value>1.7000</Value></Valute>\n"
        + "    <Valute Code=\"EUR\"><Nominal>1</Nominal><Name>Euro</Name><Value>1.9900</Value></Valute>\n"
        + "    <Valute Code=\"RUB\"><Nominal>100</Nominal><Name>Russian Ruble</Name><Value>" + rubValue + "</Value></Valute>\n"
        + "    <Valute Code=\"BYN\"><Nominal>1</Nominal><Name>Belarus Ruble</Name><Value>0.5824</Value></Valute>\n"
        + "  </ValType>\n"
        + "</ValCurs>\n";
    Files.write(
        directory.resolve(date.format(DATE_FORMATTER) + ".xml"),
        content.getBytes(StandardCharsets.UTF_8));
  }

  private static void writeInvalidFixture(Path directory, LocalDate date) throws IOException {
    String content = "<html><body>not xml rates</body></html>";
    Files.write(
        directory.resolve(date.format(DATE_FORMATTER) + ".xml"),
        content.getBytes(StandardCharsets.UTF_8));
  }
}
