package com.stock.dto;

/** 采集请求体 */
public record CollectRequest(String code, Integer days) {
}
