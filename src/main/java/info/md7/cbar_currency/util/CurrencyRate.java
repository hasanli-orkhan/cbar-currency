package info.md7.cbar_currency.util;

import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.exceptions.SpecifiedDateIsAfterException;
import info.md7.cbar_currency.model.Constants;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyRate {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.y");
  private static final String BASE_URL_OVERRIDE_PROPERTY = "cbar.currency.baseUrl";
  private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
  private static final int READ_TIMEOUT_MILLIS = 10_000;

  static {
    disableSslVerification();
  }

  /**
   * Disable SSL verification for CBAR
   */
  private static void disableSslVerification() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }
          public void checkClientTrusted(X509Certificate[] certs, String authType) {}
          public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
      };
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HostnameVerifier allHostsValid = (hostname, session) -> true;
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      e.printStackTrace();
    }
  }

  /**
   * Seçilmiş valyuta üçün cari məzənnənin alınması
   * Получение актуального курса для выбранной валюты
   * Obtaining the current exchange rate for the selected currency
   *
   * @param currencyCode - CurrencyCode (enum)
   * @return Currency
   * @see Currency
   * @throws CurrencyNotFoundException - Specified currency not found
   * @throws IncorrectContentTypeException - document type is not application/xml
   */
  public static Currency getActualCurrencyRate(CurrencyCode currencyCode)
      throws CurrencyNotFoundException, IncorrectContentTypeException {
    return findCurrency(parseCurrencies(LocalDate.now()), currencyCode);
  }

  /**
   * Cari məzənnələrin alınması
   * Получение актуальных курсов валют
   * Obtaining up-to-date exchange rates
   *
   * @return list of currencies
   * @throws IncorrectContentTypeException - document type is not application/xml
   */
  public static List<Currency> getActualCurrencyRates() throws IncorrectContentTypeException {
    return parseCurrencies(LocalDate.now());
  }

  /**
   * Müəyyən edilmiş tarix üçün seçilmiş valyuta məzənnəsinin alınması
   * Получение курса выбранной валюты для указанной даты
   * Getting the rate of the selected currency for the specified date
   *
   * @param currencyCode - CurrencyCode (enum)
   * @param specifiedDate - specified date (localDate)
   * @return Currency
   * @throws SpecifiedDateIsAfterException - Specified date is after than actual
   * @throws CurrencyNotFoundException - Specified currency not found
   * @throws IncorrectContentTypeException - document type is not application/xml
   */
  public static Currency getCurrencyRateForDate(CurrencyCode currencyCode, LocalDate specifiedDate)
      throws SpecifiedDateIsAfterException,
      CurrencyNotFoundException, IncorrectContentTypeException {
    if (currencyCode == null) {
      throw new CurrencyNotFoundException("Specified currency code not found!");
    }
    if (specifiedDate == null) {
      throw new SpecifiedDateIsAfterException("Specified date is after!");
    }
    if (specifiedDate.isAfter(LocalDate.now())) {
      throw new SpecifiedDateIsAfterException("Specified date is after!");
    }
    return findCurrency(parseCurrencies(specifiedDate), currencyCode);
  }

  /**
   * Qeyd olunmuş tarix üçün məzənnələrin əldə edilməsi
   * Получить список всех курсов валют для указанной даты
   * Obtaining exchange rates for the specified date
   *
   * @param specifiedDate - specified date
   * @return currencies list
   * @throws SpecifiedDateIsAfterException - Specified date is after than actual
   * @throws IncorrectContentTypeException - document type is not application/xml
   */
  public static List<Currency> getCurrencyRatesForDate(LocalDate specifiedDate)
      throws SpecifiedDateIsAfterException, IncorrectContentTypeException {
    if (specifiedDate == null) {
      throw new SpecifiedDateIsAfterException("Specified date is after!");
    }
    if (specifiedDate.isAfter(LocalDate.now())) {
      throw new SpecifiedDateIsAfterException("Specified date is after!");
    }
    return parseCurrencies(specifiedDate);
  }

  /**
   * Qeyd olunan tarixə əsasən bütün valyutala məzənnəsinin alınması
   * Получение списка актуальных курсов валют для выбранной даты
   * Getting a list of current exchange rates for the selected date
   *
   * @param date LocalDate (2020-12-31)
   * @return currencies list
   * @see Currency
   */
  private static List<Currency> parseCurrencies(LocalDate date)
      throws IncorrectContentTypeException {
    if (date == null) {
      throw new IncorrectContentTypeException("Date must not be null");
    }
    String url = buildUrl(date);
    List<Currency> currencies = new ArrayList<>();
    Document document = getParsedDocument(url);
    NodeList nodeList = document.getElementsByTagName("Valute");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        CurrencyCode code = parseCurrencyCode(element.getAttribute("Code"));
        if (code == null) {
          continue;
        }
        String nominal = requiredElementText(element, "Nominal");
        String name = requiredElementText(element, "Name");
        BigDecimal value = parseDecimal(requiredElementText(element, "Value"));
        Currency currency = Currency.builder()
            .code(code)
            .nominal(nominal)
            .name(name)
            .value(value)
            .build();
        currencies.add(currency);
      }
    }
    if (currencies.isEmpty()) {
      throw new IncorrectContentTypeException("No currency data found in XML document");
    }
    return currencies;
  }

  /**
   * Parse w3c document from url
   *
   * @param url - url to be parsed
   * @return Document
   */
  private static Document getParsedDocument(String url) throws IncorrectContentTypeException {
    try {
      URLConnection connection = openConnection(url);
      validateXmlContentType(connection);
      DocumentBuilder documentBuilder = createSecureDocumentBuilder();
      try (InputStream stream = connection.getInputStream()) {
        Document document = documentBuilder.parse(stream);
        document.getDocumentElement().normalize();
        return document;
      }
    } catch (IOException | ParserConfigurationException | SAXException e) {
      throw new IncorrectContentTypeException("Failed to read currency rates XML", e);
    }
  }

  private static URLConnection openConnection(String url) throws IOException {
    URLConnection connection = new URL(url).openConnection();
    connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
    connection.setReadTimeout(READ_TIMEOUT_MILLIS);
    return connection;
  }

  private static void validateXmlContentType(URLConnection connection)
      throws IncorrectContentTypeException {
    String contentType = connection.getContentType();
    if (contentType == null) {
      return;
    }
    String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
    if (!normalizedContentType.contains("xml")) {
      throw new IncorrectContentTypeException(
          "Unexpected content type: " + contentType + ". Expected XML content.");
    }
  }

  private static DocumentBuilder createSecureDocumentBuilder()
      throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    return factory.newDocumentBuilder();
  }

  private static String buildUrl(LocalDate date) {
    return resolveBaseUrl() + date.format(DATE_FORMATTER) + Constants.PAGE_EXTENSION;
  }

  private static String resolveBaseUrl() {
    return System.getProperty(BASE_URL_OVERRIDE_PROPERTY, Constants.BASE_URL);
  }

  private static CurrencyCode parseCurrencyCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      return null;
    }
    try {
      return CurrencyCode.valueOf(code.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static String requiredElementText(Element parent, String tagName)
      throws IncorrectContentTypeException {
    Node node = parent.getElementsByTagName(tagName).item(0);
    if (node == null) {
      throw new IncorrectContentTypeException("Missing required XML field: " + tagName);
    }
    return node.getTextContent();
  }

  private static BigDecimal parseDecimal(String value) throws IncorrectContentTypeException {
    try {
      return new BigDecimal(value.trim().replace(",", "."));
    } catch (NumberFormatException e) {
      throw new IncorrectContentTypeException("Invalid numeric value: " + value, e);
    }
  }

  private static Currency findCurrency(List<Currency> currencies, CurrencyCode currencyCode)
      throws CurrencyNotFoundException {
    if (currencyCode == null) {
      throw new CurrencyNotFoundException("Specified currency code not found!");
    }
    return currencies.stream()
        .filter(currency -> currency.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
  }

}
