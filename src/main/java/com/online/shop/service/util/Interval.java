package com.online.shop.service.util;

import java.util.Date;

public class Interval {

    private Date startDate;
    private Date endDate;

    public Interval(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Interval setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Interval setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public boolean intersects(Interval other) {
        return startDate.after(other.startDate) && startDate.before(other.endDate) ||
                endDate.before(other.endDate) && endDate.after(other.endDate);
    }

    public boolean isValid() {
        return startDate != null && endDate != null && startDate.before(endDate);
    }

}
