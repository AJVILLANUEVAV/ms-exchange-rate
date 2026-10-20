package com.nttdata.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.nttdata.entity.ExchangeLog;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ExchangeLogRepository implements PanacheRepository<ExchangeLog> {

    public long countTodayByDni(String dni) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);

        return count("dni = ?1 and fechaConsulta >= ?2 and fechaConsulta < ?3",
                dni, startOfDay, startOfNextDay);
    }

    @Transactional
    public ExchangeLog save(ExchangeLog exchangeLog) {
        Objects.requireNonNull(exchangeLog, "exchangeLog no puede ser null");
        persist(exchangeLog);
        return exchangeLog;
    }
}
