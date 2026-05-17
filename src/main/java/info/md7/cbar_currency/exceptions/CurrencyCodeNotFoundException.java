package info.md7.cbar_currency.exceptions;

/**
 * @deprecated Kept for backward compatibility. Use {@link CurrencyNotFoundException}.
 */
@Deprecated
public class CurrencyCodeNotFoundException extends Exception {

  public CurrencyCodeNotFoundException(String message) {
    super(message);
  }
}
