package com.stock.dto;

import java.util.List;

/** 日 K 抓取结果 */
public record KlineResult(String name, String code, List<KlineRow> rows) {
}
