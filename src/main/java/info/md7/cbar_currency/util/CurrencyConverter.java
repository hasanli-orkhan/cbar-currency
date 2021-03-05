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
   * @param aznValue Value in AZN (BigDecimal)
   * @param currencyCode CurrencyCode (enum)
   * @return BigDecimal
   * @see CurrencyCode
   * @throws CurrencyNotFoundException - Specified currency not found
   */
  public static BigDecimal convertFromAzn(BigDecimal aznValue, CurrencyCode currencyCode)
      throws CurrencyNotFoundException {
    Currency currency = currencies.stream()
        .filter(currency1 -> currency1.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
    return aznValue.multiply(currency.getNominalInBigDecimal())
        .divide(currency.getValue(), RoundingMode.HALF_UP);
  }

  /**
   * Seçilmiş valyutanı AZN-ə çevirin
   * Сконвертировать выбранную валюту в AZN
   * Convert specified currency to AZN
   *
   * @param currencyValue currency value (BigDecimal)
   * @param currencyCode currency code (enum)
   * @return BigDecimal
   * @see CurrencyCode
   * @throws CurrencyNotFoundException - Specified currency not found
   */
  public static BigDecimal convertToAzn(BigDecimal currencyValue, CurrencyCode currencyCode)
      throws CurrencyNotFoundException {
    Currency currency = currencies.stream()
        .filter(currency1 -> currency1.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
    return currencyValue.multiply(currency.getValue()).divide(currency.getNominalInBigDecimal(), RoundingMode.HALF_UP);
  }

}
