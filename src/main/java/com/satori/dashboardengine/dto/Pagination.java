package com.satori.dashboardengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pagination {
    private int start;
    private int limit;
    @JsonProperty("more_items_in_collection")
    private boolean moreItems;
    @JsonProperty("next_start")
    private int nextStart;
}