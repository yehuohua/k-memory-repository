package com.stock.controller;

import com.stock.dto.CollectRequest;
import com.stock.dto.CollectSummary;
import com.stock.dto.SearchResult;
import com.stock.entity.Stock;
import com.stock.entity.StockDaily;
import com.stock.service.StockService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    /** 已记录股票列表 */
    @GetMapping
    public List<Map<String, Object>> list() {
        return service.listStocks().stream().map(this::toSummary).toList();
    }

    /** 搜索股票 */
    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String keyword) {
        return service.search(keyword);
    }

    /** 采集并落库 */
    @PostMapping("/collect")
    public CollectSummary collect(@RequestBody CollectRequest req) {
        return service.collect(req);
    }

    /** 股票详情（含最新行情） */
    @GetMapping("/{code}")
    public Map<String, Object> detail(@PathVariable String code) {
        return toSummary(service.getStock(code));
    }

    /** 日 K 数据 */
    @GetMapping("/{code}/daily")
    public List<StockDaily> daily(@PathVariable String code,
                                  @RequestParam(required = false) Integer limit) {
        return service.getDaily(code, limit);
    }

    /** 删除股票及其数据 */
    @DeleteMapping("/{code}")
    public Map<String, Object> remove(@PathVariable String code) {
        service.remove(code);
        Map<String, Object> m = new HashMap<>();
        m.put("deleted", code);
        return m;
    }

    private Map<String, Object> toSummary(Stock stock) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", stock.getCode());
        m.put("name", stock.getName());
        m.put("market", stock.getMarket());
        m.put("rowCount", service.countDaily(stock.getCode()));
        StockDaily latest = service.latest(stock.getCode());
        if (latest != null) {
            m.put("latestDate", latest.getTradeDate().toString());
            m.put("latestClose", latest.getClose());
            m.put("latestPctChg", latest.getPctChg());
        }
        return m;
    }
}
