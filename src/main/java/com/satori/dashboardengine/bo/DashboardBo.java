package com.satori.dashboardengine.bo;

import com.satori.dashboardengine.config.PipedriveConfig;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class DashboardBo {

    private final RestTemplate restTemplate;
    private final PipedriveConfig pipedriveConfig;

    @Autowired
    public DashboardBo(RestTemplate restTemplate, PipedriveConfig pipedriveConfig) {
        this.restTemplate = restTemplate;
        this.pipedriveConfig = pipedriveConfig;
    }

    public List<DealsData> getDeals(int start){

        log.info("getDeals");

        String url = pipedriveConfig.getApiUrl()
                + "/deals?api_token="
                + pipedriveConfig.getApiToken()
                + "&limit=500"
                + "&start="
                + start
                + "&sort=add_time DESC";

        Deals deals = restTemplate.getForObject(url, Deals.class);

        assert deals != null;

        return deals.getData();
    }

    int start = 0;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<DealsData> getDealsDateTime(List<DealsData> deals) {

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        List<DealsData> filteredDeals = new ArrayList<>();

        while (true) {
            LocalDate date = null;

            for (DealsData deal : deals) {
                String addTime = deal.getAddTime();
                LocalDateTime dateTime = LocalDateTime.parse(addTime, formatter);

                // Restar 6 horas
                LocalDateTime adjustedTime = dateTime.minusHours(6);
                date = adjustedTime.toLocalDate();

                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    filteredDeals.add(deal);
                }
            }

            assert date != null;
            if (date.isBefore(startDate)) {
                break;
            }
            start += 500;
        }

        return filteredDeals;
    }

}
