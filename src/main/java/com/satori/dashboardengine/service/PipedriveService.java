package com.satori.dashboardengine.service;

import com.satori.dashboardengine.bo.PipedriveBo;
import com.satori.dashboardengine.dto.Deals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PipedriveService {

    @Autowired
    PipedriveBo pipedriveBo;

    public Deals getDeals() {
        return pipedriveBo.getDeals();
    }

    public Map<String, Integer> getDealsCountByDate(Deals deals){
        Map<String, Integer> dealsCountByDate = pipedriveBo.getDealsCountByDate(deals);
        return dealsCountByDate;
    }

    public Map<String, Integer> getStageDealsByDate(Deals deals, Integer stageId){
        return pipedriveBo.getStageDealsByDate(deals, stageId);
    }

    public Map<String, Integer> getDealsWonCountByDate(Deals deals){
        return pipedriveBo.getDealsWonCountByDate(deals);
    }

    public String getFuenteName(String sourceId){
        return pipedriveBo.getFuenteName(sourceId);
    }

    public String getStageName(Integer stageId){
        return pipedriveBo.getStageName(stageId);
    }

    public List<Integer> getCountsByDate(List<String> dates, Map<String, Integer> countsByDate) {
        return dates.stream()
                .map(date -> countsByDate.getOrDefault(date, 0))
                .collect(Collectors.toList());
    }

}
