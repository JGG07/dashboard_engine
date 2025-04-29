package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/v2/dashboard")
public class DashboardEngineController {

    private final PipedriveService pipedriveService;

    public DashboardEngineController(PipedriveService pipedriveService) {
        this.pipedriveService = pipedriveService;
    }

    @GetMapping("/getDeals")
    public ResponseEntity<List<DealsData>> getDeals(){
        log.info("************************ getDeals *************************");
        Deals deals = pipedriveService.getDealsStart(0);
        return ResponseEntity.ok(deals.getData());
    }
}
