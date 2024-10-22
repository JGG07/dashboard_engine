package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deals {

    private String success;
    private List<DealsData> data;
    @JsonProperty("additional_data")
    private AdditionalData additionalData;
}
