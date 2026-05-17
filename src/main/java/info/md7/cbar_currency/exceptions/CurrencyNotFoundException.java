package info.md7.cbar_currency.exceptions;

public class CurrencyNotFoundException extends Exception {

  public CurrencyNotFoundException(String message) {
    super(message);
  }

  public CurrencyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
