package info.md7.cbar_currency.model;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pojo for Currency
 *
 * @see CurrencyCode (ISO 4217)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

  private static final Pattern NOMINAL_PATTERN = Pattern.compile("\\d+");

  private CurrencyCode code;
  private String nominal;
  private String name;
  private BigDecimal value;

  /**
   * Parse BigDecimal value from String.
   * Used for parsing BigDecimal from strings like 1 t.u
   *
   * @return BigDecimal
   */
  public BigDecimal getNominalInBigDecimal() {
    Matcher matcher = NOMINAL_PATTERN.matcher(nominal == null ? "" : nominal);
    String numericValue = null;
    while (matcher.find()) {
      numericValue = matcher.group();
    }
    if (numericValue == null) {
      throw new IllegalStateException("Unable to parse nominal value: " + nominal);
    }
    return new BigDecimal(numericValue);
  }

}
