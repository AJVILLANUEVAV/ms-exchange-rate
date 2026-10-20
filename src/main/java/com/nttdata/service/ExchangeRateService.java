package com.nttdata.service;

import java.time.LocalDateTime;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.nttdata.entity.ExchangeLog;
import com.nttdata.proxy.ExchangeRateProxy;
import com.nttdata.proxy.ExchangeRateResponse;
import com.nttdata.repository.ExchangeLogRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ExchangeRateService {

    private static final Logger LOG = Logger.getLogger(ExchangeRateService.class);
    private static final long DAILY_QUERY_LIMIT = 10;

    @Inject
    @RestClient
    ExchangeRateProxy exchangeRateProxy;

    @Inject
    ExchangeLogRepository exchangeLogRepository;

    public ExchangeRateResponse getTodayExchangeRate(String dni) {
        LOG.info("Intento de consulta de tipo de cambio recibido");

        if (dni == null || dni.isBlank()) {
            LOG.warn("Consulta rechazada: el DNI es obligatorio");
            throw new BadRequestException("El DNI es obligatorio");
        }

        long queriesToday = exchangeLogRepository.countTodayByDni(dni);
        LOG.infof("Validación del límite diario completada: %d consultas", queriesToday);

        if (queriesToday >= DAILY_QUERY_LIMIT) {
            LOG.warn("Consulta rechazada por superar el límite diario");
            throw new WebApplicationException(
                    "Se superó el límite de 10 consultas diarias",
                    Response.Status.TOO_MANY_REQUESTS);
        }

        ExchangeRateResponse exchangeRate;
        try {
            exchangeRate = exchangeRateProxy.getTodayExchangeRate();
        } catch (RuntimeException e) {
            LOG.error("Error al consultar el servicio externo de tipo de cambio", e);
            throw e;
        }

        if (exchangeRate == null || exchangeRate.venta() == null) {
            LOG.error("El servicio externo devolvió una respuesta sin valor de venta");
            throw new IllegalStateException("Respuesta inválida del servicio de tipo de cambio");
        }

        ExchangeLog exchangeLog = new ExchangeLog();
        exchangeLog.dni = dni;
        exchangeLog.fechaConsulta = LocalDateTime.now();
        exchangeLog.resultado = exchangeRate.venta().doubleValue();

        try {
            exchangeLogRepository.save(exchangeLog);
        } catch (RuntimeException e) {
            LOG.error("Error al registrar la consulta de tipo de cambio", e);
            throw e;
        }

        LOG.info("Consulta de tipo de cambio completada y registrada");
        return exchangeRate;
    }
}
