package info.md7.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.model.CurrencyCode;
import info.md7.cbar_currency.util.CurrencyConverter;
import org.junit.Assert;
import org.junit.Test;

public class CurrencyConverterTest {

  @Test
  public void testConvertFromAzn() throws CurrencyNotFoundException {
    Assert.assertNotNull(CurrencyConverter.convertFromAzn(100d, CurrencyCode.BYN));
  }

  @Test
  public void testConvertToAzn() throws CurrencyNotFoundException {
    Assert.assertNotNull(CurrencyConverter.convertToAzn(100d, CurrencyCode.BYN));
  }

}
