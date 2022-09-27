package com.online.shop.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @Column(name = "verification_id", nullable = false, unique = true)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User holder;

    // represents a status (false - not verified using this verification, true - verified)
    @Column
    @NotNull
    private Boolean status;

    @Column
    @NotNull
    private Boolean sent;

    @Column(name = "tried_to_send")
    @NotNull
    private Boolean triedToSend;

    @Column(name = "expiration_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date expirationDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getHolder() {
        return holder;
    }

    public void setHolder(User holder) {
        this.holder = holder;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getTriedToSend() {
        return triedToSend;
    }

    public void setTriedToSend(Boolean triedToSend) {
        this.triedToSend = triedToSend;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
