package com.satori.dashboardengine.controller;

import com.satori.dashboardengine.dto.Deals;
import com.satori.dashboardengine.dto.DealsData;
import com.satori.dashboardengine.service.PipedriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DashboardController {

    @Autowired
    private PipedriveService pipedriveService;

    @GetMapping("/dashboard")
    public String viewData(Model model) {
        Deals deals = pipedriveService.getDeals();

        // Procesar conteos de tratos por fecha y etapa
        Map<String, Integer> dealsCountByDate = pipedriveService.getDealsCountByDate(deals);
        Map<String, Integer> stageContactados = pipedriveService.getStageDealsByDate(deals, 7);
        Map<String, Integer> stageInteresados = pipedriveService.getStageDealsByDate(deals, 6);
        Map<String, Integer> stageCita = pipedriveService.getStageDealsByDate(deals, 8);
        Map<String, Integer> stageVisita = pipedriveService.getStageDealsByDate(deals, 9);
        Map<String, Integer> stageNegociacion = pipedriveService.getStageDealsByDate(deals, 10);
        Map<String, Integer> stageApartado = pipedriveService.getStageDealsByDate(deals, 11);
        Map<String, Integer> wonDealsCountByDate = pipedriveService.getDealsWonCountByDate(deals);

        // Recopilar razones de pérdida
        Map<String, Integer> lostReasons = new HashMap<>();
        for (DealsData deal : deals.getData()) {
            if ("lost".equalsIgnoreCase(deal.getStatus())) {
                String reason = deal.getLostReason();
                if (reason != null && !reason.isEmpty()) {
                    lostReasons.merge(reason, 1, Integer::sum);
                }
            }
        }

        // Ordenar razones de pérdida de mayor a menor
        Map<String, Integer> sortedLostReasons = lostReasons.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Calcular el total de pérdidas
        int totalLostReasons = sortedLostReasons.values().stream().mapToInt(Integer::intValue).sum();

        // Calcular el porcentaje de pérdida para cada razón
        Map<String, String> reasonsWithPercentages = sortedLostReasons.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int count = entry.getValue();
                            double percentage = (count * 100.0) / totalLostReasons;
                            return String.format("%.2f%%", percentage);
                        }
                ));

        // Agrupar tratos por fuente, etapa y status
        Map<String, Map<String, Integer>> dealsBySource = new HashMap<>();
        Map<String, Integer> totalDealsBySource = new HashMap<>();
        Map<String, Integer> openDealsBySource = new HashMap<>();
        Map<String, Integer> lostDealsBySource = new HashMap<>();
        Map<String, Integer> wonDealsBySource = new HashMap<>();

        int totalWonDeals = 0;
        int totalOpenDeals = 0;
        int totalLostDeals = 0;

        for (DealsData deal : deals.getData()) {
            String sourceName = pipedriveService.getFuenteName(deal.getFuente());
            String stage = pipedriveService.getStageName(deal.getStageId());
            String status = deal.getStatus();

            dealsBySource.computeIfAbsent(sourceName, k -> new HashMap<>()).merge(stage, 1, Integer::sum);
            totalDealsBySource.merge(sourceName, 1, Integer::sum);

            switch (status) {
                case "open":
                    openDealsBySource.merge(sourceName, 1, Integer::sum);
                    totalOpenDeals++;
                    break;
                case "lost":
                    lostDealsBySource.merge(sourceName, 1, Integer::sum);
                    totalLostDeals++;
                    break;
                case "won":
                    wonDealsBySource.merge(sourceName, 1, Integer::sum);
                    totalWonDeals++;
                    break;
            }
        }

        // Ordenar dealsBySource de mayor a menor según el número total de tratos
        Map<String, Map<String, Integer>> sortedDealsBySource = dealsBySource.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(totalDealsBySource.get(entry2.getKey()), totalDealsBySource.get(entry1.getKey())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Generar el rango de fechas del 1 de julio al 31 de julio
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 7, 1);
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(), 7, 31);
        List<String> dates = IntStream.rangeClosed(0, (int) startDate.until(endDate).getDays())
                .mapToObj(startDate::plusDays)
                .map(LocalDate::toString)
                .collect(Collectors.toList());

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
        int totalDeals = counts.stream().mapToInt(Integer::intValue).sum();
        int totalContactados = contactados.stream().mapToInt(Integer::intValue).sum();
        int totalInteresados = interesados.stream().mapToInt(Integer::intValue).sum();
        int totalCitas = citas.stream().mapToInt(Integer::intValue).sum();
        int totalVisitas = visitas.stream().mapToInt(Integer::intValue).sum();
        int totalNegociaciones = negociaciones.stream().mapToInt(Integer::intValue).sum();
        int totalApartados = apartados.stream().mapToInt(Integer::intValue).sum();

        // Agregar datos al modelo para ser utilizados en la vista Thymeleaf
        model.addAttribute("dealsDates", dates);
        model.addAttribute("dealsCounts", counts);
        model.addAttribute("contactados", contactados);
        model.addAttribute("interesados", interesados);
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
        model.addAttribute("allDealdsData", deals.getData());

        return "index"; // Nombre de la vista Thymeleaf
    }
}
