package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activities {

    private boolean success;
    private List<ActivitiesData> data;
    @JsonProperty("additional_data")
    private AdditionalData additionalData;
}
