package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.ActivitiesData;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class DashboardController {

    @Autowired
    private PipedriveService pipedriveService;

    /**
     *
     * @param model
     * @return
     */
    @GetMapping("/mercadeo")
    public String viewData(Model model) {

        // Establecer valores predeterminados para fechas si no se proporcionan
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        // Generar el rango de fechas basado en startDate y endDate
        List<String> dates = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }

        int start = 0;
        int LIMIT = 500;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<DealsData> filteredDeals = new ArrayList<>();
        Deals deals = pipedriveService.getDealsStart(start);

        while (true) {
            LocalDate date = null;

            for (DealsData deal : deals.getData()) {
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
            start += LIMIT;
        }

        Map<String, Integer> dealsCountByDate = pipedriveService.getDealsCountByDate(filteredDeals, startDate, endDate);
        Map<String, Integer> stageInteresados = pipedriveService.getStageDealsByDate(filteredDeals, 6, startDate, endDate);
        Map<String, Integer> stageContactados = pipedriveService.getStageDealsByDate(filteredDeals, 7, startDate, endDate);
        Map<String, Integer> stageCita = pipedriveService.getStageDealsByDate(filteredDeals, 8, startDate, endDate);
        Map<String, Integer> stageVisita = pipedriveService.getStageDealsByDate(filteredDeals, 9, startDate, endDate);
        Map<String, Integer> stageNegociacion = pipedriveService.getStageDealsByDate(filteredDeals, 10, startDate, endDate);
        Map<String, Integer> stageApartado = pipedriveService.getStageDealsByDate(filteredDeals, 11, startDate, endDate);

        Map<String, Integer> wonDealsCountByDate = pipedriveService.getDealsWonCountByDate(filteredDeals);


        // Recopilar razones de pérdida
        Map<String, Integer> lostReasons = new HashMap<>();

        for (DealsData deal : filteredDeals) {
            if ("lost".equalsIgnoreCase(deal.getStatus())) {
                String reason = deal.getLostReason();
                if (reason != null && !reason.isEmpty()) {
                    if (lostReasons.containsKey(reason)) {
                        lostReasons.put(reason, lostReasons.get(reason) + 1);
                    } else {
                        lostReasons.put(reason, 1);
                    }
                }
            }
        }

        // Ordenar razones de pérdida de mayor a menor
        List<Map.Entry<String, Integer>> sortedLostReasonsList = new ArrayList<>(lostReasons.entrySet());
        sortedLostReasonsList.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                return Integer.compare(entry2.getValue(), entry1.getValue());
            }
        });

        Map<String, Integer> sortedLostReasons = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sortedLostReasonsList) {
            sortedLostReasons.put(entry.getKey(), entry.getValue());
        }

        // Calcular el total de pérdidas
        int totalLostReasons = 0;
        for (int value : sortedLostReasons.values()) {
            totalLostReasons += value;
        }

        // Calcular el porcentaje de pérdida para cada razón
        Map<String, String> reasonsWithPercentages = new HashMap<>();
        for (Map.Entry<String, Integer> entry : sortedLostReasons.entrySet()) {
            int count = entry.getValue();
            double percentage = (count * 100.0) / totalLostReasons;
            reasonsWithPercentages.put(entry.getKey(), String.format("%.2f%%", percentage));
        }

        // Agrupar tratos por fuente, etapa y status
        Map<String, Map<String, Integer>> dealsBySource = new HashMap<>();
        Map<String, Integer> totalDealsBySource = new HashMap<>();
        Map<String, Integer> openDealsBySource = new HashMap<>();
        Map<String, Integer> lostDealsBySource = new HashMap<>();
        Map<String, Integer> wonDealsBySource = new HashMap<>();

        int totalWonDeals = 0;
        int totalOpenDeals = 0;
        int totalLostDeals = 0;

        for (DealsData deal : filteredDeals) {
            String sourceName = pipedriveService.getFuenteName(deal.getFuente());
            String stage = pipedriveService.getStageName(deal.getStageId());
            String status = deal.getStatus();

            if (!dealsBySource.containsKey(sourceName)) {
                dealsBySource.put(sourceName, new HashMap<>());
            }
            Map<String, Integer> stageCount = dealsBySource.get(sourceName);
            stageCount.put(stage, stageCount.getOrDefault(stage, 0) + 1);
            totalDealsBySource.put(sourceName, totalDealsBySource.getOrDefault(sourceName, 0) + 1);

            switch (status) {
                case "open":
                    openDealsBySource.put(sourceName, openDealsBySource.getOrDefault(sourceName, 0) + 1);
                    totalOpenDeals++;
                    break;
                case "lost":
                    lostDealsBySource.put(sourceName, lostDealsBySource.getOrDefault(sourceName, 0) + 1);
                    totalLostDeals++;
                    break;
                case "won":
                    wonDealsBySource.put(sourceName, wonDealsBySource.getOrDefault(sourceName, 0) + 1);
                    totalWonDeals++;
                    break;
            }
        }

        // Ordenar dealsBySource de mayor a menor según el número total de tratos
        List<Map.Entry<String, Map<String, Integer>>> sortedDealsBySourceList = new ArrayList<>(dealsBySource.entrySet());
        sortedDealsBySourceList.sort(new Comparator<Map.Entry<String, Map<String, Integer>>>() {
            @Override
            public int compare(Map.Entry<String, Map<String, Integer>> entry1, Map.Entry<String, Map<String, Integer>> entry2) {
                return Integer.compare(totalDealsBySource.get(entry2.getKey()), totalDealsBySource.get(entry1.getKey()));
            }
        });

        Map<String, Map<String, Integer>> sortedDealsBySource = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : sortedDealsBySourceList) {
            sortedDealsBySource.put(entry.getKey(), entry.getValue());
        }

        // Crear listas de conteos de tratos por fecha y etapa
        List<Integer> counts = pipedriveService.getCountsByDate(dates, dealsCountByDate);
        List<Integer> contactados = pipedriveService.getCountsByDate(dates, stageContactados);
        List<Integer> interesados = pipedriveService.getCountsByDate(dates, stageInteresados);

        List<Integer> citas = pipedriveService.getCountsByDate(dates, stageCita);
        List<Integer> visitas = pipedriveService.getCountsByDate(dates, stageVisita);
        List<Integer> negociaciones = pipedriveService.getCountsByDate(dates, stageNegociacion);
        List<Integer> apartados = pipedriveService.getCountsByDate(dates, stageApartado);
        List<Integer> wonDeals = pipedriveService.getCountsByDate(dates, wonDealsCountByDate);

        // Calcular los totales de tratos
        int totalDeals = 0;
        int totalContactados = 0;
        int totalInteresados = 0;

        int totalCitas = 0;
        int totalVisitas = 0;
        int totalNegociaciones = 0;
        int totalApartados = 0;

        for (Integer count : counts) {
            totalDeals += count;
        }
        for (Integer count : contactados) {
            totalContactados += count;
        }
        for (Integer count : interesados) {
            totalInteresados += count;
        }

        for (Integer count : citas) {
            totalCitas += count;
        }
        for (Integer count : visitas) {
            totalVisitas += count;
        }
        for (Integer count : negociaciones) {
            totalNegociaciones += count;
        }
        for (Integer count : apartados) {
            totalApartados += count;
        }


        // Obtener los datos de tratos por etapa y estado
        Map<String, Map<String, Integer>> dealsByStageAndStatus = pipedriveService.getDealsByStageAndStatus(filteredDeals);

// Crear una lista para almacenar los conteos de tratos abiertos en el orden deseado
        List<Integer> orderedOpenDeals = new ArrayList<>();
        List<Integer> orderedLostDeals = new ArrayList<>();
        List<Integer> orderedWonDeals = new ArrayList<>();

// Definir el orden deseado de las etapas
        List<String> desiredOrder = Arrays.asList("Interesado", "Contactado", "Cita", "Visita", "Negociación", "Apartado");

// Iterar sobre las etapas en el orden deseado
        for (String stage : desiredOrder) {
            Map<String, Integer> statusCounts = dealsByStageAndStatus.getOrDefault(stage, Collections.emptyMap());
            // Obtener el número de tratos abiertos o 0 si no hay datos
            int openCount = statusCounts.getOrDefault("open", 0);
            orderedOpenDeals.add(openCount);
            int lostCount = statusCounts.getOrDefault("lost", 0);
            orderedLostDeals.add(lostCount);
            int wonCount = statusCounts.getOrDefault("won", 0);
            orderedWonDeals.add(wonCount);
        }

        // Obtener lista de asesores únicos
        List<String> listAsesores = filteredDeals.stream()
                .map(DealsData::getOwnerName)
                .distinct()
                .collect(Collectors.toList());

        // Agregar datos al modelo para ser utilizados en la vista Thymeleaf
        model.addAttribute("dealsDates", dates);
        model.addAttribute("dealsCounts", counts);
        model.addAttribute("interesados", interesados);
        model.addAttribute("contactados", contactados);

        model.addAttribute("citas", citas);
        model.addAttribute("visitas", visitas);
        model.addAttribute("negociaciones", negociaciones);
        model.addAttribute("apartados", apartados);
        model.addAttribute("wonDeals", wonDeals);
        model.addAttribute("totalDeals", totalDeals);
        model.addAttribute("totalContactado", totalContactados);
        model.addAttribute("totalInteresados", totalInteresados);

        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalVisitas", totalVisitas);
        model.addAttribute("totalNegociaciones", totalNegociaciones);
        model.addAttribute("totalApartados", totalApartados);
        model.addAttribute("dealsBySource", sortedDealsBySource);
        model.addAttribute("totalDealsBySource", totalDealsBySource);
        model.addAttribute("openDealsBySource", openDealsBySource);
        model.addAttribute("lostDealsBySource", lostDealsBySource);
        model.addAttribute("wonDealsBySource", wonDealsBySource);
        model.addAttribute("totalWonDeals", totalWonDeals);
        model.addAttribute("totalOpenDeals", totalOpenDeals);
        model.addAttribute("totalLostDeals", totalLostDeals);
        model.addAttribute("sortedLostReasons", sortedLostReasons);
        model.addAttribute("reasonsWithPercentages", reasonsWithPercentages);
        model.addAttribute("totalLostReasons", totalLostReasons);
        model.addAttribute("orderedOpenDeals", orderedOpenDeals);
        model.addAttribute("orderedLostDeals", orderedLostDeals);
        model.addAttribute("orderedWonDeals", orderedWonDeals);
        model.addAttribute("asesores", listAsesores);

        Map<String, Integer> dealsByCampaign = new HashMap<>();
        String campaign = "";
        for(DealsData deal : filteredDeals) {

            if (deal.getCampaign() != null && deal.getCampaign().contains(",")) {
                campaign = pipedriveService.getCampaignName(deal.getCampaign());
            } else if (deal.getCampaign() != null && !deal.getCampaign().isEmpty()) {
                campaign = deal.getCampaign();
            } else {
                campaign = "Desconocido";
            }

            dealsByCampaign.put(campaign, dealsByCampaign.getOrDefault(campaign, 0) + 1);
        }

        Map<String, AdvisorStats> campaignStatsMap = new HashMap<>();
        AdvisorStats statsCampaign;
        List<Map.Entry<String, Integer>> sortedDealsByCampaign = dealsByCampaign.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        List<CombinedCampaign> combinedCampaignList = new ArrayList<>();

        for(DealsData deal : filteredDeals) {
            if (deal.getCampaign() != null && deal.getCampaign().contains(",")) {
                campaign = pipedriveService.getCampaignName(deal.getCampaign());

            } else if (deal.getCampaign() != null && !deal.getCampaign().isEmpty()) {
                campaign = deal.getCampaign();

            } else {
                campaign = "Desconocido";
            }

            statsCampaign = campaignStatsMap.getOrDefault(campaign, new AdvisorStats());

            if (deal.getStageId() == 8) {
                statsCampaign.cita++;
            }

            if (deal.getStageId() == 9) {
                statsCampaign.visita++;
            }
            if (deal.getStageId() == 10) {
                statsCampaign.negociacion++;
            }

            if (deal.getStageId() == 11) {
                statsCampaign.apartado++;
            }

            if (deal.getStatus().equals("won")) {
                statsCampaign.ganado++;
            }

            campaignStatsMap.put(campaign, statsCampaign);
        }

        for (Map.Entry<String, Integer> entry : sortedDealsByCampaign) {
            campaign = entry.getKey();
            int dealsCount = entry.getValue();

            statsCampaign = campaignStatsMap.getOrDefault(campaign, new AdvisorStats());

            CombinedCampaign combinedCampaign = new CombinedCampaign(campaign, dealsCount,
                    statsCampaign.getCita(), statsCampaign.getVisita(), statsCampaign.getNegociacion(),
                    statsCampaign.getApartado(), statsCampaign.getGanado());

            combinedCampaignList.add(combinedCampaign);
        }

        model.addAttribute("combinedCampaign" , combinedCampaignList);

        // Inicializar los totales
        int totalDealsCampaign = 0;
        int totalCitasCampaign = 0;
        int totalVisitasCampaign = 0;
        int totalNegociacionesCampaign = 0;
        int totalApartadosCampaign = 0;
        int totalWonDealsCampaign = 0;

        for(CombinedCampaign stat : combinedCampaignList){
            totalDealsCampaign += stat.getDeals();
            totalCitasCampaign += stat.getCita();
            totalVisitasCampaign += stat.getVisita();
            totalNegociacionesCampaign += stat.getNegociacion();
            totalApartadosCampaign += stat.getApartado();
            totalWonDealsCampaign += stat.getGanado();
        }

        // Pasar los totales al modelo
        model.addAttribute("totalDealsCampaign", totalDealsCampaign);
        model.addAttribute("totalCitasCampaign", totalCitasCampaign);
        model.addAttribute("totalVisitasCampaign", totalVisitasCampaign);
        model.addAttribute("totalNegociacionesCampaign", totalNegociacionesCampaign);
        model.addAttribute("totalApartadosCampaign", totalApartadosCampaign);
        model.addAttribute("totalWonDealsCampaign", totalWonDealsCampaign);

        return "mercadeo"; // Retorna la vista con los datos filtrados
    }

    @GetMapping("/comercial")
    public String comercial(Model model) {
        log.info("*************** COMERCIAL ***************");

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        int start = 0;
        int LIMIT = 500;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Datos filtrados por fecha
        List<DealsData> filteredDeals = new ArrayList<>();

        // Datos filtrados por cambio de actividad
        List<DealsData> filteredDealsByStageChange = new ArrayList<>();


        while (true) {
            log.info("*************** FirstWhile ***************");

            Deals deals = pipedriveService.getDealsStart(start);
            LocalDate date = null;

            for (DealsData deal : deals.getData()) {
                String addTime = deal.getAddTime();
                LocalDateTime dateTime = LocalDateTime.parse(addTime, formatter);

                // Restar 6 horas
                LocalDateTime adjustedTime = dateTime.minusHours(6);
                date = adjustedTime.toLocalDate();

                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    filteredDeals.add(deal);
                }

                // Tomará los won que estén dentro del rango de fechas
                if(deal.getStatus().equalsIgnoreCase("won")){
                    if(deal.getWonTime() != null){

                        LocalDateTime wonTime = LocalDateTime.parse(deal.getWonTime(), formatter);
                        // Restar 6 horas
                        LocalDateTime adjustedWonTime = wonTime.minusHours(6);
                        date = adjustedWonTime.toLocalDate();

                        if(!date.isBefore(startDate) && !date.isAfter(endDate)){
                            filteredDealsByStageChange.add(deal);
                        }
                    }
                } else {
                    if(deal.getStageChangeTime() != null) {
                        String addTimeStage = deal.getStageChangeTime();

                        // Procesar el caso donde addTime no es null
                        dateTime = LocalDateTime.parse(addTimeStage, formatter);

                        // Restar 6 horas
                        adjustedTime = dateTime.minusHours(6);
                        date = adjustedTime.toLocalDate();

                        if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                            filteredDealsByStageChange.add(deal);
                        }
                    }
                }

            }

            assert date != null;
            if (!deals.getAdditionalData().getPagination().isMoreItems()) {
                break;
            }
            start += LIMIT;
        }

        // Crear un mapa para almacenar la suma de deals por asesor
        Map<String, Integer> dealsByAdvisor = new HashMap<>();
        Map<String, Integer> dealsByFuente = new HashMap<>();

        // Iterar sobre la lista de filteredDeals
        for (DealsData deal : filteredDeals) {

            String advisor = deal.getOwnerName();  // Suponiendo que getOwnerName() devuelve el nombre del asesor
            dealsByAdvisor.put(advisor, dealsByAdvisor.getOrDefault(advisor, 0) + 1);

            String fuente = pipedriveService.getFuenteName(deal.getFuente());
            dealsByFuente.put(fuente, dealsByFuente.getOrDefault(fuente, 0) +1 );

        }

        // Ordenar el mapa por número de deals en orden descendente
        List<Map.Entry<String, Integer>> sortedDealsByAdvisor = dealsByAdvisor.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        // Ordenar el mapa por número de deals en orden descendente
        List<Map.Entry<String, Integer>> sortedDealsByFuente = dealsByFuente.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();


        Map<String, AdvisorStats> advisorStatsMap = new HashMap<>();
        Map<String, AdvisorStats> fuenteStatsMap = new HashMap<>();

        AdvisorStats stats;
        AdvisorStats statsFuente;

        for (DealsData deal : filteredDealsByStageChange) {
            String advisor = deal.getOwnerName();
            String fuente = pipedriveService.getFuenteName(deal.getFuente());

            stats = advisorStatsMap.getOrDefault(advisor, new AdvisorStats());
            statsFuente = fuenteStatsMap.getOrDefault(fuente, new AdvisorStats());

            if (deal.getStageId() == 8) {
                stats.cita++;

                statsFuente.cita++;

            }

            if (deal.getStageId() == 9) {
                stats.cita++;
                stats.visita++;

                statsFuente.cita++;
                statsFuente.visita++;

            }
            if (deal.getStageId() == 10) {
                stats.cita++;
                stats.visita++;
                stats.negociacion++;

                statsFuente.cita++;
                statsFuente.visita++;
                statsFuente.negociacion++;

            }

            if (deal.getStageId() == 11) {
                stats.cita++;
                stats.visita++;
                stats.negociacion++;
                stats.apartado++;

                statsFuente.cita++;
                statsFuente.visita++;
                statsFuente.negociacion++;
                statsFuente.apartado++;

            }

            if (deal.getStatus().equals("won")) {
                stats.ganado++;

                statsFuente.ganado++;

            }

            advisorStatsMap.put(advisor, stats);
            fuenteStatsMap.put(fuente, statsFuente);
        }


        // Crear una lista para almacenar la combinación de ambos
        List<CombinedAdvisorStats> combinedList = new ArrayList<>();
        // Crear una lista para almacenar la combinación de ambos con conversiones
        List<CombinedAdvisorConversionStats> combinedConversionList = new ArrayList<>();

        List<CombinedFuenteStats> combinedFuenteStatsList = new ArrayList<>();
        List<CombinedFuenteConversionStats> combinedFuenteConversionStatsList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedDealsByAdvisor) {
            String advisor = entry.getKey();
            int dealsCount = entry.getValue();

            stats = advisorStatsMap.getOrDefault(advisor, new AdvisorStats());

            CombinedAdvisorStats combinedStats = new CombinedAdvisorStats(advisor, dealsCount,
                    stats.getCita(), stats.getVisita(), stats.getNegociacion(),
                    stats.getApartado(), stats.getGanado());

            float cita = (float) stats.getCita()/dealsCount*100;
            float visita = (float) stats.getVisita()/dealsCount*100;
            float negociacion = (float) stats.getNegociacion()/dealsCount*100;
            float apartado = (float) stats.getApartado()/dealsCount*100;
            float ganado = (float) stats.getGanado()/dealsCount*100;

            CombinedAdvisorConversionStats combinedStatsConversion = new CombinedAdvisorConversionStats(advisor, dealsCount,
                    cita, visita, negociacion,
                    apartado, ganado);

            combinedConversionList.add(combinedStatsConversion);
            combinedList.add(combinedStats);
        }

        for (Map.Entry<String, Integer> entry : sortedDealsByFuente) {

            String fuente = entry.getKey();
            int dealsCount = entry.getValue();

            statsFuente = fuenteStatsMap.getOrDefault(fuente, new AdvisorStats());

            CombinedFuenteStats combinedStats = new CombinedFuenteStats(fuente, dealsCount,
                    statsFuente.getCita(), statsFuente.getVisita(), statsFuente.getNegociacion(),
                    statsFuente.getApartado(), statsFuente.getGanado());

            float cita = (float) statsFuente.getCita()/dealsCount*100;
            float visita = (float) statsFuente.getVisita()/dealsCount*100;
            float negociacion = (float) statsFuente.getNegociacion()/dealsCount*100;
            float apartado = (float) statsFuente.getApartado()/dealsCount*100;
            float ganado = (float) statsFuente.getGanado()/dealsCount*100;

            CombinedFuenteConversionStats combinedStatsConversionFuente = new CombinedFuenteConversionStats(fuente, dealsCount,
                    cita, visita, negociacion,
                    apartado, ganado);

            combinedFuenteStatsList.add(combinedStats);
            combinedFuenteConversionStatsList.add(combinedStatsConversionFuente);

        }

        // Pasar la lista combinada al modelo
        model.addAttribute("combinedAdvisorStats", combinedList);
        model.addAttribute("combinedAdvisorConversionStats", combinedConversionList);

        model.addAttribute("combinedFuenteStats", combinedFuenteStatsList);
        model.addAttribute("combinedFuenteStatsConversion", combinedFuenteConversionStatsList);

        // Inicializar los totales
        int totalDeals = 0;
        int totalCitas = 0;
        int totalVisitas = 0;
        int totalNegociaciones = 0;
        int totalApartados = 0;
        int totalWonDeals = 0;

        // Calcular totales
        for (CombinedAdvisorStats stat : combinedList) {
            totalDeals += stat.getDeals();
            totalCitas += stat.getCita();
            totalVisitas += stat.getVisita();
            totalNegociaciones += stat.getNegociacion();
            totalApartados += stat.getApartado();
            totalWonDeals += stat.getGanado();
        }

        // Inicializar los totales
        int totalDealsFuente = 0;
        int totalCitasFuente = 0;
        int totalVisitasFuente = 0;
        int totalNegociacionesFuente = 0;
        int totalApartadosFuente = 0;
        int totalWonDealsFuente = 0;

        for(CombinedFuenteStats stat : combinedFuenteStatsList){
            totalDealsFuente += stat.getDeals();
            totalCitasFuente += stat.getCita();
            totalVisitasFuente += stat.getVisita();
            totalNegociacionesFuente += stat.getNegociacion();
            totalApartadosFuente += stat.getApartado();
            totalWonDealsFuente += stat.getGanado();
        }

        // Inicializar los totales
        float citas = (float) totalCitas/totalDeals*100 ;
        float visitas = (float) totalVisitas/totalDeals*100;
        float negociaciones = (float) totalNegociaciones/totalDeals*100;
        float apartados = (float) totalApartados/totalDeals*100;
        float wonDeals = (float) totalWonDeals/totalDeals*100;

        // Inicializar los totales
        float citasFuente = (float) totalCitasFuente/totalDealsFuente*100 ;
        float visitasFuente = (float) totalVisitasFuente/totalDealsFuente*100;
        float negociacionesFuente = (float) totalNegociacionesFuente/totalDealsFuente*100;
        float apartadosFuente = (float) totalApartadosFuente/totalDealsFuente*100;
        float wonDealsFuente = (float) totalWonDealsFuente/totalDealsFuente*100;

        // Pasar los totales al modelo
        model.addAttribute("totalDeals", totalDeals);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalVisitas", totalVisitas);
        model.addAttribute("totalNegociaciones", totalNegociaciones);
        model.addAttribute("totalApartados", totalApartados);
        model.addAttribute("totalWonDeals", totalWonDeals);

        // Pasar los totales al modelo
        model.addAttribute("totalDealsFuente", totalDealsFuente);
        model.addAttribute("totalCitasFuente", totalCitasFuente);
        model.addAttribute("totalVisitasFuente", totalVisitasFuente);
        model.addAttribute("totalNegociacionesFuente", totalNegociacionesFuente);
        model.addAttribute("totalApartadosFuente", totalApartadosFuente);
        model.addAttribute("totalWonDealsFuente", totalWonDealsFuente);

        model.addAttribute("citas", citas);
        model.addAttribute("visitas", visitas);
        model.addAttribute("negociaciones", negociaciones);
        model.addAttribute("apartados", apartados);
        model.addAttribute("wonDeals", wonDeals);

        model.addAttribute("citasFuente", citasFuente);
        model.addAttribute("visitasFuente", visitasFuente);
        model.addAttribute("negociacionesFuente", negociacionesFuente);
        model.addAttribute("apartadosFuente", apartadosFuente);
        model.addAttribute("wonDealsFuente", wonDealsFuente);

        // Recopilación de actividades por asesor y por fecha
        Map<String, Map<String, Integer>> actividadesPorAsesorYFecha = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<ActivitiesData> activitiesList = new ArrayList<>();
        List<String> asesoresList = new ArrayList<>();
        List<Integer> processedIds = new ArrayList<>();

        for (CombinedAdvisorStats asesor : combinedList) {
            asesoresList.add(asesor.getAdvisor());

            for (DealsData deal : filteredDeals) {
                if(deal.getUserId().getId() != 12918702) {
                    int userId = deal.getUserId().getId();

                    if (!processedIds.contains(userId)) {
                        processedIds.add(userId);
                    }
                }
            }
        }

        for (Integer userId : processedIds) {
            activitiesList.addAll(pipedriveService.getAllActivities(startDate, endDate, userId));
        }

        for (ActivitiesData activity : activitiesList) {
            //System.out.println(activity.getUpdateTime() + " " + activity.getOwnerName());
            for(String asesor : asesoresList) {
                // Si el asesor no está en el mapa, añadirlo con un nuevo mapa de fechas y actividades
                actividadesPorAsesorYFecha.putIfAbsent(asesor, new HashMap<>());

                if (activity.getOwnerName().equals(asesor)) {
                    LocalDateTime dateTime = LocalDateTime.parse(activity.getDoneTime(), formatter);
                    String fecha = dateTime.format(dateFormatter); // Formato "yyyy-MM-dd"
                    // Sumar la actividad a la fecha correspondiente
                    actividadesPorAsesorYFecha.get(asesor).merge(fecha, 1, Integer::sum);
                }
            }
        }
        // Preparar los datos para Highcharts
        List<String> fechas = new ArrayList<>();
        List<Map<String, Object>> series = new ArrayList<>();

        // Recolectar todas las fechas únicas
        List<String> finalFechas = fechas;
        actividadesPorAsesorYFecha.values().forEach(map -> finalFechas.addAll(map.keySet()));
        // Filtrar las fechas dentro del rango
        fechas = fechas.stream()
                .distinct()
                .map(fecha -> LocalDate.parse(fecha)) // Convertir las cadenas a LocalDate
                .filter(fecha -> (fecha.isEqual(startDate) || fecha.isAfter(startDate)) && (fecha.isEqual(endDate) || fecha.isBefore(endDate))) // Filtrar las fechas dentro del rango
                .sorted()
                .map(LocalDate::toString) // Convertir de nuevo a String si es necesario
                .collect(Collectors.toList());

        // Crear las series para cada asesor
        for (Map.Entry<String, Map<String, Integer>> entry : actividadesPorAsesorYFecha.entrySet()) {
            String asesor = entry.getKey();
            Map<String, Integer> actividadesPorFecha = entry.getValue();

            // Preparar la data para este asesor con valores alineados a las fechas
            List<Integer> data = new ArrayList<>();
            for (String fecha : fechas) {
                data.add(actividadesPorFecha.getOrDefault(fecha, 0));
            }

            // Agregar la serie
            series.add(Map.of("name", asesor, "data", data));
        }

        model.addAttribute("fechas", fechas);
        model.addAttribute("series", series);

        return "comercial";
    }

    @Data
    @AllArgsConstructor
    public static class CombinedAdvisorStats {
        private String advisor;
        private int deals;
        private int cita;
        private int visita;
        private int negociacion;
        private int apartado;
        private int ganado;
    }

    @Data
    @AllArgsConstructor
    public static class CombinedAdvisorConversionStats {
        private String advisor;
        private int deals;
        private float cita;
        private float visita;
        private float negociacion;
        private float apartado;
        private float ganado;
    }

    @Data
    @AllArgsConstructor
    public static class CombinedFuenteStats {
        private String fuente;
        private int deals;
        private int cita;
        private int visita;
        private int negociacion;
        private int apartado;
        private int ganado;
    }

    @Data
    @AllArgsConstructor
    public static class CombinedFuenteConversionStats {
        private String fuente;
        private int deals;
        private float cita;
        private float visita;
        private float negociacion;
        private float apartado;
        private float ganado;
    }

    @Data
    @AllArgsConstructor
    public static class CombinedCampaign {
        private String campaign;
        private int deals;
        private int cita;
        private int visita;
        private int negociacion;
        private int apartado;
        private int ganado;
    }

    @Data
    @AllArgsConstructor
    public static class CombinedCampaignConversionStats {
        private String campaign;
        private int deals;
        private float cita;
        private float visita;
        private float negociacion;
        private float apartado;
        private float ganado;
    }

    @Getter
    @Data
    public static class AdvisorStats {
        public int cita = 0;
        public int visita = 0;
        public int negociacion = 0;
        public int apartado = 0;
        public int ganado = 0;


    }
}
