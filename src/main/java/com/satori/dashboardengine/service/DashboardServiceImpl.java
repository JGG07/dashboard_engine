package com.satori.dashboardengine.service;

import com.satori.dashboardengine.bo.DashboardBo;
import com.satori.dashboardengine.dto.DealsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardEngineService{

    private final DashboardBo dashboardBo;

    @Autowired
    public DashboardServiceImpl(DashboardBo dashboardBo) {
        this.dashboardBo = dashboardBo;
    }

    @Override
    public List<DealsData> getDeals(int start){
        return dashboardBo.getDeals(start);
    }
}
