package com.nttdata.proxy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(
        LocalDate fecha,
        BigDecimal sunat,
        BigDecimal compra,
        BigDecimal venta) {
}
