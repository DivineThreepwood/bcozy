package org.openbase.bco.bcozy.controller.powerterminal;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.tools.FlowGridPane;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.stage.Screen;
import org.influxdata.query.FluxRecord;
import org.influxdata.query.FluxTable;
import org.openbase.bco.bcozy.controller.powerterminal.chartattributes.DateRange;
import org.openbase.bco.bcozy.controller.powerterminal.chartattributes.VisualizationType;
import org.openbase.bco.bcozy.model.InfluxDBHandler;
import org.openbase.bco.bcozy.model.LanguageSelection;
import org.openbase.bco.bcozy.model.powerterminal.ChartStateModel;
import org.openbase.bco.bcozy.view.BackgroundPane;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.visual.javafx.control.AbstractFXController;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class PowerChartVisualizationController extends AbstractFXController {

    public static final VisualizationType DEFAULT_VISUALISATION_TYPE = VisualizationType.BARCHART;
    public static final String CHART_HEADER_IDENTIFIER = "powerterminal.chartHeader";
    public static final int AMPLITUDE_DIVIDER = 10;

    @FXML
    FlowGridPane pane;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PowerChartVisualizationController.class);

    public static final String WEBENGINE_ALERT_MESSAGE = "Webengine alert detected!";
    public static final String WEBENGINE_ERROR_MESSAGE = "Webengine error detected!";
    //todo: make configurable via bcozy settings, see #89
    public static String CHRONOGRAPH_URL = "http://192.168.75.100:9999/orgs/03e2c6b79272c000/dashboards/03e529b61ff2c000?lower=now%28%29%20-%2024h";
//    public static String CHRONOGRAPH_URL = "http://localhost:9999";
    public static final int TILE_WIDTH = (int) Screen.getPrimary().getVisualBounds().getWidth();
    public static final int TILE_HEIGHT = (int) Screen.getPrimary().getVisualBounds().getHeight();
    private static final long REFRESH_INTERVAL_MILLISECONDS = 30000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PowerChartVisualizationController.class);

    private WebEngine webEngine;

    private ChartStateModel chartStateModel;
    private BackgroundPane backgroundPane;

    @Override
    public void updateDynamicContent() throws CouldNotPerformException {

    }

    @Override
    public void initContent() throws InitializationException {
        pane.setMinSize(Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight() - 600);
        //setChartType(DEFAULT_VISUALISATION_TYPE, true);
        enableDataRefresh(REFRESH_INTERVAL_MILLISECONDS);
    }


    /**
     * Depending on the skinType the correct DataType is added to the Chart
     */
    private void addCorrectDataType(Tile.SkinType skinType, Tile chart, List<ChartData> data) throws NotSupportedException {
        XYChart.Series<String, Number> series = new XYChart.Series();

        switch (skinType) {
            case MATRIX:
                chart.setAnimated(true);
                chart.setChartData(data);
                //The Matrix skinType does not show any data if they are not updated (Bug in tilesfx)
                if (skinType.equals(Tile.SkinType.MATRIX)) {
                    GlobalScheduledExecutorService.execute(() -> {
                        for (ChartData datum : data) {
                            datum.setValue(datum.getValue());
                        }
                    });
                }
                break;
            case SMOOTHED_CHART:
                for (ChartData datum : data) {
                    series.getData().add(new XYChart.Data(datum.getName(), datum.getValue()));
                }
                chart.addSeries(series);
                break;
            default:
                throw new NotSupportedException(skinType, this.getClass(), "Unsupported chart skintype!");
        }
        chart.setSkinType(skinType);
    }


    /**
     * Schedules repeating data refreshment
     *
     * @param refreshInterval interval to refresh the data
     */
    private void enableDataRefresh(long refreshInterval) {
        try {
            GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
                Platform.runLater(() -> {
                    setChartType(this.chartStateModel.getVisualizationType(), false);
                });
            }, refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory("Could not refresh power chart data", ex, LOGGER);
        }
    }


    /**
     * Initializes the previous energy consumption
     *
     * @return List of ChartData with previous Energy Consumption
     */
    private List<ChartData> loadChartData(String interval, long startTime, long endTime) {
        List<ChartData> data = new ArrayList<ChartData>();
        int change = 0;
        try {
            List<FluxTable> energy = InfluxDBHandler.getAveragePowerConsumptionTables(
                    interval, TimeUnit.MILLISECONDS.toSeconds(startTime), TimeUnit.MILLISECONDS.toSeconds(endTime), "consumption");
            for (FluxTable fluxTable : energy) {
                List<FluxRecord> records = fluxTable.getRecords();
                for (FluxRecord fluxRecord : records) {
                    if (fluxRecord.getValueByKey("_value") == null) {
                        ChartData temp = new ChartData(String.valueOf(change), 0, Tile.ORANGE);
                        temp.setName(String.valueOf(change));
                        data.add(temp);
                    } else {
                        ChartData temp = new ChartData(String.valueOf(change), (double) fluxRecord.getValueByKey("_value") / AMPLITUDE_DIVIDER, Tile.ORANGE);
                        temp.setName(String.valueOf(change));
                        data.add(temp);
                    }

                }
                change++;
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Could not load chart data!", e, LOGGER);
        }
        return data;
    }

    /**
     * Connects the given chart attribute properties to the chart by creating listeners incorporating the changes
     * into the chart
     *
     * @param chartStateModel StateModel that describes the state of the chart as configured by other panes
     */
    public void initChartState(ChartStateModel chartStateModel) {

        this.chartStateModel = chartStateModel;

        chartStateModel.visualizationTypeProperty().addListener(
                (ChangeListener<? super VisualizationType>) (observableValue, oldVisualizationType, newVisualizationType) -> {
                    setChartType(newVisualizationType, false);
                });

        chartStateModel.dateRangeProperty().addListener((ChangeListener<? super DateRange>) (observableValue, oldDateRange, newDateRange) -> {
            setChartType(this.chartStateModel.getVisualizationType(), false);
        });
    }


    /**
     * Gets the BackgroundPane
     *
     * @param backgroundPane the BackgroundPane
     */
    public void initBackgroundPane(BackgroundPane backgroundPane) {
        this.backgroundPane = backgroundPane;
    }

    private void setChartType(VisualizationType newVisualizationType, boolean firstRun) {
        if (newVisualizationType==null)
            newVisualizationType = DEFAULT_VISUALISATION_TYPE;

        Node node;
        switch (newVisualizationType) {
            case HEATMAP:
                pane.getChildren().clear();
                this.backgroundPane.activateHeatmap();
                break;
            default:
                if (chartStateModel == null) {
                    node = generateTilesFxChart(newVisualizationType);
                } else {
                    node = generateTilesFxChart(newVisualizationType, chartStateModel.getDateRange());
                }
                if (!firstRun)
                    this.backgroundPane.deactivateHeatmap();
                pane.getChildren().clear();
                pane.getChildren().add(node);
                break;
        }
    }

    private Tile generateTilesFxChart(VisualizationType newVisualizationType) {
        LocalDate endTime = LocalDate.now();
        LocalDate startTime = LocalDate.now().minus(Period.of(0, 0, 1));
        DateRange defaultDateRange = new DateRange(startTime, endTime);
        return generateTilesFxChart(newVisualizationType, defaultDateRange);
    }

    private Tile generateTilesFxChart(VisualizationType visualizationType, DateRange dateRange) {
        Tile chart = new Tile();
        chart.setPrefSize(TILE_WIDTH, TILE_HEIGHT);
        Tile.SkinType skinType = visualizationType == VisualizationType.BARCHART ?
                Tile.SkinType.MATRIX : Tile.SkinType.SMOOTHED_CHART;
        chart.setTextAlignment(TextAlignment.RIGHT);

        String interval = dateRange.getDefaultIntervalSize().getInfluxIntervalString();
        chart.setTitle(LanguageSelection.getLocalized(CHART_HEADER_IDENTIFIER));
        chart.setText(LanguageSelection.getLocalized(dateRange.getDefaultIntervalSize().name()));

        LOGGER.debug("Interval String is: " + interval);
        LOGGER.debug("Start time is " + dateRange.getStartDate().toString() + ", as Timestamp it is " + dateRange.getStartDateAtCurrentTime().getTime());
        LOGGER.debug("End time is " + dateRange.getEndDate().toString() + ", as Timestamp it is " + dateRange.getEndDateAtCurrentTime().getTime());
        List<ChartData> data = loadChartData(interval, dateRange.getStartDateAtCurrentTime().getTime(), dateRange.getEndDateAtCurrentTime().getTime());
        try {
            addCorrectDataType(skinType, chart, data);
        } catch (NotSupportedException e) {
            ExceptionPrinter.printHistory(e, LOGGER);
        }
        return chart;
    }

}