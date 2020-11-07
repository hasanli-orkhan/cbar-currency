package info.md7.cbar_currency.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyConverter {

  private static List<Currency> currencies;

  static {
    try {
      currencies = CurrencyRate.getActualCurrencyRates();
    } catch (IncorrectContentTypeException e) {
      e.printStackTrace();
    }
  }

  /**
   * AZN-i seçilmiş valyutaya çevirin
   * Сконвертировать AZN в указанную валюту
   * Convert AZN to specified currency
   *
   * @param aznValue Value in AZN (double)
   * @param currencyCode CurrencyCode (enum)
   * @return Double
   * @see CurrencyCode
   * @throws CurrencyNotFoundException - Specified currency not found
   */
  public static Double convertFromAzn(Double aznValue, CurrencyCode currencyCode)
      throws CurrencyNotFoundException {
    Currency currency = currencies.stream()
        .filter(currency1 -> currency1.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
    double result = aznValue * currency.getNominalInDouble() / currency.getValue();
    return round(result, 2);
  }

  /**
   * Seçilmiş valyutanı AZN-ə çevirin
   * Сконвертировать выбранную валюту в AZN
   * Convert specified currency to AZN
   *
   * @param currencyValue currency value (double)
   * @param currencyCode currency code (enum)
   * @return Double
   * @see CurrencyCode
   * @throws CurrencyNotFoundException - Specified currency not found
   */
  public static Double convertToAzn(Double currencyValue, CurrencyCode currencyCode)
      throws CurrencyNotFoundException {
    Currency currency = currencies.stream()
        .filter(currency1 -> currency1.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
    double result = currencyValue * currency.getValue() / currency.getNominalInDouble();
    return round(result, 2);
  }

  /**
   * Round double value to 2 decimals
   *
   * @param value rounded value
   * @param places decimal
   * @throws IllegalArgumentException - Places is < 0
   */
  private static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException("Places is < 0");
    }
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

}
