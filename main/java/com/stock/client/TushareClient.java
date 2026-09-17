package com.stock.client;

import com.stock.dto.KlineResult;
import com.stock.dto.KlineRow;
import com.stock.dto.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直连 Tushare Pro 行情接口（需 token）。
 * <p>
 * 股票列表（stock_basic）在免费账号下限频 1 次/小时，因此落地成磁盘缓存：
 * 首次成功后缓存 24 小时，期间搜索/取名都走缓存，限频时退回旧缓存。
 */
@Component
public class TushareClient {

    private static final String API_URL = "http://api.tushare.pro";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final long CACHE_TTL = 24 * 60 * 60 * 1000L; // 列表缓存 24 小时

    private final RestClient restClient = RestClient.create();
    private final String token;
    private final File cacheFile = new File(System.getProperty("user.dir"), "stock_list_cache.tsv");

    private volatile List<SearchResult> stockCache;
    private volatile long stockCacheTime = 0;

    public TushareClient(@Value("${tushare.token}") String token) {
        this.token = token;
    }

    /** 6 位代码 -> 市场（SH/SZ/BJ） */
    public String marketOf(String code) {
        if (code.startsWith("6")) return "SH";
        if (code.startsWith("0") || code.startsWith("3")) return "SZ";
        return "BJ";
    }

    /** 6 位代码 -> tushare ts_code，如 600519.SH */
    public String tsCode(String code) {
        return code + "." + marketOf(code);
    }

    /** 搜索股票（数字按代码前缀，其余按中文名包含） */
    public List<SearchResult> search(String keyword) {
        String kw = keyword.trim();
        List<SearchResult> all = allStocks();
        if (kw.matches("\\d+")) {
            return all.stream().filter(s -> s.code().startsWith(kw)).limit(10).toList();
        }
        return all.stream().filter(s -> s.name().contains(kw)).limit(20).toList();
    }

    /** 拉取日 K（最近 days 个交易日） */
    public KlineResult fetchDaily(String code, int days) {
        String start = LocalDate.now().minusDays(days * 2L).format(DateTimeFormatter.BASIC_ISO_DATE);
        String end = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, Object> resp = call("daily",
                Map.of("ts_code", tsCode(code), "start_date", start, "end_date", end),
                "trade_date,open,high,low,close,pre_close,change,pct_chg,vol,amount");

        List<KlineRow> rows = new ArrayList<>();
        for (Map<String, Object> row : rowsOf(resp)) {
            String date = formatDate(str(row.get("trade_date")));
            if (date == null) continue;
            Double open = dbl(row.get("open"));
            Double close = dbl(row.get("close"));
            Double high = dbl(row.get("high"));
            Double low = dbl(row.get("low"));
            Double preClose = dbl(row.get("pre_close"));
            Double volume = dbl(row.get("vol"));
            Double amount = dbl(row.get("amount"));
            Double change = dbl(row.get("change"));
            Double pctChg = dbl(row.get("pct_chg"));
            Double amplitude = (high != null && low != null && preClose != null && preClose != 0)
                    ? Math.round((high - low) / preClose * 10000) / 100.0 : null;
            rows.add(new KlineRow(date, open, close, high, low, volume, amount, amplitude, pctChg, change, null));
        }
        // Tushare daily 返回按日期倒序（新→旧），反转为升序后再取最近 days 条
        Collections.reverse(rows);
        if (rows.size() > days) {
            rows = new ArrayList<>(rows.subList(rows.size() - days, rows.size()));
        }

        String name = allStocks().stream()
                .filter(s -> s.code().equals(code))
                .map(SearchResult::name)
                .findFirst().orElse(code);
        return new KlineResult(name, code, rows);
    }

    private List<SearchResult> allStocks() {
        long now = System.currentTimeMillis();
        if (stockCache == null || now - stockCacheTime > CACHE_TTL) {
            synchronized (this) {
                if (stockCache == null || now - stockCacheTime > CACHE_TTL) {
                    stockCache = loadOrFetch();
                    stockCacheTime = System.currentTimeMillis();
                }
            }
        }
        return stockCache;
    }

    /** 优先磁盘缓存，过期再拉 Tushare，限频/失败时退回旧缓存 */
    private List<SearchResult> loadOrFetch() {
        List<SearchResult> disk = readCacheFile();
        if (disk != null && System.currentTimeMillis() - cacheFile.lastModified() < CACHE_TTL) {
            return disk;
        }
        try {
            List<SearchResult> fresh = fetchAllStocks();
            if (!fresh.isEmpty()) {
                writeCacheFile(fresh);
                return fresh;
            }
        } catch (Exception ignored) {
            // 网络/限频失败，退回磁盘缓存
        }
        return disk != null ? disk : List.of();
    }

    private List<SearchResult> fetchAllStocks() {
        Map<String, Object> resp = call("stock_basic", Map.of("list_status", "L"), "ts_code,symbol,name");
        List<SearchResult> out = new ArrayList<>();
        for (Map<String, Object> row : rowsOf(resp)) {
            String code = str(row.get("symbol"));
            String name = str(row.get("name"));
            if (code == null || name == null || !code.matches("\\d{6}")) continue;
            out.add(new SearchResult(code, name, marketOf(code)));
        }
        return out;
    }

    /** 读磁盘缓存（code\tname 每行一条），失败返回 null */
    private List<SearchResult> readCacheFile() {
        if (!cacheFile.exists()) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile), StandardCharsets.UTF_8))) {
            List<SearchResult> out = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                int i = line.indexOf('\t');
                if (i <= 0) continue;
                String code = line.substring(0, i).trim();
                String name = line.substring(i + 1).trim();
                if (!code.matches("\\d{6}") || name.isEmpty()) continue;
                out.add(new SearchResult(code, name, marketOf(code)));
            }
            return out.isEmpty() ? null : out;
        } catch (IOException e) {
            return null;
        }
    }

    /** 写磁盘缓存，失败不影响主流程 */
    private void writeCacheFile(List<SearchResult> list) {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), StandardCharsets.UTF_8))) {
            for (SearchResult s : list) {
                w.write(s.code());
                w.write('\t');
                w.write(s.name());
                w.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    /** 调用 Tushare 接口 */
    private Map<String, Object> call(String apiName, Map<String, Object> params, String fields) {
        Map<String, Object> body = new HashMap<>();
        body.put("api_name", apiName);
        body.put("token", token);
        body.put("params", params);
        body.put("fields", fields);
        return restClient.post()
                .uri(URI.create(API_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(MAP_TYPE);
    }

    /** 把 Tushare 的 data.fields + data.items 转成一行一个 Map（限频/无数据时返回空） */
    private List<Map<String, Object>> rowsOf(Map<String, Object> resp) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (resp == null || !(resp.get("data") instanceof Map<?, ?> d)) return out;
        if (!(d.get("fields") instanceof List<?> fields) || !(d.get("items") instanceof List<?> items)) return out;
        for (Object item : items) {
            if (!(item instanceof List<?> row)) continue;
            Map<String, Object> m = new HashMap<>();
            for (int i = 0; i < fields.size() && i < row.size(); i++) {
                m.put(str(fields.get(i)), row.get(i));
            }
            out.add(m);
        }
        return out;
    }

    /** 20260827 -> 2026-08-27（Tushare 日期为 yyyyMMdd） */
    private String formatDate(String basic) {
        if (basic == null || basic.length() != 8) return basic;
        return basic.substring(0, 4) + "-" + basic.substring(4, 6) + "-" + basic.substring(6, 8);
    }

    private Double dbl(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }
}
