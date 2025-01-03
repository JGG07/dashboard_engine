package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealsData {

    @JsonProperty("person_name")
    private String personName;
    @JsonProperty("user_id")
    private UserId userId;
    @JsonProperty("559fbdb1362314763f554be0f832dec60c65bb37")
    private String fuente;
    @JsonProperty("147365f5c6ee5c87124bb1efac90d538cfca927d")
    private String campaign;
    @JsonProperty("add_time")
    private String addTime;
    @JsonProperty("stage_id")
    private int stageId;
    @JsonProperty("won_time")
    private String wonTime;
    private String status;
    @JsonProperty("lost_reason")
    private String lostReason;
    @JsonProperty("owner_name")
    private String ownerName;
    @JsonProperty("stage_change_time")
    private String stageChangeTime;
}
