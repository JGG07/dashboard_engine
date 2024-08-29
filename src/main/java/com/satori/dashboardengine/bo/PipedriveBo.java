package com.satori.dashboardengine.bo;

import com.satori.dashboardengine.config.PipedriveConfig;
import com.satori.dashboardengine.dto.Activities;
import com.satori.dashboardengine.dto.ActivitiesData;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class PipedriveBo {

    private final RestTemplate restTemplate;
    private final PipedriveConfig pipedriveConfig;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PipedriveBo(RestTemplate restTemplate, PipedriveConfig pipedriveConfig) {
        this.restTemplate = restTemplate;
        this.pipedriveConfig = pipedriveConfig;
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public Deals getDealsStart(int start){
        String url = pipedriveConfig.getApiUrl() + "/deals?api_token=" + pipedriveConfig.getApiToken() + "&limit=500" + "&start=" + start + "&sort=add_time DESC";
        return restTemplate.getForObject(url, Deals.class);
    }

    public List<ActivitiesData> getAllActivities(LocalDate startDate, LocalDate endDate, int userId){
        boolean active = true;

        List<ActivitiesData> activitiesDataList = new ArrayList<>();

        String url = pipedriveConfig.getApiUrl() + "/activities?api_token=" + pipedriveConfig.getApiToken() + "&start_date=" + startDate + "&end_date=" + endDate + "&user_id=" + userId;
        Activities activities = restTemplate.getForObject(url, Activities.class);

        if(activities.getAdditionalData().getPagination().isMoreItems()) {
            int start = 0;
            while (active) {
                activities = restTemplate.getForObject(url + "&start=" + start, Activities.class);
                activitiesDataList.addAll(activities.getData());
                start += activities.getAdditionalData().getPagination().getLimit();
                if(!activities.getAdditionalData().getPagination().isMoreItems()) {
                    active = false;
                }
            }
        }
        return activitiesDataList;
    }

    // Procesar los datos de los tratos para obtener conteos por fecha y etapa
    public Map<String, Integer> getDealsCountByDate(List<DealsData> dealsDataList) {
        return processDealsByDate(dealsDataList);
    }

    public Map<String, Integer> getDealsCountByDate(List<DealsData> dealsDataList, LocalDate startDate, LocalDate endDate) {
        return processDealsByDate(dealsDataList, startDate, endDate);
    }

    public Map<String, Integer> getDealsCountByDate(DealsData dealsDataList) {
        return processDealsByDate(dealsDataList);
    }

    public Map<String, Integer> getStageDealsByDate(List<DealsData> dealsDataList, Integer stageId){
        return processStageDealsByDate(dealsDataList, stageId);
    }

    /**
     *
     * @param dealsDataList
     * @param stageId
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, Integer> getStageDealsByDate(List<DealsData> dealsDataList, Integer stageId, LocalDate startDate, LocalDate endDate){
        return processStageDealsByDate(dealsDataList, stageId, startDate, endDate);
    }

    public Map<String, Integer> getStageDealsByDate(DealsData dealsDataList, Integer stageId){
        return processStageDealsByDate(dealsDataList, stageId);
    }


    public Map<String, Integer> getDealsWonCountByDate(List<DealsData> dealsDataList){
        return processWonDealsByDate(dealsDataList);
    }

    public Map<String, Integer> getDealsWonCountByDate(DealsData dealsDataList){
        return processWonDealsByDate(dealsDataList);
    }


    private Map<String, Integer> processDealsByDate(List<DealsData> dealsDataList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> dealsCountByDate = new HashMap<>();

        // Contar tratos por fecha
        for (DealsData deal : dealsDataList) {
            LocalDateTime addTime = LocalDateTime.parse(deal.getAddTime(), formatter);

            // Restar 6 horas
            LocalDateTime adjustedTime = addTime.minus(6, ChronoUnit.HOURS);
            LocalDate adjustedDate = adjustedTime.toLocalDate();

            // Filtrar solo los tratos de julio
            if (adjustedDate.getMonthValue() == 7) {
                String dateString = adjustedDate.toString();
                dealsCountByDate.put(dateString, dealsCountByDate.getOrDefault(dateString, 0) + 1);
            }
        }
        return dealsCountByDate;
    }

    private Map<String, Integer> processDealsByDate(List<DealsData> dealsDataList, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> dealsCountByDate = new HashMap<>();

        // Contar tratos por fecha
        for (DealsData deal : dealsDataList) {
            LocalDateTime addTime = LocalDateTime.parse(deal.getAddTime(), formatter);

            // Restar 6 horas
            LocalDateTime adjustedTime = addTime.minus(6, ChronoUnit.HOURS);
            LocalDate adjustedDate = adjustedTime.toLocalDate();

            // Filtrar tratos dentro del rango de fechas
            if (!adjustedDate.isBefore(startDate) && !adjustedDate.isAfter(endDate)) {
                String dateString = adjustedDate.toString();
                dealsCountByDate.put(dateString, dealsCountByDate.getOrDefault(dateString, 0) + 1);
            }
        }
        return dealsCountByDate;
    }

    private Map<String, Integer> processDealsByDate(DealsData deal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> dealsCountByDate = new HashMap<>();

        // Contar tratos por fecha

            LocalDateTime addTime = LocalDateTime.parse(deal.getAddTime(), formatter);

            // Restar 6 horas
            LocalDateTime adjustedTime = addTime.minus(6, ChronoUnit.HOURS);
            LocalDate adjustedDate = adjustedTime.toLocalDate();

            // Filtrar solo los tratos de julio
            if (adjustedDate.getMonthValue() == 7) {
                String dateString = adjustedDate.toString();
                dealsCountByDate.put(dateString, dealsCountByDate.getOrDefault(dateString, 0) + 1);
            }

        return dealsCountByDate;
    }

    private Map<String, Integer> processStageDealsByDate(List<DealsData> dealsDataList, int stageId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> stageDealsCountByDate = new HashMap<>();

        // Contar tratos por fecha y etapa específica
        for(DealsData dealsData : dealsDataList) {
            LocalDate date = LocalDate.parse(dealsData.getAddTime(), formatter);
            // Filtrar solo los tratos de julio en la etapa especificada
            if (date.getMonthValue() == 7 && dealsData.getStageId() == stageId) {
                String dateString = date.toString();
                stageDealsCountByDate.put(dateString, stageDealsCountByDate.getOrDefault(dateString, 0) + 1);
            }
        }

        return stageDealsCountByDate;
    }

    /**
     *
     * @param dealsDataList
     * @param stageId
     * @param startDate
     * @param endDate
     * @return
     */
    private Map<String, Integer> processStageDealsByDate(List<DealsData> dealsDataList, int stageId, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> stageDealsCountByDate = new HashMap<>();

        // Contar tratos por fecha y etapa específica
        for(DealsData dealsData : dealsDataList) {
            LocalDateTime dateTime = LocalDateTime.parse(dealsData.getAddTime(), formatter);

            // Restar 6 horas
            LocalDateTime adjustedTime = dateTime.minusHours(6);
            LocalDate adjustedDate = adjustedTime.toLocalDate();

            // Filtrar solo los tratos en el rango de fechas especificado y en la etapa especificada
            if (!adjustedDate.isBefore(startDate) && !adjustedDate.isAfter(endDate) && dealsData.getStageId() == stageId) {
                String dateString = adjustedDate.toString();
                stageDealsCountByDate.put(dateString, stageDealsCountByDate.getOrDefault(dateString, 0) + 1);
            }
        }
        return stageDealsCountByDate;
    }

    private Map<String, Integer> processStageDealsByDate(DealsData dealsDataList, int stageId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> stageDealsCountByDate = new HashMap<>();

        // Contar tratos por fecha y etapa específica
        LocalDate date = LocalDate.parse(dealsDataList.getAddTime(), formatter);
        // Filtrar solo los tratos de julio en la etapa especificada
        if (date.getMonthValue() == 7 && dealsDataList.getStageId() == stageId) {
            String dateString = date.toString();
            stageDealsCountByDate.put(dateString, stageDealsCountByDate.getOrDefault(dateString, 0) + 1);
        }

        return stageDealsCountByDate;
    }

    public Map<String, Integer> processWonDealsByDate(List<DealsData> dealsData) {
        Map<String, Integer> wonDealsCountByDate = new HashMap<>();
        // Contar tratos ganados por fecha
        for (DealsData deal : dealsData) {
            if ("won".equals(deal.getStatus())) {
                String date = deal.getAddTime().toString();
                wonDealsCountByDate.merge(date, 1, Integer::sum);
            }
        }
        return wonDealsCountByDate;
    }

    public Map<String, Integer> processWonDealsByDate(DealsData deal) {
        Map<String, Integer> wonDealsCountByDate = new HashMap<>();
        // Contar tratos ganados por fecha
        if ("won".equals(deal.getStatus())) {
            String date = deal.getAddTime();
            wonDealsCountByDate.merge(date, 1, Integer::sum);
        }
        return wonDealsCountByDate;
    }

    public String getFuenteName(String fuente) {
        // Retornar el nombre de la fuente basado en su ID
        if (fuente == null){
            return "Desconocido";
        }
        switch (fuente) {
            case "20": return "Evento";
            case "69": return "Tapial Obra";
            case "28": return "Facebook";
            case "65": return "Whatsapp";
            case "68": return "Espectacular";
            case "30": return "Instagram";
            case "77": return "Sitio Web";
            case "67": return "Cartera Propia";
            case "23": return "Referido";
            case "87": return "TikTok";
            case "26": return "Youtube";
            case "18": return "X";
            case "70": return "Tapial ShowRoom";
            case "73": return "Valla Publicitaria";
            case "71": return "Real Estate";
            case "114": return "LinkedIn";
            default: return "Desconocido";
        }
    }

    public String getStageName(int stageId) {
        // Retornar el nombre de la etapa basado en su ID
        switch (stageId) {
            case 6: return "Interesado";
            case 7: return "Contactado";
            case 8: return "Cita";
            case 9: return "Visita";
            case 10: return "Negociación";
            case 11: return "Apartado";
            default: return "Desconocido";
        }
    }

}
