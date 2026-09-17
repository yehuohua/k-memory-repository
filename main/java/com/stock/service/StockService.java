package com.stock.service;

import com.stock.client.TushareClient;
import com.stock.dto.CollectRequest;
import com.stock.dto.CollectSummary;
import com.stock.dto.KlineResult;
import com.stock.dto.KlineRow;
import com.stock.dto.SearchResult;
import com.stock.entity.Stock;
import com.stock.entity.StockDaily;
import com.stock.repository.StockDailyRepository;
import com.stock.repository.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final TushareClient client;
    private final StockRepository stockRepo;
    private final StockDailyRepository dailyRepo;

    public StockService(TushareClient client, StockRepository stockRepo, StockDailyRepository dailyRepo) {
        this.client = client;
        this.stockRepo = stockRepo;
        this.dailyRepo = dailyRepo;
    }

    public List<SearchResult> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入搜索关键词");
        }
        return client.search(keyword.trim());
    }

    @Transactional
    public CollectSummary collect(CollectRequest req) {
        String code = req.code() == null ? "" : req.code().trim();
        if (!code.matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "股票代码必须是 6 位数字");
        }
        int days = req.days() == null ? 250 : req.days();
        days = Math.max(1, Math.min(days, 2000));

        KlineResult result = client.fetchDaily(code, days);
        if (result.rows().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未获取到该股票数据，请检查代码");
        }
        String name = result.name() != null ? result.name() : code;

        // 保存股票基本信息
        Stock stock = stockRepo.findById(code).orElseGet(Stock::new);
        if (stock.getCode() == null) {
            stock.setCode(code);
            stock.setCreatedAt(LocalDateTime.now());
        }
        stock.setName(name);
        stock.setMarket(client.marketOf(code));
        stock.setTsCode(client.tsCode(code));
        stockRepo.save(stock);

        // 增量保存日 K
        Map<LocalDate, StockDaily> existing = dailyRepo.findByCodeOrderByTradeDateAsc(code)
                .stream().collect(Collectors.toMap(StockDaily::getTradeDate, Function.identity()));

        long inserted = 0, updated = 0;
        for (KlineRow row : result.rows()) {
            LocalDate date = LocalDate.parse(row.tradeDate());
            StockDaily daily = existing.get(date);
            if (daily == null) {
                daily = new StockDaily();
                daily.setCode(code);
                daily.setTradeDate(date);
                inserted++;
            } else {
                updated++;
            }
            daily.setOpen(row.open());
            daily.setHigh(row.high());
            daily.setLow(row.low());
            daily.setClose(row.close());
            daily.setVolume(row.volume());
            daily.setAmount(row.amount());
            daily.setAmplitude(row.amplitude());
            daily.setPctChg(row.pctChg());
            daily.setChangeAmt(row.change());
            daily.setTurnoverRate(row.turnoverRate());
            dailyRepo.save(daily);
        }

        return new CollectSummary(code, name, result.rows().size(), inserted, updated);
    }

    public List<Stock> listStocks() {
        return stockRepo.findAll();
    }

    public Stock getStock(String code) {
        return stockRepo.findById(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该股票，请先采集"));
    }

    public StockDaily latest(String code) {
        return dailyRepo.findFirstByCodeOrderByTradeDateDesc(code).orElse(null);
    }

    public long countDaily(String code) {
        return dailyRepo.countByCode(code);
    }

    public List<StockDaily> getDaily(String code, Integer limit) {
        if (!stockRepo.existsById(code)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该股票，请先采集");
        }
        List<StockDaily> list = dailyRepo.findByCodeOrderByTradeDateAsc(code);
        if (limit != null && limit > 0 && list.size() > limit) {
            list = list.subList(list.size() - limit, list.size());
        }
        return list;
    }

    @Transactional
    public void remove(String code) {
        if (!stockRepo.existsById(code)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该股票");
        }
        dailyRepo.deleteByCode(code);
        stockRepo.deleteById(code);
    }
}
