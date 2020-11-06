package info.md7.cbar_currency.util;

import info.md7.cbar_currency.exceptions.CurrencyCodeNotFoundException;
import info.md7.cbar_currency.exceptions.CurrencyNotFoundException;
import info.md7.cbar_currency.exceptions.IncorrectContentTypeException;
import info.md7.cbar_currency.exceptions.SpecifiedDateIsAfterException;
import info.md7.cbar_currency.model.Constants;
import info.md7.cbar_currency.model.Currency;
import info.md7.cbar_currency.model.CurrencyCode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  /**
   * Seçilmiş valyuta üçün cari məzənnənin alınması
   * Получение актуального курса для выбранной валюты
   * Obtaining the current exchange rate for the selected currency
   *
   * @return Currency
   * @see Currency
   */
  public static Currency getActualCurrencyRate(CurrencyCode currencyCode)
      throws CurrencyNotFoundException, CurrencyCodeNotFoundException, IncorrectContentTypeException {
    Arrays.stream(CurrencyCode.values()).filter(currencyCode1 -> currencyCode == currencyCode1)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency code not found!"));
    List<Currency> currencies = parseCurrencies(LocalDate.now());
    return currencies.stream().filter(currency -> currency.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
  }

  /**
   * Cari məzənnələrin alınması
   * Получение актуальных курсов валют
   * Obtaining up-to-date exchange rates
   *
   * @return List\<Currency\>
   */
  public static List<Currency> getActualCurrencyRates() throws IncorrectContentTypeException {
    return parseCurrencies(LocalDate.now());
  }

  /**
   * Müəyyən edilmiş tarix üçün seçilmiş valyuta məzənnəsinin alınması
   * Получение курса выбранной валюты для указанной даты
   * Getting the rate of the selected currency for the specified date
   *
   * @return Currency
   */
  public static Currency getCurrencyRateForDate(CurrencyCode currencyCode, LocalDate specifiedDate)
      throws CurrencyCodeNotFoundException, SpecifiedDateIsAfterException,
      CurrencyNotFoundException, IncorrectContentTypeException {
    Arrays.stream(CurrencyCode.values()).filter(currencyCode1 -> currencyCode == currencyCode1)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency code not found!"));
    if (specifiedDate.isAfter(LocalDate.now())) {
      throw new SpecifiedDateIsAfterException("Specified date is after!");
    }
    List<Currency> currencies = parseCurrencies(specifiedDate);
    return currencies.stream().filter(currency -> currency.getCode() == currencyCode)
        .findFirst()
        .orElseThrow(() -> new CurrencyNotFoundException("Specified currency not found!"));
  }

  /**
   * Qeyd olunmuş tarix üçün məzənnələrin əldə edilməsi
   * Получить список всех курсов валют для указанной даты
   * Obtaining exchange rates for the specified date
   *
   * @return List\<Currency\>
   */
  public static List<Currency> getCurrencyRatesForDate(LocalDate specifiedDate)
      throws SpecifiedDateIsAfterException, IncorrectContentTypeException {
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
   * @return List\<Currency\> currencies
   * @see Currency
   */
  private static List<Currency> parseCurrencies(LocalDate date)
      throws IncorrectContentTypeException {
    String formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.y"));
    String url = Constants.BASE_URL + formattedDate + Constants.PAGE_EXTENSION;
    if (!checkIfContentTypeIsXml(url)) {
      throw new IncorrectContentTypeException("Content-Type is not application/xml");
    }
    List<Currency> currencies = new ArrayList<>();
    Document document = getParsedDocument(url);
    NodeList nodeList = document.getElementsByTagName("Valute");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        String code = element.getAttribute("Code");
        String nominal = element.getElementsByTagName("Nominal").item(0).getTextContent();
        String name = element.getElementsByTagName("Name").item(0).getTextContent();
        Double value = Double
            .valueOf(element.getElementsByTagName("Value").item(0).getTextContent());
        Currency currency = Currency.builder()
            .code(CurrencyCode.valueOf(code))
            .nominal(nominal)
            .name(name)
            .value(value)
            .build();
        currencies.add(currency);
      }
    }
    return currencies;
  }

  /**
   * Check if the Content-Type of the document is application/xml.
   *
   * @return boolean
   */
  private static boolean checkIfContentTypeIsXml(String specifiedUrl) {
    URL url = null;
    try {
      url = new URL(specifiedUrl);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    URLConnection urlConnection = null;
    try {
      urlConnection = url.openConnection();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return urlConnection.getContentType().equals("application/xml");
  }

  /**
   * Check if the given xml document is up-to-date
   */
  private static boolean checkIfActualCurrenciesExist(String url) {
    Document document = getParsedDocument(url);
    Node node = document.getElementsByTagName("ValCurs").item(0);
    Element element = (Element) node;
    String date = element.getAttribute("Date");
    LocalDate documentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.y"));
    return documentDate.isEqual(LocalDate.now());
  }

  /**
   * Parse w3c document from url
   *
   * @return Document
   */
  private static Document getParsedDocument(String url) {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = null;
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    Document document = null;
    try {
      document = documentBuilder.parse(new URL(url).openStream());
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    document.getDocumentElement().normalize();
    return document;
  }

}
