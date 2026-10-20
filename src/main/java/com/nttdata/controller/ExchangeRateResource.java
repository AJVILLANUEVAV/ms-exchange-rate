package com.nttdata.controller;

import java.util.Map;

import com.nttdata.proxy.ExchangeRateResponse;
import com.nttdata.service.ExchangeRateService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/exchange-rate")
@Produces(MediaType.APPLICATION_JSON)
public class ExchangeRateResource {

    @Inject
    ExchangeRateService exchangeRateService;

    @GET
    public Response getTodayExchangeRate(@QueryParam("dni") String dni) {
        if (dni == null || dni.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensaje", "El parámetro dni es obligatorio"))
                    .build();
        }

        try {
            ExchangeRateResponse exchangeRate = exchangeRateService.getTodayExchangeRate(dni);
            return Response.ok(exchangeRate).build();
        } catch (WebApplicationException e) {
            if (e.getResponse() != null
                    && e.getResponse().getStatus() == Response.Status.TOO_MANY_REQUESTS.getStatusCode()) {
                return Response.status(422)
                        .entity(Map.of("mensaje", "Se superó el límite de 10 consultas diarias"))
                        .build();
            }
            throw e;
        }
    }
}
