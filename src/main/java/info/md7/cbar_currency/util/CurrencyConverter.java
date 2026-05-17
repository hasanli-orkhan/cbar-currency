package info.md7.cbar_currency.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyConverter {

  private static final Duration CACHE_TTL = Duration.ofHours(12);
  private static final Object CACHE_LOCK = new Object();
  private static volatile Map<CurrencyCode, Currency> currenciesByCode = Collections.emptyMap();
  private static volatile Instant lastCacheUpdate = Instant.EPOCH;

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
    Currency currency = resolveCurrency(currencyCode);
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
    Currency currency = resolveCurrency(currencyCode);
    return currencyValue.multiply(currency.getValue())
        .divide(currency.getNominalInBigDecimal(), RoundingMode.HALF_UP);
  }

  private static Currency resolveCurrency(CurrencyCode currencyCode) throws CurrencyNotFoundException {
    if (currencyCode == null) {
      throw new CurrencyNotFoundException("Specified currency not found!");
    }
    try {
      refreshRatesIfRequired();
    } catch (IncorrectContentTypeException e) {
      throw new CurrencyNotFoundException("Unable to load currency rates", e);
    }

    Currency currency = currenciesByCode.get(currencyCode);
    if (currency == null) {
      throw new CurrencyNotFoundException("Specified currency not found!");
    }
    return currency;
  }

  private static void refreshRatesIfRequired() throws IncorrectContentTypeException {
    if (isCacheActual()) {
      return;
    }

    synchronized (CACHE_LOCK) {
      if (isCacheActual()) {
        return;
      }
      List<Currency> currencies = CurrencyRate.getActualCurrencyRates();
      currenciesByCode = currencies.stream()
          .collect(Collectors.toMap(Currency::getCode, currency -> currency, (left, right) -> left));
      lastCacheUpdate = Instant.now();
    }
  }

  private static boolean isCacheActual() {
    return !currenciesByCode.isEmpty()
        && Duration.between(lastCacheUpdate, Instant.now()).compareTo(CACHE_TTL) < 0;
  }

}
