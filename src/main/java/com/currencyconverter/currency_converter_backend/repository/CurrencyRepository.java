package com.currencyconverter.currency_converter_backend.repository;

import com.currencyconverter.currency_converter_backend.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // Find currency by code (e.g., "USD", "EUR")
    Optional<Currency> findByCode(String code);

    // Find all active currencies
    List<Currency> findByIsActiveTrue();

    // Find currencies by name containing (for search functionality)
    List<Currency> findByNameContainingIgnoreCase(String name);

    // Check if currency code exists
    boolean existsByCode(String code);
}

