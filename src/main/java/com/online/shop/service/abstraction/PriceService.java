package com.online.shop.service.abstraction;

import com.online.shop.controller.admin.forms.PriceUpdateForm;
import com.online.shop.dto.PriceDto;
import com.online.shop.entity.Price;
import com.online.shop.service.util.Pagination;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;

public interface PriceService {

    static String formatDecimal(BigDecimal decimal) {
        //TODO DecimalFormat is not thread safe,
        // but need to refactor and avoid creating new instance of DecimalFormat
        return new DecimalFormat(
                "###,###.00",
                DecimalFormatSymbols.getInstance(Locale.UK)
        ).format(decimal);
    }

    Pagination<PriceDto> findPaginated(int pageSize, int currentPage);

    Collection<PriceDto> getAllPrices();

    PriceDto getById(Long id);

    PriceDto createPrice(Price price);

    PriceDto updatePrice(Price price);

    Price updatePriceFieldsByForm(Price entity, PriceUpdateForm form);

    void deletePriceById(Long id);

    PriceDto detachItemFromPrice(Long priceId);

    int getPageSizeProp();

    boolean existsById(Long id);
}
