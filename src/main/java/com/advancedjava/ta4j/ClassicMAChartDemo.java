package com.advancedjava.ta4j;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.averages.WMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;

import javax.swing.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 添加缺失的导入
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


public class ClassicMAChartDemo {

    public static void main(String[] args) {
        // 加载真实数据
        // 使用ta4j内置的CsvBarsLoader加载数据
        BarSeries series = null;
        try {
            // 尝试从资源文件夹加载CSV数据
            series = loadFromCsvResource("AAPL-2024.csv");
        } catch (Exception e) {
            System.err.println("Error loading CSV data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

//        BarSeries series = CsvBarsLoader.loadCsvSeries("AAPL-2024.csv");
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        int period = 20;

        // 创建三个经典移动平均线指标
        SMAIndicator sma = new SMAIndicator(closePrice, period);
        EMAIndicator ema = new EMAIndicator(closePrice, period);
        WMAIndicator wma = new WMAIndicator(closePrice, period);

        // 创建数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(createTimeSeries(series, closePrice, "Close Price"));
        dataset.addSeries(createTimeSeries(series, sma, "SMA (" + period + ")"));
        dataset.addSeries(createTimeSeries(series, ema, "EMA (" + period + ")"));
        dataset.addSeries(createTimeSeries(series, wma, "WMA (" + period + ")"));

        // 创建并显示图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Apple Stock - Moving Averages Comparison (Period 20)",
                "Date", "Price (USD)", dataset, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Ta4j Classic MA Chart - Apple Inc.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static TimeSeries createTimeSeries(BarSeries series,
                                               org.ta4j.core.Indicator<Num> indicator,
                                               String name) {
        TimeSeries timeSeries = new TimeSeries(name);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault());

        for (int i = 0; i < series.getBarCount(); i++) {
            String dateStr = series.getBar(i).getEndTime().toString().substring(0, 10);
            double value = indicator.getValue(i).doubleValue();
            if (!Double.isNaN(value)) {
                Day day = new Day(
                        Integer.parseInt(dateStr.substring(8, 10)),
                        Integer.parseInt(dateStr.substring(5, 7)),
                        Integer.parseInt(dateStr.substring(0, 4))
                );
                timeSeries.addOrUpdate(day, value);
            }
        }
        return timeSeries;
    }

    /**
     * 从CSV资源文件加载BarSeries
     */
    private static BarSeries loadFromCsvResource(String csvFileName) throws Exception {
        List<String> lines = java.nio.file.Files.readAllLines(
                java.nio.file.Paths.get("src/main/resources/" + csvFileName));

        // 跳过标题行
        boolean skipHeader = true;
        BaseBarSeries series = new BaseBarSeries("AAPL");

        for (String line : lines) {
            if (skipHeader) {
                skipHeader = false;
                continue;
            }

            String[] values = line.split(",");
            if (values.length >= 6) {
                String dateStr = values[0];
                double open = Double.parseDouble(values[1]);
                double high = Double.parseDouble(values[2]);
                double low = Double.parseDouble(values[3]);
                double close = Double.parseDouble(values[4]);
                long volume = Long.parseLong(values[5]);

                java.time.ZonedDateTime dateTime = java.time.ZonedDateTime.parse(dateStr + "T00:00:00Z");

                // Create Num instances from double values
                BaseBar bar = BaseBar.builder(Duration.ofDays(1), dateTime)
                    .openPrice(series.numOf(open))
                    .highPrice(series.numOf(high))
                    .lowPrice(series.numOf(low))
                    .closePrice(series.numOf(close))
                    .volume(volume)
                    .build();
                series.addBar(bar);
            }
        }

        return seriesBuilder.build();
    }
}
