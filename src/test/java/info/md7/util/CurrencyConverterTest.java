package info.md7.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.model.CurrencyCode;
import info.md7.cbar_currency.util.CurrencyConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CurrencyConverterTest {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.y");

  @BeforeClass
  public static void setUpFixture() throws IOException {
    Path fixtureDirectory = Files.createTempDirectory("cbar-currency-converter-fixtures");
    System.setProperty("cbar.currency.baseUrl", fixtureDirectory.toUri().toString());
    writeRatesFixture(fixtureDirectory, LocalDate.now());
  }

  @Test
  public void testConvertFromAzn() throws CurrencyNotFoundException {
    BigDecimal result = CurrencyConverter.convertFromAzn(new BigDecimal("100"), CurrencyCode.BYN);
    Assert.assertEquals("172", result.toPlainString());
  }

  @Test
  public void testConvertToAzn() throws CurrencyNotFoundException {
    BigDecimal result = CurrencyConverter.convertToAzn(new BigDecimal("100"), CurrencyCode.BYN);
    Assert.assertEquals("58.2400", result.setScale(4, RoundingMode.HALF_UP).toPlainString());
  }

  private static void writeRatesFixture(Path directory, LocalDate date) throws IOException {
    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<ValCurs Date=\"" + date.format(DATE_FORMATTER) + "\" Name=\"Foreign Currency Rates\">\n"
        + "  <ValType Type=\"Xarici valyutalar\">\n"
        + "    <Valute Code=\"USD\"><Nominal>1</Nominal><Name>US Dollar</Name><Value>1.7000</Value></Valute>\n"
        + "    <Valute Code=\"EUR\"><Nominal>1</Nominal><Name>Euro</Name><Value>1.9900</Value></Valute>\n"
        + "    <Valute Code=\"RUB\"><Nominal>100</Nominal><Name>Russian Ruble</Name><Value>2.2257</Value></Valute>\n"
        + "    <Valute Code=\"BYN\"><Nominal>1</Nominal><Name>Belarus Ruble</Name><Value>0.5824</Value></Valute>\n"
        + "  </ValType>\n"
        + "</ValCurs>\n";
    Files.write(
        directory.resolve(date.format(DATE_FORMATTER) + ".xml"),
        content.getBytes(StandardCharsets.UTF_8));
  }

}
