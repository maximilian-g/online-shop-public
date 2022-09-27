package com.online.shop.api;

import com.online.shop.service.impl.ReportService;
import com.online.shop.service.util.InputResource;
import com.online.shop.service.util.Interval;
import com.online.shop.service.util.report.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportsRestController extends BaseRestController {

    private final ReportService reportService;

    @Autowired
    public ReportsRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/sales_report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> getSalesReport(@RequestParam(name = "startDate")
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                 @RequestParam(name = "endDate")
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        return ResponseEntity.ok(reportService.getSalesByPeriodReport(new Interval(startDate, endDate)));
    }

    @GetMapping(value = "/sales_report/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getSalesExcelReport(@RequestParam(name = "startDate")
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                        @RequestParam(name = "endDate")
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        InputResource excelFile = reportService.getReportAsExcelFile(
                reportService.getSalesByPeriodReport(new Interval(startDate, endDate))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "filename=" + excelFile.getResourceName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFile.getContentLength())
                .body(excelFile.getInputStreamResource());
    }

    @GetMapping(value = "/item_quantity_report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> getItemQuantityReport(@RequestParam(name = "startDate")
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                        @RequestParam(name = "endDate")
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        return ResponseEntity.ok(reportService
                .getItemQuantityDifferenceByPeriod(new Interval(startDate, endDate)));
    }

    @GetMapping(value = "/item_quantity_report/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getItemQuantityExcelReport(@RequestParam(name = "startDate")
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                               @RequestParam(name = "endDate")
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        InputResource excelFile = reportService.getReportAsExcelFile(reportService
                .getItemQuantityDifferenceByPeriod(new Interval(startDate, endDate)));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "filename=" + excelFile.getResourceName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFile.getContentLength())
                .body(excelFile.getInputStreamResource());
    }

    @GetMapping(value = "/sales_diff_report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> getSalesDiffReport(@RequestParam(name = "firstStartDate")
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstStartDate,
                                                     @RequestParam(name = "firstEndDate")
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstEndDate,
                                                     @RequestParam(name = "secondStartDate")
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondStartDate,
                                                     @RequestParam(name = "secondEndDate")
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondEndDate) {
        return ResponseEntity.ok(reportService.getOrderDiffBetweenPeriods(
                new Interval(firstStartDate, firstEndDate),
                new Interval(secondStartDate, secondEndDate)
        ));
    }

    @GetMapping(value = "/sales_diff_report/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getSalesDiffExcelReport(@RequestParam(name = "firstStartDate")
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstStartDate,
                                                            @RequestParam(name = "firstEndDate")
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date firstEndDate,
                                                            @RequestParam(name = "secondStartDate")
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondStartDate,
                                                            @RequestParam(name = "secondEndDate")
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date secondEndDate) {
        InputResource excelFile = reportService.getReportAsExcelFile(reportService.getOrderDiffBetweenPeriods(
                new Interval(firstStartDate, firstEndDate),
                new Interval(secondStartDate, secondEndDate)
        ));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "filename=" + excelFile.getResourceName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFile.getContentLength())
                .body(excelFile.getInputStreamResource());
    }

    @GetMapping(value = "/active_users_report", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Report> getActiveUsersReport(@RequestParam(name = "startDate")
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                       @RequestParam(name = "endDate")
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        return ResponseEntity.ok(reportService
                .getUserStatisticsOnPeriod(new Interval(startDate, endDate)));
    }

    @GetMapping(value = "/active_users_report/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getActiveUsersExcelReport(@RequestParam(name = "startDate")
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                              @RequestParam(name = "endDate")
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        InputResource excelFile = reportService.getReportAsExcelFile(reportService
                .getUserStatisticsOnPeriod(new Interval(startDate, endDate)));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "filename=" + excelFile.getResourceName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFile.getContentLength())
                .body(excelFile.getInputStreamResource());
    }

}
