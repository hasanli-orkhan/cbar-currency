package info.md7.cbar_currency.model;

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

  private CurrencyCode code;
  private String nominal;
  private String name;
  private Double value;

  /**
   * Parse Double value from String. Used for parsing double from strings like 1 t.u
   *
   * @return Double
   */
  public Double getNominalInDouble() {
    Pattern pattern = Pattern.compile("\\d+");
    Matcher matcher = pattern.matcher(nominal);
    String doubleValue = "";
    while (matcher.find()) {
      doubleValue = matcher.group();
    }
    return Double.parseDouble(doubleValue);
  }

}
