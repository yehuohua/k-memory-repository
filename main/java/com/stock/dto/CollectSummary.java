package com.stock.dto;

/** 采集结果汇总 */
public record CollectSummary(String code, String name, long total, long inserted, long updated) {
}
