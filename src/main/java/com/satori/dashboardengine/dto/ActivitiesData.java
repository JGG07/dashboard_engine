package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitiesData {

    private String note;
    @JsonProperty("marked_as_done_time")
    private String doneTime;
    @JsonProperty("owner_name")
    private String ownerName;

    // Formato esperado de updateTime (ajusta el patrón según sea necesario)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void getDoneTime(String doneTime) {
        try {
            // Convertir la cadena updateTime a LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(doneTime, formatter);

            // Restar 6 horas
            dateTime = dateTime.minusHours(6);

            // Volver a formatear a String para almacenarlo
            this.doneTime = dateTime.format(formatter);

        } catch (DateTimeParseException e) {
            // Manejar excepción si el formato de fecha es incorrecto
            System.err.println("Error al analizar la fecha: " + doneTime);
            this.doneTime = doneTime; // Dejar la fecha original si no se puede analizar
        }
    }

}
