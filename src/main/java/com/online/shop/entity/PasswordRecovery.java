package com.online.shop.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "password_recovery")
public class PasswordRecovery {

    @Id
    @Column(name = "recovery_id", nullable = false, unique = true)
    private String id;

    @Column
    @NotNull
    private Boolean used;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User holder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public User getHolder() {
        return holder;
    }

    public void setHolder(User holder) {
        this.holder = holder;
    }
}
