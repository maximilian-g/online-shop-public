package com.online.shop.service.impl;

import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.dto.OrderDto;
import com.online.shop.dto.UserDto;
import com.online.shop.repository.data.ItemQuantityDiffOnPeriodView;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.abstraction.LoggableService;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.exception.ExportException;
import com.online.shop.service.util.InputResource;
import com.online.shop.service.util.Interval;
import com.online.shop.service.util.report.CellType;
import com.online.shop.service.util.report.ExportOption;
import com.online.shop.service.util.report.ExportType;
import com.online.shop.service.util.report.Report;
import com.online.shop.service.util.report.ReportCell;
import com.online.shop.service.util.report.ReportGrid;
import com.online.shop.util.DateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService extends LoggableService {

    private final OrderService orderService;
    private final ItemService itemService;
    private final UserService userService;

    public ReportService(OrderService orderService, ItemService itemService, UserService userService) {
        super(LoggerFactory.getLogger(ReportService.class));
        this.orderService = orderService;
        this.itemService = itemService;
        this.userService = userService;
    }

    public Report getUserStatisticsOnPeriod(Interval interval) {
        if (!interval.isValid()) {
            throw new ExportException("End date must be after start date.");
        }
        Collection<UserDto> activeUsersOnInterval =
                userService.getActiveUsersOn(interval.getStartDate(), interval.getEndDate());

        String intervalStr = DateUtil.formatDate(interval.getStartDate()) +
                " - " + DateUtil.formatDate(interval.getEndDate());


        Report report = new Report();
        report.setReportFileName("Users_statistics_from_" +
                DateUtil.formatDate(interval.getStartDate()) +
                "_to_" + DateUtil.formatDate(interval.getEndDate()));
        report.setReportName("Active users statistics on " + intervalStr);
        ReportGrid grid = new ReportGrid();
        grid.setHeaders(Collections.emptyList());
        grid.setRows(Collections.emptyList());
        report.setMainGrid(grid);

        int registeredOnThisInterval = 0;
        int registeredAndOrderedOnThisInterval = 0;
        int activeOnThisInterval = activeUsersOnInterval.size();
        for (UserDto dto : activeUsersOnInterval) {
            try {
                Date createDate = DateUtil.getDateTimeFromString(dto.getRegisteredAt());
                if (createDate.after(interval.getStartDate()) && createDate.before(interval.getEndDate())) {
                    registeredOnThisInterval++;
                    if (!dto.getOrderIds().isEmpty()) {
                        registeredAndOrderedOnThisInterval++;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        report.getBeforeGrid().add("Users active on " + intervalStr +
                " : " + activeOnThisInterval);
        report.getBeforeGrid().add("Registered on " + intervalStr + " : " + registeredOnThisInterval);
        report.getBeforeGrid().add("Registered and ordered something on " + intervalStr +
                " : " + registeredAndOrderedOnThisInterval);
        report.getExportOptions().add(new ExportOption().setExportType(ExportType.EXCEL));

        return report;
    }

    public Report getOrderDiffBetweenPeriods(Interval firstInterval,
                                             Interval secondInterval) {
        if (!firstInterval.isValid() || !secondInterval.isValid()) {
            throw new ExportException("Interval(s) are not valid, check dates, end date must be after start date.");
        }
        if (firstInterval.intersects(secondInterval)) {
            throw new ExportException("Intervals are intersecting, choose different intervals");
        }

        String firstIntervalStr = DateUtil.formatDate(firstInterval.getStartDate()) +
                " - " + DateUtil.formatDate(firstInterval.getEndDate());
        String secondIntervalStr = DateUtil.formatDate(secondInterval.getStartDate()) +
                " - " + DateUtil.formatDate(secondInterval.getEndDate());

        Report report = new Report();
        report.setReportName("Sales difference between period " +
                firstIntervalStr + " and " + secondIntervalStr);

        Collection<OrderDto> firstResult =
                orderService.getPaidOrdersForPeriod(firstInterval.getStartDate(), firstInterval.getEndDate());
        Map<Long, List<ItemQuantityDto>> firstItemByIdMap = new HashMap<>();

        Collection<OrderDto> secondResult =
                orderService.getPaidOrdersForPeriod(secondInterval.getStartDate(), secondInterval.getEndDate());
        Map<Long, List<ItemQuantityDto>> secondItemByIdMap = new HashMap<>();

        IntegerHolder ordersCompletedForFirstInterval = new IntegerHolder(0);
        iterateOrdersAndSumCompleted(firstResult, firstItemByIdMap, ordersCompletedForFirstInterval);

        IntegerHolder ordersCompletedForSecondInterval = new IntegerHolder(0);
        iterateOrdersAndSumCompleted(secondResult, secondItemByIdMap, ordersCompletedForSecondInterval);

        report.setReportFileName("Sold_difference");

        report.getBeforeGrid().add("Orders completed on " + firstIntervalStr + ": " +
                ordersCompletedForFirstInterval.value);
        report.getBeforeGrid().add("Orders completed on " + secondIntervalStr + ": " +
                ordersCompletedForSecondInterval.value);

        report.getBeforeGrid().add("Completed " +
                (ordersCompletedForSecondInterval.value >= ordersCompletedForFirstInterval.value ?
                        " more or equal" : " less") +
                " than on " + firstIntervalStr);

        report.getBeforeGrid().add("Orders paid on " + firstIntervalStr + ": " + firstResult.size());
        report.getBeforeGrid().add("Orders paid on " + secondIntervalStr + ": " + secondResult.size());

        report.getBeforeGrid().add("Paid " +
                (secondResult.size() > firstResult.size() ? " more or equal" : " less") +
                " than on " + firstIntervalStr);

        Map<Long, Map<String, List<ItemQuantityDto>>> firstItemByIdGroupedByPriceMap =
                getItemByIdGroupedByPriceMap(firstItemByIdMap);
        Map<Long, Map<String, List<ItemQuantityDto>>> secondItemByIdGroupedByPriceMap =
                getItemByIdGroupedByPriceMap(secondItemByIdMap);

        boolean isInverse = firstItemByIdGroupedByPriceMap.size() > secondItemByIdGroupedByPriceMap.size();
        Map<Long, Map<String, List<ItemQuantityDto>>> mapToIterateThrough =
                isInverse ? firstItemByIdGroupedByPriceMap : secondItemByIdGroupedByPriceMap;
        Map<Long, Map<String, List<ItemQuantityDto>>> mapToCompareTo =
                isInverse ? secondItemByIdGroupedByPriceMap : firstItemByIdGroupedByPriceMap;

        ReportGrid grid = new ReportGrid();
        grid.setHeaders(List.of("Item name",
                "Total sold on " + firstIntervalStr,
                "Total sold on " + secondIntervalStr,
                "Difference",
                "Price"));
        List<List<ReportCell>> rows = new LinkedList<>();
        grid.setRows(rows);
        report.setMainGrid(grid);
        BigDecimal totalFirst = new BigDecimal(0);
        BigDecimal totalSecond = new BigDecimal(0);
        for (Map.Entry<Long, Map<String, List<ItemQuantityDto>>> entry : mapToIterateThrough.entrySet()) {
            for (Map.Entry<String, List<ItemQuantityDto>> innerEntry : entry.getValue().entrySet()) {
                List<ReportCell> row = new LinkedList<>();
                String rowPrice = "";
                BigDecimal rowPriceObj = new BigDecimal(0);
                long quantity = 0;
                BigDecimal quantityObj;
                for (ItemQuantityDto itemQuantityDto : innerEntry.getValue()) {
                    if (row.isEmpty()) {
                        row.add(new ReportCell().setValue(itemQuantityDto.getItem().getName()).setType(CellType.STRING));
                        rowPrice = itemQuantityDto.getItem().getPrice().getPriceFormatted();
                        rowPriceObj = new BigDecimal(rowPrice.replaceAll(",", ""));
                    }
                    quantity += itemQuantityDto.getQuantity();
                }
                quantityObj = new BigDecimal(quantity);
                if (isInverse) {
                    totalFirst = totalFirst.add(rowPriceObj.multiply(quantityObj));
                } else {
                    totalSecond = totalSecond.add(rowPriceObj.multiply(quantityObj));
                }
                Map<String, List<ItemQuantityDto>> priceItemMap = mapToCompareTo.get(entry.getKey());
                if (priceItemMap != null && priceItemMap.containsKey(rowPrice)) {
                    List<ItemQuantityDto> itemQuantityDtos = priceItemMap.get(rowPrice);
                    long anotherQuantity = itemQuantityDtos.stream().mapToLong(ItemQuantityDto::getQuantity).sum();
                    long diff = quantity - anotherQuantity;
                    if (isInverse) {
                        diff *= -1;
                        totalSecond = totalSecond.add(rowPriceObj.multiply(new BigDecimal(anotherQuantity)));
                        row.add(new ReportCell().setValue(Long.toString(quantity)).setType(CellType.STRING));
                        row.add(new ReportCell().setValue(Long.toString(anotherQuantity)).setType(CellType.STRING));
                    } else {
                        totalFirst = totalFirst.add(rowPriceObj.multiply(new BigDecimal(anotherQuantity)));
                        row.add(new ReportCell().setValue(Long.toString(anotherQuantity)).setType(CellType.STRING));
                        row.add(new ReportCell().setValue(Long.toString(quantity)).setType(CellType.STRING));
                    }

                    row.add(new ReportCell().setValue(Long.toString(diff)).setType(CellType.DIFFERENCE));

                } else {
                    if (isInverse) {
                        row.add(new ReportCell().setValue(Long.toString(quantity)).setType(CellType.STRING));
                        row.add(new ReportCell().setValue("0").setType(CellType.STRING));
                    } else {
                        row.add(new ReportCell().setValue("0").setType(CellType.STRING));
                        row.add(new ReportCell().setValue(Long.toString(quantity)).setType(CellType.STRING));
                    }
                    row.add(new ReportCell().setValue(Long.toString(isInverse ? -quantity : quantity))
                            .setType(CellType.DIFFERENCE));
                }


                row.add(new ReportCell().setValue(rowPrice).setType(CellType.MONEY));
                rows.add(row);
            }
        }
        rows.add(List.of(
                new ReportCell().setValue("Total:").setType(CellType.STRING),
                new ReportCell().setValue(PriceService.formatDecimal(totalFirst)).setType(CellType.MONEY),
                new ReportCell().setValue(PriceService.formatDecimal(totalSecond)).setType(CellType.MONEY),
                new ReportCell().setValue(totalSecond.subtract(totalFirst)
                        .setScale(2, RoundingMode.HALF_UP).toString())
                        .setType(CellType.DIFFERENCE)
        ));
        report.getExportOptions().add(new ExportOption().setExportType(ExportType.EXCEL));

        return report;
    }

    public Report getItemQuantityDifferenceByPeriod(Interval interval) {
        if (!interval.isValid()) {
            throw new ExportException("End date must be after start date.");
        }

        Report report = new Report();
        report.setReportName("Item quantity changes by period from " +
                DateUtil.formatDate(interval.getStartDate()) +
                " to " + DateUtil.formatDate(interval.getEndDate()));
        report.setReportFileName("Item_changes_from_" +
                DateUtil.formatDate(interval.getStartDate()) +
                "_to_" + DateUtil.formatDate(interval.getEndDate()));
        report.setBeforeGrid(new LinkedList<>());

        ReportGrid grid = new ReportGrid();
        grid.setHeaders(List.of("Item name",
                "Quantity on " + DateUtil.formatDate(interval.getStartDate()),
                "Quantity on " + DateUtil.formatDate(interval.getEndDate()),
                "Change"));
        List<List<ReportCell>> rows = new LinkedList<>();
        grid.setRows(rows);
        report.setMainGrid(grid);

        Collection<ItemQuantityDiffOnPeriodView> allItemsQuantityDiffForPeriod =
                itemService.getAllWithChangeOnPeriod(interval.getStartDate(), interval.getEndDate());

        for (ItemQuantityDiffOnPeriodView item : allItemsQuantityDiffForPeriod) {
            List<ReportCell> row = new LinkedList<>();
            row.add(new ReportCell().setValue(item.getItemName()).setType(CellType.STRING));
            row.add(new ReportCell().setValue(item.getQuantityOnStartDate().toString()).setType(CellType.STRING));
            row.add(new ReportCell().setValue(item.getQuantityOnEndDate().toString()).setType(CellType.STRING));
            row.add(new ReportCell().setValue(item.getDifference().toString()).setType(CellType.DIFFERENCE));
            rows.add(row);
        }
        report.getExportOptions().add(new ExportOption().setExportType(ExportType.EXCEL));

        return report;
    }

    public Report getSalesByPeriodReport(Interval interval) {
        if (!interval.isValid()) {
            throw new ExportException("End date must be after start date.");
        }
        Collection<OrderDto> result = orderService.getPaidOrdersForPeriod(interval.getStartDate(), interval.getEndDate());
        Map<Long, List<ItemQuantityDto>> itemByIdMap = new HashMap<>();
        IntegerHolder ordersCompleted = new IntegerHolder(0);
        iterateOrdersAndSumCompleted(result, itemByIdMap, ordersCompleted);

        Report report = new Report();

        report.setReportName("Sold by period from " +
                DateUtil.formatDate(interval.getStartDate()) +
                " to " + DateUtil.formatDate(interval.getEndDate()));
        report.setReportFileName("Sold_from_" +
                DateUtil.formatDate(interval.getStartDate()) +
                "_to_" + DateUtil.formatDate(interval.getEndDate()));
        report.setBeforeGrid(new LinkedList<>());

        report.getBeforeGrid().add("Orders paid: " + result.size() + ".");
        report.getBeforeGrid().add("Orders completed: " + ordersCompleted.value + ".");
        report.getBeforeGrid().add("Orders in process of delivery: " + (result.size() - ordersCompleted.value) + ".");
        populateReportWithMostPopularItem(report, itemByIdMap);

        BigDecimal totalForPeriod = new BigDecimal("0");
        Map<Long, Map<String, List<ItemQuantityDto>>> itemByIdGroupedByPriceMap =
                getItemByIdGroupedByPriceMap(itemByIdMap);

        ReportGrid grid = new ReportGrid();
        grid.setHeaders(List.of("Item name", "Quantity", "Sold by price", "Item cost"));
        List<List<ReportCell>> rows = new LinkedList<>();
        grid.setRows(rows);
        report.setMainGrid(grid);
        for (Map.Entry<Long, Map<String, List<ItemQuantityDto>>> entry : itemByIdGroupedByPriceMap.entrySet()) {
            for (Map.Entry<String, List<ItemQuantityDto>> innerEntry : entry.getValue().entrySet()) {
                List<ReportCell> row = new LinkedList<>();
                String rowPrice = "";
                long quantity = 0;
                for (ItemQuantityDto itemQuantityDto : innerEntry.getValue()) {
                    if (row.isEmpty()) {
                        row.add(new ReportCell().setValue(itemQuantityDto.getItem().getName()).setType(CellType.STRING));
                        rowPrice = itemQuantityDto.getItem().getPrice().getPriceFormatted();
                    }
                    quantity += itemQuantityDto.getQuantity();
                }
                row.add(new ReportCell().setValue(Long.toString(quantity)).setType(CellType.STRING));
                row.add(new ReportCell().setValue(rowPrice).setType(CellType.MONEY));
                BigDecimal cost = new BigDecimal(rowPrice.replaceAll(",", ""))
                        .multiply(new BigDecimal(quantity));
                totalForPeriod = totalForPeriod.add(cost);
                row.add(new ReportCell().setValue(PriceService.formatDecimal(cost)).setType(CellType.MONEY));
                rows.add(row);
            }
        }
        rows.add(List.of(ReportCell.emptyStringCell(),
                ReportCell.emptyStringCell(),
                new ReportCell().setValue("Total: ").setType(CellType.STRING),
                new ReportCell().setValue(PriceService.formatDecimal(totalForPeriod)).setType(CellType.MONEY)));
        report.getExportOptions().add(new ExportOption().setExportType(ExportType.EXCEL));

        return report;
    }

    public InputResource getReportAsExcelFile(Report report) {

        try (Workbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            int rowIndex = 0;
            Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(report.getReportFileName()));

            if (!report.getBeforeGrid().isEmpty()) {
                Row row = sheet.createRow(rowIndex++);
                Cell cell = row.createCell(0);
                cell.setCellValue("General report info:");
            }

            for (String value : report.getBeforeGrid()) {
                Row row = sheet.createRow(rowIndex++);
                Cell cell = row.createCell(0);
                cell.setCellValue(value);
            }

            if(rowIndex > 0) {
                Row row = sheet.createRow(rowIndex++);
                Cell cell = row.createCell(0);
                cell.setCellValue("");
            }

            if (!report.getMainGrid().getRows().isEmpty()) {
                int cellIndex = 0;
                Row row = sheet.createRow(rowIndex++);
                for (String value : report.getMainGrid().getHeaders()) {
                    Cell cell = row.createCell(cellIndex++);
                    cell.setCellValue(value);
                }
                populateExcel(sheet, report.getMainGrid().getRows(), rowIndex);
            }
            workbook.write(byteArrayOutputStream);

            return new InputResource(
                    new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())),
                    byteArrayOutputStream.size(),
                    report.getReportFileName() + ".xlsx"
            );
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        throw new ExportException("Could not create report as excel file. Report name '" + report.getReportName() + "'");
    }

    protected void populateExcel(Sheet sheet, List<List<ReportCell>> cells, int rowIndex) {
        for (List<ReportCell> columns : cells) {
            Row row = sheet.createRow(rowIndex);
            int cellIndex = 0;
            for (ReportCell column : columns) {
                Cell cell = row.createCell(cellIndex);
                cell.setCellValue(column.getValue());
                cellIndex++;
            }
            rowIndex++;
        }
    }

    protected void iterateOrdersAndSumCompleted(Collection<OrderDto> collection,
                                                Map<Long, List<ItemQuantityDto>> itemByIdMap,
                                                IntegerHolder completedHolder) {
        for (OrderDto orderDto : collection) {
            for (ItemQuantityDto itemQuantityDto : orderDto.getOrderItems()) {
                addToItemByIdMap(itemQuantityDto, itemByIdMap);
            }
            if (orderDto.getEndDate() != null) {
                completedHolder.value++;
            }
        }
    }

    protected void addToItemByIdMap(ItemQuantityDto itemQuantityDto, Map<Long, List<ItemQuantityDto>> itemByIdMap) {
        itemByIdMap.putIfAbsent(itemQuantityDto.getItem().getId(), new LinkedList<>());
        itemByIdMap.get(itemQuantityDto.getItem().getId()).add(itemQuantityDto);
    }

    protected void populateReportWithMostPopularItem(Report report, Map<Long, List<ItemQuantityDto>> itemsByIdMap) {
        long maxQuantity = 0;
        String mostPopularItemName = "";
        for (Map.Entry<Long, List<ItemQuantityDto>> entry : itemsByIdMap.entrySet()) {
            long localQuantity = 0;
            String localItemName = "";
            for (ItemQuantityDto itemQuantityDto : entry.getValue()) {
                if (localItemName.isEmpty()) {
                    localItemName = itemQuantityDto.getItem().getName();
                }
                localQuantity += itemQuantityDto.getQuantity();
            }
            if (localQuantity > maxQuantity) {
                maxQuantity = localQuantity;
                mostPopularItemName = localItemName;
            }
        }
        if (!mostPopularItemName.isEmpty()) {
            report.getBeforeGrid().add("Most popular item is '" + mostPopularItemName + "', ordered " + maxQuantity + " items.");
        }
    }

    protected Map<Long, Map<String, List<ItemQuantityDto>>> getItemByIdGroupedByPriceMap(Map<Long, List<ItemQuantityDto>> itemByIdMap) {
        Map<Long, Map<String, List<ItemQuantityDto>>> itemByIdGroupedByPriceMap = new HashMap<>();
        for (Map.Entry<Long, List<ItemQuantityDto>> entry : itemByIdMap.entrySet()) {
            for (ItemQuantityDto itemQuantityDto : entry.getValue()) {
                String priceFormatted = itemQuantityDto.getItem().getPrice().getPriceFormatted();
                itemByIdGroupedByPriceMap.compute(itemQuantityDto.getItem().getId(), (key, oldVal) -> {
                    if (oldVal == null) {
                        oldVal = new HashMap<>();
                    }
                    oldVal.compute(priceFormatted, (innerKey, innerOldVal) -> {
                        if (innerOldVal == null) {
                            innerOldVal = new LinkedList<>();
                        }
                        innerOldVal.add(itemQuantityDto);
                        return innerOldVal;
                    });
                    return oldVal;
                });
            }
        }
        return itemByIdGroupedByPriceMap;
    }

    static class IntegerHolder {
        public int value;

        public IntegerHolder(int value) {
            this.value = value;
        }
    }

}
