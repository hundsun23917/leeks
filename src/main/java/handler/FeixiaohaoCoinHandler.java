package handler;

import bean.*;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import net.sf.cglib.core.Local;
import utils.HttpClientPool;
import utils.LogUtil;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FeixiaohaoCoinHandler extends CoinRefreshHandler {
    private static final String URL = "https://dncapi.fxhapp.com/api/coin/web-coinrank";
    private final JLabel refreshTimeLabel;

    private static ScheduledExecutorService mSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();

    private static Gson gson = new Gson();

    public FeixiaohaoCoinHandler(JTable table, JLabel label) {
        super(table);
        this.refreshTimeLabel = label;
    }

    @Override
    public void handle(List<String> code) {
        if (code.isEmpty()) {
            return;
        }

        useScheduleThreadExecutor(code);
    }

    public void useScheduleThreadExecutor(List<String> code) {
        if (mSchedulerExecutor.isShutdown()){
            mSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        mSchedulerExecutor.scheduleAtFixedRate(getWork(code), 0, threadSleepTime, TimeUnit.SECONDS);
    }

    private Runnable getWork(List<String> code) {
        return () -> {
            pollStock(code);
        };
    }

    private void pollStock(List<String> code) {
        if (code.isEmpty()){
            return;
        }
        try {
            HttpClientPool httpClient = HttpClientPool.getHttpClient();
            String res = httpClient.get(URL);
//            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
//            System.out.printf("%s,%s%n", time, res);
            handleResponse(res,code);
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response ,List<String> code) {
        List<String> refreshTimeList = new ArrayList<>();
        try{
            FxhResult fxhResult = gson.fromJson(response, FxhResult.class);
            List<CoinBean> result = fxhResult.getData().stream().filter(datum -> {
                return code.stream().anyMatch(subscribeCode -> {
                    return subscribeCode.toUpperCase().equals(datum.getName().toUpperCase());
                });
            }).map(datum -> {
                CoinBean coinBean = new CoinBean(datum.getName());
                BigDecimal currentPriceUsd = BigDecimal.valueOf(datum.getCurrentPriceUsd());
                BigDecimal changerateUtc8 = BigDecimal.valueOf(datum.getChangerateUtc8());
                BigDecimal changePrice =currentPriceUsd.subtract(
                        currentPriceUsd.divide(
                                changerateUtc8.divide(new BigDecimal(100)).add(BigDecimal.ONE),4));
                coinBean.setRegularMarketChange(changePrice.doubleValue());
                coinBean.setRegularMarketChangePercent(datum.getChangerateUtc8());
                String klineData = datum.getKlineData();
                coinBean.setRegularMarketDayLow(Arrays.asList(klineData.split(",")).stream().map(element->{
                    Double price = Double.parseDouble(element);
                    return price;
                }).mapToDouble(e->e).min().getAsDouble());
                coinBean.setRegularMarketDayHigh(Arrays.asList(klineData.split(",")).stream().map(element->{
                    Double price = Double.parseDouble(element);
                    return price;
                }).mapToDouble(e->e).max().getAsDouble());
                coinBean.setRegularMarketPrice(datum.getCurrentPriceUsd());
                coinBean.setTimeStamp(new Date().getTime());
                return coinBean;
            }).collect(Collectors.toList());
            for (CoinBean coinBean : result) {
                updateData(coinBean);
                refreshTimeList.add(coinBean.getValueByColumn("更新时间",false));
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }

        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }

    @Override
    public void stopHandle() {
        mSchedulerExecutor.shutdown();
        LogUtil.info("leeks stock 自动刷新关闭!");
    }


}