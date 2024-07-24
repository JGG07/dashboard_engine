package com.satori.dashboardengine.bo;

import com.satori.dashboardengine.config.PipedriveConfig;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PipedriveBo {

    private final RestTemplate restTemplate;
    private final PipedriveConfig pipedriveConfig;

    public PipedriveBo(RestTemplate restTemplate, PipedriveConfig pipedriveConfig) {
        this.restTemplate = restTemplate;
        this.pipedriveConfig = pipedriveConfig;
    }

    public Deals getDeals(){
        String url = pipedriveConfig.getApiUrl() + "/deals?api_token=" + pipedriveConfig.getApiToken() + "&filter_id=90&limit=100000000";
        return restTemplate.getForObject(url, Deals.class);
    }

    // Procesar los datos de los tratos para obtener conteos por fecha y etapa
    public Map<String, Integer> getDealsCountByDate(Deals deals) {
        return processDealsByDate(deals.getData());
    }

    public Map<String, Integer> getStageDealsByDate(Deals deals, Integer stageId){
        return processStageDealsByDate(deals.getData(), stageId);
    }

    public Map<String, Integer> getDealsWonCountByDate(Deals deals){
        return processWonDealsByDate(deals.getData());
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

    private Map<String, Integer> processStageDealsByDate(List<DealsData> dealsDataList, int stageId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Integer> stageDealsCountByDate = new HashMap<>();

        // Contar tratos por fecha y etapa específica
        for (DealsData deal : dealsDataList) {
            LocalDate date = LocalDate.parse(deal.getAddTime(), formatter);
            // Filtrar solo los tratos de julio en la etapa especificada
            if (date.getMonthValue() == 7 && deal.getStageId() == stageId) {
                String dateString = date.toString();
                stageDealsCountByDate.put(dateString, stageDealsCountByDate.getOrDefault(dateString, 0) + 1);
            }
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
