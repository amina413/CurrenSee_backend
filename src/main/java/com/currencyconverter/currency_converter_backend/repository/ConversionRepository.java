package com.currencyconverter.currency_converter_backend.repository;


import com.currencyconverter.currency_converter_backend.entity.Conversion;
import com.currencyconverter.currency_converter_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversionRepository extends JpaRepository<Conversion, Long> {

    // Find conversions by user (paginated for performance)
    Page<Conversion> findByUserOrderByConversionDateDesc(User user, Pageable pageable);

    // Find conversions by user and currency pair
    List<Conversion> findByUserAndFromCurrencyCodeAndToCurrencyCode(
            User user, String fromCurrency, String toCurrency);

    // Find conversions by user within date range
    List<Conversion> findByUserAndConversionDateBetweenOrderByConversionDateDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    // Get conversion count for a user
    long countByUser(User user);

    // Find recent conversions for a user (last N conversions)
    @Query("SELECT c FROM Conversion c WHERE c.user = :user ORDER BY c.conversionDate DESC")
    List<Conversion> findRecentConversions(@Param("user") User user, Pageable pageable);
}

