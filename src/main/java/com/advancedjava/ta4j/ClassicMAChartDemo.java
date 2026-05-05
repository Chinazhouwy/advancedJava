//package com.advancedjava.ta4j;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneOffset;
//import java.util.List;
//import javax.swing.JFrame;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.time.Day;
//import org.jfree.data.time.TimeSeries;
//import org.jfree.data.time.TimeSeriesCollection;
//import org.ta4j.core.BarSeries;
//import org.ta4j.core.BaseBar;
//import org.ta4j.core.BaseBarSeriesBuilder;
//import org.ta4j.core.indicators.averages.EMAIndicator;
//import org.ta4j.core.indicators.averages.SMAIndicator;
//import org.ta4j.core.indicators.averages.WMAIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.num.DecimalNumFactory;
//import org.ta4j.core.num.Num;
//
///**
// * ta4j 经典移动平均线绘图示例。
// *
// * <p>本类演示如何：
// * 1. 从 CSV 读取日线行情。
// * 2. 构建 ta4j 的 {@code BarSeries}。
// * 3. 计算 SMA、EMA、WMA 三类均线。
// * 4. 使用 JFreeChart 将价格与指标曲线画出来。
// *
// * <p>当前实现已经按项目里使用的 ta4j 0.22.6 版本调整过 API 调用方式，
// * 因此既能保留教学可读性，也能通过编译。
// */
//public class ClassicMAChartDemo {
//
//    /**
//     * 示例入口：读取 CSV、计算均线并弹出图表窗口。
//     */
//    public static void main(String[] args) {
//        BarSeries series = null;
//        try {
//            series = loadFromCsvResource("AAPL-2024.csv");
//        } catch (Exception e) {
//            System.err.println("Error loading CSV data: " + e.getMessage());
//            e.printStackTrace();
//            return;
//        }
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        int period = 20;
//
//        SMAIndicator sma = new SMAIndicator(closePrice, period);
//        EMAIndicator ema = new EMAIndicator(closePrice, period);
//        WMAIndicator wma = new WMAIndicator(closePrice, period);
//
//        TimeSeriesCollection dataset = new TimeSeriesCollection();
//        dataset.addSeries(createTimeSeries(series, closePrice, "Close Price"));
//        dataset.addSeries(createTimeSeries(series, sma, "SMA (" + period + ")"));
//        dataset.addSeries(createTimeSeries(series, ema, "EMA (" + period + ")"));
//        dataset.addSeries(createTimeSeries(series, wma, "WMA (" + period + ")"));
//
//        JFreeChart chart = ChartFactory.createTimeSeriesChart(
//                "Apple Stock - Moving Averages Comparison (Period 20)",
//                "Date", "Price (USD)", dataset, true, true, false);
//
//        ChartPanel chartPanel = new ChartPanel(chart);
//        JFrame frame = new JFrame("Ta4j Classic MA Chart - Apple Inc.");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add(chartPanel);
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    /**
//     * 把 ta4j 指标转换成 JFreeChart 的时间序列。
//     */
//    private static TimeSeries createTimeSeries(
//            BarSeries series, org.ta4j.core.Indicator<Num> indicator, String name) {
//        TimeSeries timeSeries = new TimeSeries(name);
//
//        for (int i = 0; i < series.getBarCount(); i++) {
//            LocalDate date = series.getBar(i).getEndTime().atZone(ZoneOffset.UTC).toLocalDate();
//            double value = indicator.getValue(i).doubleValue();
//            if (!Double.isNaN(value)) {
//                Day day = new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
//                timeSeries.addOrUpdate(day, value);
//            }
//        }
//        return timeSeries;
//    }
//
//    /**
//     * 从 CSV 资源文件加载 BarSeries。
//     *
//     * <p>CSV 列格式：date,open,high,low,close,volume
//     */
//    private static BarSeries loadFromCsvResource(String csvFileName) throws Exception {
//        List<String> lines = Files.readAllLines(Path.of("src/main/resources", csvFileName));
//
//        boolean skipHeader = true;
//        BarSeries series =
//                new BaseBarSeriesBuilder()
//                        .withName("AAPL")
//                        .withNumFactory(DecimalNumFactory.getInstance())
//                        .build();
//        var numFactory = series.numFactory();
//
//        for (String line : lines) {
//            if (skipHeader) {
//                skipHeader = false;
//                continue;
//            }
//
//            String[] values = line.split(",");
//            if (values.length >= 6) {
//                String dateStr = values[0];
//                double open = Double.parseDouble(values[1]);
//                double high = Double.parseDouble(values[2]);
//                double low = Double.parseDouble(values[3]);
//                double close = Double.parseDouble(values[4]);
//                long volume = Long.parseLong(values[5]);
//                Instant endTime = LocalDate.parse(dateStr).atStartOfDay(ZoneOffset.UTC).toInstant();
//
//                BaseBar bar =
//                        new BaseBar(
//                                Duration.ofDays(1),
//                                endTime.minus(Duration.ofDays(1)),
//                                endTime,
//                                numFactory.numOf(open),
//                                numFactory.numOf(high),
//                                numFactory.numOf(low),
//                                numFactory.numOf(close),
//                                numFactory.numOf(volume),
//                                numFactory.zero(),
//                                0L);
//                series.addBar(bar, true);
//            }
//        }
//
//        return series;
//    }
//}
