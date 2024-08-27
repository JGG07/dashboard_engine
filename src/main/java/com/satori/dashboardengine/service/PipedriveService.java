package com.satori.dashboardengine.service;

import com.satori.dashboardengine.bo.PipedriveBo;
import com.satori.dashboardengine.dto.Activities;
import com.satori.dashboardengine.dto.ActivitiesData;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PipedriveService {

    @Autowired
    PipedriveBo pipedriveBo;

    public Deals getDeals() {
        return pipedriveBo.getDeals();
    }

    public Deals getDealsStart(Integer start){
        return pipedriveBo.getDealsStart(start);
    }

    public List<ActivitiesData> getAllActivities(LocalDate start, LocalDate end, int userId){
        return pipedriveBo.getAllActivities(start, end, userId);
    }

    public Map<String, Integer> getDealsCountByDate(List<DealsData> dealsDataList){
        return pipedriveBo.getDealsCountByDate(dealsDataList);
    }

    public Map<String, Integer> getDealsCountByDate(List<DealsData> dealsDataList, LocalDate startDate, LocalDate endDate) {
        return pipedriveBo.getDealsCountByDate(dealsDataList, startDate, endDate);
    }

    public Map<String, Integer> getDealsCountByDate(DealsData dealsDataList){
        return pipedriveBo.getDealsCountByDate(dealsDataList);
    }

    public Map<String, Integer> getStageDealsByDate(List<DealsData> dealsDataList, Integer stageId){
        return pipedriveBo.getStageDealsByDate(dealsDataList, stageId);
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
        return pipedriveBo.getStageDealsByDate(dealsDataList, stageId, startDate, endDate);
    }

    public Map<String, Integer> getStageDealsByDate(DealsData dealsDataList, Integer stageId){
        return pipedriveBo.getStageDealsByDate(dealsDataList, stageId);
    }

    public Map<String, Integer> getDealsWonCountByDate(List<DealsData> dealsDataList){
        return pipedriveBo.getDealsWonCountByDate(dealsDataList);
    }

    public Map<String, Integer> getDealsWonCountByDate(DealsData dealsDataList){
        return pipedriveBo.getDealsWonCountByDate(dealsDataList);
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

    public Map<String, Map<String, Integer>> getDealsByStageAndStatus(List<DealsData> deals) {
        Map<String, Map<String, Integer>> dealsByStageAndStatus = new HashMap<>();

        // Verificar si la lista de tratos no es nula ni vacía
        if (deals == null || deals.isEmpty()) {
            System.out.println("No deals data available.");
            return dealsByStageAndStatus; // Retornar mapa vacío si no hay tratos
        }

        // Definir las etapas como se necesite
        // (Este array no se usa directamente en el código actual, pero puede ser útil para validación o referencia)
        String[] stages = {"Interesado", "Contactado", "Cita", "Visita", "Negociación", "Apartado"};

        for (DealsData deal : deals) {
            // Obtener el nombre de la etapa basado en el ID
            String stage = pipedriveBo.getStageName(deal.getStageId());
            if (stage == null) {
                System.out.println("Unknown stage ID: " + deal.getStageId());
                continue; // Continuar con el siguiente trato si el nombre de la etapa es nulo
            }

            // Obtener el estado del trato
            String status = deal.getStatus();
            if (status == null) {
                System.out.println("Unknown status for deal ID: " + deal.getStageId());
                continue; // Continuar con el siguiente trato si el estado es nulo
            }

            // Inicializar el mapa de estado si no está presente
            dealsByStageAndStatus.putIfAbsent(stage, new HashMap<>());
            Map<String, Integer> statusMap = dealsByStageAndStatus.get(stage);

            // Inicializar el conteo del estado si no está presente
            statusMap.putIfAbsent(status, 0);

            // Incrementar el conteo del estado
            statusMap.put(status, statusMap.get(status) + 1);
        }

        return dealsByStageAndStatus;
    }

    public Map<String, Object> prepareChartData(Map<String, Map<String, Integer>> dealsByStageAndStatus) {
        Map<String, Object> chartData = new LinkedHashMap<>();

        // Prepara series de datos
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> stageEntry : dealsByStageAndStatus.entrySet()) {
            String stage = stageEntry.getKey();
            Map<String, Integer> statusMap = stageEntry.getValue();

            Map<String, Object> seriesItem = new LinkedHashMap<>();
            seriesItem.put("name", stage);
            seriesItem.put("data", new ArrayList<>(statusMap.values()));

            series.add(seriesItem);
        }

        // Añade series al mapa de datos de la gráfica
        chartData.put("series", series);
        return chartData;
    }


}
