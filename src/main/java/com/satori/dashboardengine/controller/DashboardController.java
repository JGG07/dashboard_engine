package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.ActivitiesData;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import lombok.AllArgsConstructor;
import lombok.Data;
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

        List<DealsData> filteredDeals = pipedriveService.getDealsStart(startDate, endDate);
        while (true) {
            LocalDate date = null;

            for (DealsData deal : filteredDeals) {
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

        return "mercadeo"; // Retorna la vista con los datos filtrados
    }

    /**
     *
     * @param model
     * @return
     */
    @GetMapping("/comercial")
    public String comerical(Model model) {
        log.info("*************** COMERCIAL ***************");

        // Establecer valores predeterminados para fechas si no se proporcionan
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Trae los deals conforme a los valores predeterminados de fechas
        List<DealsData> dealsDataList = pipedriveService.getDealsStart(startDate, endDate);
        List<Map.Entry<String, Integer>> sortedDealsByAdvisorList = sortedDealsByAdvisor(dealsDataList);
        List<CombinedAdvisorStats> combinedList = combinedList(sortedDealsByAdvisorList);
        List<DealsData> filteredDealsByStageChange = filteredDealsByStageChange(dealsDataList, startDate, endDate);
        List<String> datesList = datesList(combinedList, filteredDealsByStageChange, startDate, endDate, formatter);

        // Inicializar los totales
        int totalDeals = 0;
        int totalCitas = 0;
        int totalVisitas = 0;
        int totalNegociaciones = 0;
        int totalApartados = 0;
        int totalWonDeals = 0;

        // Ccalcular totales
        for (CombinedAdvisorStats stats : combinedList) {
            totalDeals += stats.getDeals();
            totalCitas += stats.getCita();
            totalVisitas += stats.getVisita();
            totalNegociaciones += stats.getNegociacion();
            totalApartados += stats.getApartado();
            totalWonDeals += stats.getGanado();
        }

        model.addAttribute("fechas", datesList);
        model.addAttribute("series", series(datesList));

        // Pasar los totales al modelo
        model.addAttribute("totalDeals", totalDeals);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalVisitas", totalVisitas);
        model.addAttribute("totalNegociaciones", totalNegociaciones);
        model.addAttribute("totalApartados", totalApartados);
        model.addAttribute("totalWonDeals", totalWonDeals);

        // Pasar la lista de Deals/Asesor
        model.addAttribute("sortedDealsByAdvisor", sortedDealsByAdvisorList);

        // Pasar la lista combinada al modelo
        model.addAttribute("combinedAdvisorStats", combinedList);

        return "comercial";
    }

    public List<Map.Entry<String, Integer>> sortedDealsByAdvisor(List<DealsData> dealsDataList) {

        // Crear un mapa para almacenar la suma de deals por asesor
        Map<String, Integer> dealsByAdvisor = new HashMap<>();

        // Iterar sobre la lista de dealsDataList
        for (DealsData deal : dealsDataList) {
            String advisor = deal.getOwnerName();  // Suponiendo que getOwnerName() devuelve el nombre del asesor
            dealsByAdvisor.put(advisor, dealsByAdvisor.getOrDefault(advisor, 0) + 1);
        }

        // Ordenar el mapa por número de deals en orden descendente

        return dealsByAdvisor.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();
    }

    Map<String, AdvisorStats> advisorStatsMap = new HashMap<>();

    public List<DealsData> filteredDealsByStageChange(List<DealsData> dealsDataList, LocalDate startDate, LocalDate endDate){
        LocalDate date;
        List<DealsData> filteredDealsByStageChange = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (DealsData deal : dealsDataList) {
            String addTime = deal.getStageChangeTime();

            // Procesar el caso donde addTime no es null
            LocalDateTime dateTime = LocalDateTime.parse(addTime, formatter);

            // Restar 6 horas
            LocalDateTime adjustedTime = dateTime.minusHours(6);
            date = adjustedTime.toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                filteredDealsByStageChange.add(deal);
            }
        }

        for (DealsData deal : filteredDealsByStageChange) {
            String advisor = deal.getOwnerName();
            AdvisorStats stats = advisorStatsMap.getOrDefault(advisor, new AdvisorStats());

            if (deal.getStageId() == 8) {
                stats.cita++;
                //System.out.println(deal.getPersonName() + " " + deal.getStageId() + " " + deal.getOwnerName() + " " + deal.getAddTime() + " " + deal.getStageChangeTime());
            }
            if (deal.getStageId() == 9) {
                stats.cita++;
                stats.visita++;
                //System.out.println(deal.getPersonName() + " " + deal.getStageId() + " " + deal.getOwnerName() + " " + deal.getAddTime() + " " + deal.getStageChangeTime());

            }
            if (deal.getStageId() == 10) {
                stats.cita++;
                stats.visita++;
                stats.negociacion++;
                //System.out.println(deal.getPersonName() + " " + deal.getStageId() + " " + deal.getOwnerName() + " " + deal.getAddTime() + " " + deal.getStageChangeTime());

            }
            if (deal.getStageId() == 11) {
                stats.cita++;
                stats.visita++;
                stats.negociacion++;
                stats.apartado++;
                //System.out.println(deal.getPersonName() + " " + deal.getStageId() + " " + deal.getOwnerName() + " " + deal.getAddTime() + " " + deal.getStageChangeTime());

            }
            if (deal.getStatus().equals("won")) {
                stats.ganado++;
                //System.out.println(deal.getPersonName() + " " + deal.getStageId() + " " + deal.getOwnerName() + " " + deal.getAddTime() + " " + deal.getStageChangeTime());

            }

            advisorStatsMap.put(advisor, stats);
        }

        return filteredDealsByStageChange;
    }

    public List<CombinedAdvisorStats> combinedList(List<Map.Entry<String, Integer>> sortedDealsByAdvisor) {

        // Crear una lista para almacenar la combinación de ambos
        List<CombinedAdvisorStats> combinedList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedDealsByAdvisor) {
            String advisor = entry.getKey();
            int dealsCount = entry.getValue();

            AdvisorStats stats = advisorStatsMap.getOrDefault(advisor, new AdvisorStats());

            CombinedAdvisorStats combinedStats = new CombinedAdvisorStats(advisor, dealsCount,
                    stats.getCita(), stats.getVisita(), stats.getNegociacion(),
                    stats.getApartado(), stats.getGanado());

            combinedList.add(combinedStats);
        }
        return combinedList;
    }

    Map<String, Map<String, Integer>> actividadesPorAsesorYFecha = new HashMap<>();

    public List<String> datesList(List<CombinedAdvisorStats> combinedList, List<DealsData> filteredDealsByStageChange, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter){
        List<String> datesList = new ArrayList<>();

        // Recopilación de actividades por asesor y por fecha
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<ActivitiesData> activitiesList = new ArrayList<>();
        List<String> asesoresList = new ArrayList<>();
        List<Integer> processedIds = new ArrayList<>();

        for (CombinedAdvisorStats asesor : combinedList) {
            asesoresList.add(asesor.getAdvisor());

            for (DealsData deal : filteredDealsByStageChange) {
                int userId = deal.getUserId().getId();

                if (!processedIds.contains(userId)) {
                    processedIds.add(userId);
                }
            }
        }

        for (Integer userId : processedIds) {
            activitiesList.addAll(pipedriveService.getAllActivities(startDate, endDate, userId));
        }

        for (ActivitiesData activity : activitiesList) {
            System.out.println(activity.getUpdateTime() + " " + activity.getOwnerName());
            for(String asesor : asesoresList) {
                // Si el asesor no está en el mapa, añadirlo con un nuevo mapa de fechas y actividades
                actividadesPorAsesorYFecha.putIfAbsent(asesor, new HashMap<>());

                if (activity.getOwnerName().equals(asesor)) {
                    LocalDateTime dateTime = LocalDateTime.parse(activity.getUpdateTime(), formatter);
                    String fecha = dateTime.format(dateFormatter); // Formato "yyyy-MM-dd"
                    // Sumar la actividad a la fecha correspondiente
                    actividadesPorAsesorYFecha.get(asesor).merge(fecha, 1, Integer::sum);
                }
            }
        }

        // Preparar los datos para Highcharts
        List<String> fechas = new ArrayList<>();

        // Recolectar todas las fechas únicas
        List<String> finalFechas = fechas;
        actividadesPorAsesorYFecha.values().forEach(map -> finalFechas.addAll(map.keySet()));
        fechas = fechas.stream().distinct().sorted().collect(Collectors.toList());

        return datesList;
    }

    public List<Map<String, Object>> series(List<String> fechas){

        List<Map<String, Object>> series = new ArrayList<>();
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

        for(int i = 0; i < series.size(); i++) {
            System.out.println(series.get(i) + " " + fechas.get(i));
        }
        return series;
    }


    @Data
    @AllArgsConstructor
    // Clase auxiliar para combinar los datos
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
    public static class AdvisorStats {
        public int cita = 0;
        public int visita = 0;
        public int negociacion = 0;
        public int apartado = 0;
        public int ganado = 0;
    }
}
