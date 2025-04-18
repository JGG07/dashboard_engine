package com.satori.dashboardengine.service;

import com.satori.dashboardengine.dto.DealsData;

import java.util.List;

public interface DashboardEngineService {

    List<DealsData> getDeals(int start);

}
