package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/deals")
public class HomeController {

    @Autowired
    PipedriveService pipedriveService;

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/getDeals")
    public List<DealsData> getDeals(){
        Deals deals = pipedriveService.getDealsStart(0);
        return deals.getData();
    }
}
