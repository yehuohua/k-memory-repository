package com.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 股票基本信息表
 */
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @Column(length = 6)
    private String code;

    @Column(nullable = false)
    private String name;

    /** 市场：SH / SZ / BJ */
    @Column(nullable = false, length = 2)
    private String market;

    /** 交易所带后缀代码（ts_code），如 002241.SZ，沿用旧列名 secid */
    @Column(name = "secid", nullable = false, length = 16)
    private String tsCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getTsCode() {
        return tsCode;
    }

    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
