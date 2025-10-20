package com.currencyconverter.currency_converter_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CurrencyConverterBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(CurrencyConverterBackendApplication.class, args);
	}
}
