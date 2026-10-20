package com.nttdata.entity;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "exchange_log")
public class ExchangeLog extends PanacheEntity {

    @Column(nullable = false)
    public String dni;

    @Column(name = "fecha_consulta", nullable = false)
    public LocalDateTime fechaConsulta;

    @Column(nullable = false)
    public Double resultado;
}
