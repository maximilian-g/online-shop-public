package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.impl.ReportService;
import com.online.shop.service.util.Interval;
import com.online.shop.service.util.report.ExportOption;
import com.online.shop.service.util.report.Report;
import com.online.shop.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(path = "/reports")
public class ReportsController extends BaseController {

    private final ReportService reportService;

    protected ReportsController(UserService userService, ReportService reportService) {
        super(userService, LoggerFactory.getLogger(ReportsController.class));
        this.reportService = reportService;
    }

    // page where all available reports are shown, links are provided
    @GetMapping
    public String getReportPage(Model model) {
        addAuthAttribute(model);
        return "reportIndex";
    }

    @GetMapping("/sales_report")
    public String getSalesReport(Model model,
                                 @RequestParam(name = "startDate", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                 @RequestParam(name = "endDate", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        addAuthAttribute(model);
        model.addAttribute("reportName", "sales_report");
        if(startDate != null && endDate != null) {
            Interval interval = new Interval(startDate, endDate);
            Report report = reportService.getSalesByPeriodReport(interval);
            processExportOptions(report.getExportOptions(), interval, "sales_report");
            model.addAttribute("report", report);
            return "report";
        }
        return "reportByPeriod";
    }

    @GetMapping("/item_quantity_report")
    public String getItemQuantityReport(Model model,
                                        @RequestParam(name = "startDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                        @RequestParam(name = "endDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        addAuthAttribute(model);
        model.addAttribute("reportName", "item_quantity_report");
        if(startDate != null && endDate != null) {
            Interval interval = new Interval(startDate, endDate);
            Report report = reportService.getItemQuantityDifferenceByPeriod(interval);
            processExportOptions(report.getExportOptions(), interval, "item_quantity_report");
            model.addAttribute("report", report);
            return "report";
        }
        return "reportByPeriod";
    }

    @GetMapping("/sales_diff_report")
    public String getSalesDiffReport(Model model,
                                        @RequestParam(name = "firstStartDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstStartDate,
                                        @RequestParam(name = "firstEndDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstEndDate,
                                        @RequestParam(name = "secondStartDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondStartDate,
                                        @RequestParam(name = "secondEndDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondEndDate) {
        addAuthAttribute(model);
        model.addAttribute("reportName", "sales_diff_report");
        if(firstStartDate != null && firstEndDate != null && secondStartDate != null && secondEndDate != null) {
            Interval firstInterval = new Interval(firstStartDate, firstEndDate);
            Interval secondInterval = new Interval(secondStartDate, secondEndDate);
            Report report = reportService.getOrderDiffBetweenPeriods(
                    firstInterval,
                    secondInterval
            );
            processExportOptions(report.getExportOptions(), firstInterval, secondInterval, "sales_diff_report");
            model.addAttribute("report", report);
            return "report";
        }
        return "reportByPeriodDiff";
    }

    @GetMapping("/active_users_report")
    public String getActiveUsersReportReport(Model model,
                                        @RequestParam(name = "startDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                        @RequestParam(name = "endDate", required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        addAuthAttribute(model);
        model.addAttribute("reportName", "active_users_report");
        if(startDate != null && endDate != null) {
            Interval interval = new Interval(startDate, endDate);
            Report report = reportService.getUserStatisticsOnPeriod(interval);
            processExportOptions(report.getExportOptions(), interval, "active_users_report");
            model.addAttribute("report", report);
            return "report";
        }
        return "reportByPeriod";
    }

    private void processExportOptions(List<ExportOption> exportOptions,
                                      Interval interval,
                                      String reportName) {
        String queryParams = "?startDate=" + URLEncoder.encode(DateUtil.formatDateTime(interval.getStartDate()), StandardCharsets.UTF_8) +
                "&endDate=" + URLEncoder.encode(DateUtil.formatDateTime(interval.getEndDate()), StandardCharsets.UTF_8);
        setLinks(exportOptions, reportName, queryParams);
    }

    private void processExportOptions(List<ExportOption> exportOptions,
                                      Interval firstInterval,
                                      Interval secondInterval,
                                      String reportName) {
        String queryParams = "?firstStartDate=" + URLEncoder.encode(DateUtil.formatDateTime(firstInterval.getStartDate()), StandardCharsets.UTF_8) +
                "&firstEndDate=" + URLEncoder.encode(DateUtil.formatDateTime(firstInterval.getEndDate()), StandardCharsets.UTF_8) +
                "&secondStartDate=" + URLEncoder.encode(DateUtil.formatDateTime(secondInterval.getStartDate()), StandardCharsets.UTF_8) +
                "&secondEndDate=" + URLEncoder.encode(DateUtil.formatDateTime(secondInterval.getEndDate()), StandardCharsets.UTF_8);
        setLinks(exportOptions, reportName, queryParams);
    }

    private void setLinks(List<ExportOption> exportOptions,
                          String reportName,
                          String queryParams) {
        for (ExportOption exportOption : exportOptions) {
            exportOption.setLink("/" + reportName +
                    "/" + exportOption.getExportType().getType() + queryParams);
        }
    }

}
