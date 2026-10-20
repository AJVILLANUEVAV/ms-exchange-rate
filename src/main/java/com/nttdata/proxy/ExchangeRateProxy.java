package com.nttdata.proxy;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "exchange-api")
@Path("/tipo-cambio/today.json")
public interface ExchangeRateProxy {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ExchangeRateResponse getTodayExchangeRate();
}
