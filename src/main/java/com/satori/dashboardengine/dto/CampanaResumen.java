package com.satori.dashboardengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CampanaResumen {
    private String nombre;
    private int anio;
    private int[] meses = new int[12];

    public CampanaResumen(String nombre, int anio) {
        this.nombre = nombre;
        this.anio = anio;
    }

    public void incrementarMes(int mes) {
        this.meses[mes - 1]++;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAnio() {
        return anio;
    }

    public int[] getMeses() {
        return meses;
    }
}


