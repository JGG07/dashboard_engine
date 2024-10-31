package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.ActivitiesData;
import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class FilterController {

    @Autowired
    private PipedriveService pipedriveService;

    /**
     *
     * @param source
     * @param ownerName
     * @param model
     * @return
     */
    @PostMapping("/mercadeoFilter")
    public String viewData(@RequestParam(value = "source", required = false) List<String> source,
                           @RequestParam(value = "ownerName", required = false) List<String> ownerName,
                           @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                           @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                           Model model) {

        // Si source o ownerName son nulos, usa "all" como valor predeterminado.
        if (source == null || source.isEmpty()) {
            source = List.of("all"); // Usa una lista con "all" como valor predeterminado
        }
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = List.of("all"); // Usa una lista con "all" como valor predeterminado
        }

        // Establecer valores predeterminados para fechas si no se proporcionan
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // Primer día del mes actual
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }


        // Verificar si todas las condiciones son verdaderas
        boolean allConditionsTrue = "all".equals(source) && "all".equals(ownerName) && startDate.equals(LocalDate.now().withDayOfMonth(1)) && endDate.equals(LocalDate.now());

        if (allConditionsTrue) {
            return "redirect:/mercadeo"; // Redirigir a "/mercadeo" si todas las condiciones son verdaderas
        }

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

        while (true) {

            LocalDate date = null;
            Deals deals = pipedriveService.getDealsStart(start);

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

        // Supongamos que `source` y `ownerName` son List<String> en lugar de String
        List<String> finalSource = source != null ? source : List.of("all");
        List<String> finalOwnerName = ownerName != null ? ownerName : List.of("all");

        List<DealsData> finalFilteredDeals = filteredDeals.stream()
                .filter(deal -> (finalSource.contains("all") || finalSource.contains(deal.getFuente())) &&
                        (finalOwnerName.contains("all") || finalOwnerName.contains(deal.getOwnerName())))
                .collect(Collectors.toList());


        Map<String, Integer> dealsCountByDate = pipedriveService.getDealsCountByDate(finalFilteredDeals, startDate, endDate);
        Map<String, Integer> stageInteresados = pipedriveService.getStageDealsByDate(finalFilteredDeals, 6, startDate, endDate);
        Map<String, Integer> stageContactados = pipedriveService.getStageDealsByDate(finalFilteredDeals, 7, startDate, endDate);
        Map<String, Integer> stageCita = pipedriveService.getStageDealsByDate(finalFilteredDeals, 8, startDate, endDate);
        Map<String, Integer> stageVisita = pipedriveService.getStageDealsByDate(finalFilteredDeals, 9, startDate, endDate);
        Map<String, Integer> stageNegociacion = pipedriveService.getStageDealsByDate(finalFilteredDeals, 10, startDate, endDate);
        Map<String, Integer> stageApartado = pipedriveService.getStageDealsByDate(finalFilteredDeals, 11, startDate, endDate);

        Map<String, Integer> wonDealsCountByDate = pipedriveService.getDealsWonCountByDate(finalFilteredDeals);


        // Recopilar razones de pérdida
        Map<String, Integer> lostReasons = new HashMap<>();
        for (DealsData deal : finalFilteredDeals) {
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

        for (DealsData deal : finalFilteredDeals) {
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
        Map<String, Map<String, Integer>> dealsByStageAndStatus = pipedriveService.getDealsByStageAndStatus(finalFilteredDeals);

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

        // Agregar el valor seleccionado al modelo
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedOwner", ownerName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "mercadeo"; // Retorna la vista con los datos filtrados
    }


    /**
     *
     * @param source
     * @param ownerName
     * @param startDate
     * @param endDate
     * @param model
     * @return
     */
    @PostMapping("/filterComercial")
    public String comerical(@RequestParam(value = "source", required = false) List<String> source,
                            @RequestParam(value = "ownerName", required = false) List<String> ownerName,
                            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            Model model){

        log.info("************************ COMERCIAL ************************");

        // Establecer valores predeterminados para fechas si no se proporcionan

        // Si source o ownerName son nulos, usa "all" como valor predeterminado.
        if (source == null || source.isEmpty()) {
            source = List.of("all"); // Usa una lista con "all" como valor predeterminado
        }
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = List.of("all"); // Usa una lista con "all" como valor predeterminado
        }

        // Establecer valores predeterminados para fechas si no se proporcionan
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // Primer día del mes actual
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }


        // Verificar si todas las condiciones son verdaderas
        boolean allConditionsTrue = "all".equals(source) && "all".equals(ownerName) && startDate.equals(LocalDate.now().withDayOfMonth(1)) && endDate.equals(LocalDate.now());

        if (allConditionsTrue) {
            return "redirect:/comercial"; // Redirigir a "/mercadeo" si todas las condiciones son verdaderas
        }

        int start = 0;
        int LIMIT = 500;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<DealsData> filteredDeals = new ArrayList<>();

        boolean filterBySource = !source.contains("all");
        boolean filterByOwner = !ownerName.contains("all");

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


                // Filtrar por source, rango de fechas y asesor
                if (filterBySource && filterByOwner) {
                    // Filtrar por fuente, asesor y rango de fechas
                    if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                            source.contains(deal.getFuente()) && ownerName.contains(deal.getOwnerName())) {
                        filteredDeals.add(deal);
                    }
                } else if (filterBySource) {
                    // Filtrar solo por fuente y rango de fechas
                    if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                            source.contains(deal.getFuente())) {
                        filteredDeals.add(deal);
                    }
                } else if (filterByOwner) {
                    // Filtrar solo por asesor y rango de fechas
                    if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                            ownerName.contains(deal.getOwnerName())) {
                        filteredDeals.add(deal);
                    }
                } else {
                    // Filtrar solo por rango de fechas
                    if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                        filteredDeals.add(deal);
                    }
                }

            }

            assert date != null;
            if (date.isBefore(startDate)) {
                break;
            }
            start += LIMIT;
        }

        // Crear un mapa para almacenar la suma de deals por asesor
        Map<String, Integer> dealsByAdvisor = new HashMap<>();

        // Crear un mapa para almacenar la suma de deals por fuente
        Map<String, Integer> dealsByFuente = new HashMap<>();


        // Iterar sobre la lista de filteredDeals
        for (DealsData deal : filteredDeals) {
            String advisor = deal.getOwnerName();  // Suponiendo que getOwnerName() devuelve el nombre del asesor
            String fuente = pipedriveService.getFuenteName(deal.getFuente());

            dealsByAdvisor.put(advisor, dealsByAdvisor.getOrDefault(advisor, 0) + 1);
            dealsByFuente.put(fuente, dealsByFuente.getOrDefault(fuente, 0) + 1);

        }

        // Ordenar el mapa por número de deals en orden descendente por fuente
        List<Map.Entry<String, Integer>> sortedDealsByFuente = dealsByFuente.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();


        // Ordenar el mapa por número de deals en orden descendente
        List<Map.Entry<String, Integer>> sortedDealsByAdvisor = dealsByAdvisor.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        model.addAttribute("sortedDealsByFuente", sortedDealsByFuente);
        model.addAttribute("sortedDealsByAdvisor", sortedDealsByAdvisor);

        List<DealsData> filteredDealsByStageChange = new ArrayList<>();

        start = 0;

        while (true) {
            log.info("*************** SecondWhile ***************");
            Deals dealsData = pipedriveService.getDealsStart(start);

            if (!dealsData.getAdditionalData().getPagination().isMoreItems()) {
                break;
            }

            for (DealsData deal : dealsData.getData()) {
                String addTime = deal.getStageChangeTime();

                LocalDate date = null;

                if (addTime != null) {
                    // Procesar el caso donde addTime no es null
                    LocalDateTime dateTime = LocalDateTime.parse(addTime, formatter);

                    // Restar 6 horas
                    LocalDateTime adjustedTime = dateTime.minusHours(6);
                    date = adjustedTime.toLocalDate();

                    // Filtrar por source, rango de fechas y asesor
                    if (filterBySource && filterByOwner) {
                        // Filtrar por fuente, asesor y rango de fechas
                        if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                                source.contains(deal.getFuente()) && ownerName.contains(deal.getOwnerName())) {
                            filteredDealsByStageChange.add(deal);
                        }
                    } else if (filterBySource) {
                        // Filtrar solo por fuente y rango de fechas
                        if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                                source.contains(deal.getFuente())) {
                            filteredDealsByStageChange.add(deal);
                        }
                    } else if (filterByOwner) {
                        // Filtrar solo por asesor y rango de fechas
                        if (!date.isBefore(startDate) && !date.isAfter(endDate) &&
                                ownerName.contains(deal.getOwnerName())) {
                            filteredDealsByStageChange.add(deal);
                        }
                    } else {
                        // Filtrar solo por rango de fechas
                        if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                            filteredDealsByStageChange.add(deal);
                        }
                    }
                }
            }
            // Incrementar el valor de `start` para la próxima iteración
            start += LIMIT;
        }

        Map<String, DashboardController.AdvisorStats> advisorStatsMap = new HashMap<>();
        Map<String, DashboardController.AdvisorStats> fuenteStatsMap = new HashMap<>();

        DashboardController.AdvisorStats stats;
        DashboardController.AdvisorStats statsFuente;

        for (DealsData deal : filteredDealsByStageChange) {
            String advisor = deal.getOwnerName();
            String fuente = pipedriveService.getFuenteName(deal.getFuente());

            stats = advisorStatsMap.getOrDefault(advisor, new DashboardController.AdvisorStats());
            statsFuente = fuenteStatsMap.getOrDefault(fuente, new DashboardController.AdvisorStats());

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

            if(deal.getWonTime() != null) {
                LocalDate dateTime = LocalDate.parse(deal.getWonTime(), formatter);
                if (!dateTime.isBefore(startDate) && !dateTime.isAfter(endDate)) {
                    stats.ganado++;

                    statsFuente.ganado++;
                }
            }

//            System.out.println("citas: " + stats.getCita());
//            System.out.println("visitas: " + stats.getVisita());
//            System.out.println("negociaciones: " + stats.getNegociacion());
//            System.out.println("apartados: " + stats.getApartado());
//            System.out.println(advisorStatsMap);
            advisorStatsMap.put(advisor, stats);
            fuenteStatsMap.put(fuente, statsFuente);

        }

        // Crear una lista para almacenar la combinación de ambos
        List<DashboardController.CombinedAdvisorStats> combinedList = new ArrayList<>();
        List<DashboardController.CombinedFuenteStats> combinedFuenteStatsList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedDealsByAdvisor) {
            String advisor = entry.getKey();
            int dealsCount = entry.getValue();

            stats = advisorStatsMap.getOrDefault(advisor, new DashboardController.AdvisorStats());

            DashboardController.CombinedAdvisorStats combinedStats = new DashboardController.CombinedAdvisorStats(advisor, dealsCount,
                    stats.getCita(), stats.getVisita(), stats.getNegociacion(),
                    stats.getApartado(), stats.getGanado());

            combinedList.add(combinedStats);
        }

        for (Map.Entry<String, Integer> entry : sortedDealsByFuente) {

            String fuente = entry.getKey();
            int dealsCount = entry.getValue();

            statsFuente = fuenteStatsMap.getOrDefault(fuente, new DashboardController.AdvisorStats());

            DashboardController.CombinedFuenteStats combinedStats = new DashboardController.CombinedFuenteStats(fuente, dealsCount,
                    statsFuente.getCita(), statsFuente.getVisita(), statsFuente.getNegociacion(),
                    statsFuente.getApartado(), statsFuente.getGanado());

            combinedFuenteStatsList.add(combinedStats);
        }
        // Pasar la lista combinada al modelo
        model.addAttribute("combinedAdvisorStats", combinedList);
        model.addAttribute("combinedFuenteStats", combinedFuenteStatsList);

        // Inicializar los totales
        int totalDeals = 0;
        int totalCitas = 0;
        int totalVisitas = 0;
        int totalNegociaciones = 0;
        int totalApartados = 0;
        int totalWonDeals = 0;

// Calcular totales
        for (DashboardController.CombinedAdvisorStats stat : combinedList) {
            totalDeals += stat.getDeals();
            totalCitas += stat.getCita();
            totalVisitas += stat.getVisita();
            totalNegociaciones += stat.getNegociacion();
            totalApartados += stat.getApartado();
            totalWonDeals += stat.getGanado();
        }

// Pasar los totales al modelo
        model.addAttribute("totalDeals", totalDeals);
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalVisitas", totalVisitas);
        model.addAttribute("totalNegociaciones", totalNegociaciones);
        model.addAttribute("totalApartados", totalApartados);
        model.addAttribute("totalWonDeals", totalWonDeals);

        // Recopilación de actividades por asesor y por fecha
        Map<String, Map<String, Integer>> actividadesPorAsesorYFecha = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<ActivitiesData> activitiesList = new ArrayList<>();
        List<String> asesoresList = new ArrayList<>();
        List<Integer> processedIds = new ArrayList<>();

        for (DashboardController.CombinedAdvisorStats asesor : combinedList) {
            asesoresList.add(asesor.getAdvisor());

            for (DealsData deal : filteredDealsByStageChange) {
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
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;
        fechas = fechas.stream()
                .distinct()
                .map(fecha -> LocalDate.parse(fecha)) // Convertir las cadenas a LocalDate
                .filter(fecha -> (fecha.isEqual(finalStartDate) || fecha.isAfter(finalStartDate)) && (fecha.isEqual(finalEndDate) || fecha.isBefore(finalEndDate))) // Filtrar las fechas dentro del rango
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
                // Verificar si la fecha está en el rango de fechas
                LocalDate currentDate = LocalDate.parse(fecha); // Asumiendo que fecha está en formato 'YYYY-MM-DD'
                if ((startDate == null || !currentDate.isBefore(startDate)) &&
                        (endDate == null || !currentDate.isAfter(endDate))) {
                    data.add(actividadesPorFecha.getOrDefault(fecha, 0));
                } else {
                    data.add(0); // Añadir 0 si la fecha no está en el rango
                }
            }

            // Agregar la serie
            series.add(Map.of("name", asesor, "data", data));
        }


        model.addAttribute("fechas", fechas);
        model.addAttribute("series", series);

        // Agregar los valores seleccionados al modelo
        model.addAttribute("selectedSource", source != null ? source : Collections.emptyList());
        model.addAttribute("selectedOwner", ownerName != null ? ownerName : Collections.emptyList());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);



        return "comercial";
    }
}
