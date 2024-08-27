package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitiesData {

    private String note;
    @JsonProperty("update_time")
    private String updateTime;
    @JsonProperty("owner_name")
    private String ownerName;

}
