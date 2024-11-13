package com.satori.dashboardengine.repository;

import com.satori.dashboardengine.config.PipedriveConfig;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository("DashboardRepository")
@Log4j2
public class DashboardRepository {

    @Autowired
    private PipedriveConfig pipedriveConfig;

    @Autowired
    private RestTemplate restTemplate;

    /**
     *
     * @return
     */
    public List<DealsData> getAllDeals(){
        log.info("************************ getAllDeals *************************");
        int start = 0;

        Deals deals;

        String url = pipedriveConfig.getApiUrl() + "/deals?api_token=" + pipedriveConfig.getApiToken() + "&limit=500" + "&start=" + start + "&sort=add_time DESC";
        log.info("url: " + url);

        deals = restTemplate.getForObject(url, Deals.class);
        assert deals != null;
        if(deals.getAdditionalData().getPagination().isMoreItems()) {
            while (true) {
                start += 500;
                log.info("url: " + url);

                url = pipedriveConfig.getApiUrl() + "/deals?api_token=" + pipedriveConfig.getApiToken() + "&limit=500" + "&start=" + start + "&sort=add_time DESC";
                restTemplate.getForObject(url, Deals.class);

                if(!deals.getAdditionalData().getPagination().isMoreItems()){
                    break;
                }

            }
        }
        return deals.getData();
    }
}
