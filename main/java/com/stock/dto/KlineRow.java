package com.stock.dto;

/** 单条日 K 记录（东方财富原始字段） */
public record KlineRow(
        String tradeDate,
        Double open,
        Double close,
        Double high,
        Double low,
        Double volume,
        Double amount,
        Double amplitude,
        Double pctChg,
        Double change,
        Double turnoverRate) {
}
