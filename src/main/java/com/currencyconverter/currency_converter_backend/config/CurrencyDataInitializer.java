package com.currencyconverter.currency_converter_backend.config;

import com.currencyconverter.currency_converter_backend.entity.Currency;
import com.currencyconverter.currency_converter_backend.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CurrencyDataInitializer implements CommandLineRunner {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeCurrencies();
    }

    private void initializeCurrencies() {
        // Check if currencies already exist
        if (currencyRepository.count() > 0) {
            return; // Data already initialized
        }

        // Add major world currencies
        addCurrency("USD", "US Dollar", "$");
        addCurrency("EUR", "Euro", "€");
        addCurrency("GBP", "British Pound", "£");
        addCurrency("JPY", "Japanese Yen", "¥");
        addCurrency("CAD", "Canadian Dollar", "C$");
        addCurrency("AUD", "Australian Dollar", "A$");
        addCurrency("CHF", "Swiss Franc", "Fr");
        addCurrency("CNY", "Chinese Yuan", "¥");
        addCurrency("INR", "Indian Rupee", "₹");
        addCurrency("KRW", "South Korean Won", "₩");
        addCurrency("SGD", "Singapore Dollar", "S$");
        addCurrency("HKD", "Hong Kong Dollar", "HK$");
        addCurrency("NOK", "Norwegian Krone", "kr");
        addCurrency("SEK", "Swedish Krona", "kr");
        addCurrency("DKK", "Danish Krone", "kr");
        addCurrency("PLN", "Polish Złoty", "zł");
        addCurrency("CZK", "Czech Koruna", "Kč");
        addCurrency("HUF", "Hungarian Forint", "Ft");
        addCurrency("RUB", "Russian Ruble", "₽");
        addCurrency("BRL", "Brazilian Real", "R$");
        addCurrency("MXN", "Mexican Peso", "$");
        addCurrency("ZAR", "South African Rand", "R");
        addCurrency("TRY", "Turkish Lira", "₺");
        addCurrency("NZD", "New Zealand Dollar", "NZ$");
        addCurrency("THB", "Thai Baht", "฿");
        addCurrency("MYR", "Malaysian Ringgit", "RM");
        addCurrency("PHP", "Philippine Peso", "₱");
        addCurrency("IDR", "Indonesian Rupiah", "Rp");
        addCurrency("VND", "Vietnamese Dong", "₫");
        addCurrency("ILS", "Israeli Shekel", "₪");
        addCurrency("AED", "UAE Dirham", "د.إ");
        addCurrency("SAR", "Saudi Riyal", "﷼");
        addCurrency("EGP", "Egyptian Pound", "£");
        addCurrency("NGN", "Nigerian Naira", "₦");
        addCurrency("GHS", "Ghanaian Cedi", "₵");
        addCurrency("KES", "Kenyan Shilling", "Sh");
        addCurrency("ZWL", "Zimbabwean Dollar", "Z$");

        System.out.println("Currency data initialized successfully!");
    }

    private void addCurrency(String code, String name, String symbol) {
        if (!currencyRepository.existsByCode(code)) {
            Currency currency = new Currency(code, name, symbol);
            currencyRepository.save(currency);
        }
    }
}
