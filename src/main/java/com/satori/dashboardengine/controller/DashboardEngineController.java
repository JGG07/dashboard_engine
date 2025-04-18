package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.DashboardEngineService;
import com.satori.dashboardengine.service.PipedriveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
public class DashboardEngineController {

    @Autowired
    private PipedriveService pipedriveService;

    private final DashboardEngineService dashboardEngineService;

    @Autowired
    public DashboardEngineController(DashboardEngineService dashboardEngineService) {
        this.dashboardEngineService = dashboardEngineService;
    }

    @GetMapping("/deals")
    public List<DealsData> getDeals() {
        return dashboardEngineService.getDeals(0);
    }
}
