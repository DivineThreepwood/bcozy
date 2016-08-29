/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.bcozy.
 *
 * org.openbase.bco.bcozy is free software: you can redistribute it and modify it
 * under the terms of the GNU General Public License (Version 3) as published by
 * the Free Software Foundation.
 *
 * org.openbase.bco.bcozy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * org.openbase.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.bcozy.view.devicepanes;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.openbase.bco.bcozy.view.Constants;
import org.openbase.bco.bcozy.view.SVGIcon;
import org.openbase.jul.extension.rsb.com.AbstractIdentifiableRemote;
import org.openbase.bco.dal.remote.unit.DimmableLightRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.schedule.RecurrenceEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.state.BrightnessStateType.BrightnessState;
import rst.homeautomation.state.PowerStateType.PowerState;
import rst.homeautomation.state.PowerStateType.PowerState.State;
import rst.homeautomation.unit.DimmableLightDataType.DimmableLightData;

/**
 * Created by agatting on 12.01.16.
 */
public class DimmableLightPane extends UnitPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(DimmableLightPane.class);

    private RecurrenceEventFilter recurrenceEventFilter;
    private final SVGIcon unknownForegroundIcon;
    private final SVGIcon unknownBackgroundIcon;
    private final DimmableLightRemote dimmableLightRemote;
    private final ProgressBar progressBar;
    private final BorderPane headContent;
    private final SVGIcon lightBulbIcon;
    private final StackPane stackPane;
    private final VBox bodyContent;
    private final Slider slider;

    private final EventHandler<MouseEvent> sendBrightness = event -> new Thread(new Task() {
        @Override
        protected Object call() {
            try {
                dimmableLightRemote.setBrightnessState(BrightnessState.newBuilder().setBrightness(slider.getValue()).build());
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
            return null;
        }
    }).start();

    /**
     * Constructor for the DimmerPane.
     *
     * @param dimmerRemote dimmerRemote.
     */
    public DimmableLightPane(final AbstractIdentifiableRemote dimmerRemote) {
        this.dimmableLightRemote = (DimmableLightRemote) dimmerRemote;

        lightBulbIcon
                = new SVGIcon(MaterialDesignIcon.LIGHTBULB, MaterialDesignIcon.LIGHTBULB_OUTLINE, Constants.SMALL_ICON);
        unknownBackgroundIcon = new SVGIcon(MaterialDesignIcon.CHECKBOX_BLANK_CIRCLE, Constants.SMALL_ICON - 2, false);
        unknownForegroundIcon = new SVGIcon(MaterialDesignIcon.HELP_CIRCLE, Constants.SMALL_ICON, false);
        progressBar = new ProgressBar();
        headContent = new BorderPane();
        stackPane = new StackPane();
        bodyContent = new VBox();
        slider = new Slider();

        initUnitLabel();
        initTitle();
        initContent();
        createWidgetPane(headContent, bodyContent, true);
        initEffectAndSwitch();
        tooltip.textProperty().bind(observerText.textProperty());

        this.dimmableLightRemote.addDataObserver(this);
    }

    private void initEffectAndSwitch() {
        State powerState = State.OFF;
        double brightness = 0.0;

        try {
            powerState = dimmableLightRemote.getPowerState().getValue();
            brightness = dimmableLightRemote.getBrightnessState().getBrightness() / Constants.ONE_HUNDRED;
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        setEffectColorAndSlider(powerState, brightness);
    }

    private void setEffectColorAndSlider(final State powerState, final double brightness) {
        iconPane.getChildren().clear();

        if (powerState.equals(State.ON)) {
            iconPane.add(lightBulbIcon, 0, 0);

            final Color color = Color.hsb(Constants.LIGHTBULB_COLOR.getHue(),
                    Constants.LIGHTBULB_COLOR.getSaturation(), brightness, Constants.LIGHTBULB_COLOR.getOpacity());
            lightBulbIcon.setBackgroundIconColorAnimated(color);
            progressBar.setProgress(brightness);
            slider.setValue(brightness * slider.getMax());

            observerText.setIdentifier("lightOn");

            if (!toggleSwitch.isSelected()) {
                toggleSwitch.setSelected(true);
            }

        } else if (powerState.equals(State.OFF)) {
            iconPane.add(lightBulbIcon, 0, 0);

            lightBulbIcon.setBackgroundIconColorAnimated(Color.TRANSPARENT);
            progressBar.setProgress(0);
            slider.setValue(0);

            observerText.setIdentifier("lightOff");

            if (toggleSwitch.isSelected()) {
                toggleSwitch.setSelected(false);
            }
        } else {
            iconPane.add(unknownBackgroundIcon, 0, 0);
            iconPane.add(unknownForegroundIcon, 0, 0);
            observerText.setIdentifier("unknown");
        }
    }

    private void sendStateToRemote(final State state) {
        try {
            dimmableLightRemote.setPowerState(state);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            setWidgetPaneDisable(true);
        }
    }

    @Override
    protected void initTitle() {
        lightBulbIcon.setBackgroundIconColorAnimated(Color.TRANSPARENT);

        oneClick.addListener((observable, oldValue, newValue) -> new Thread(new Task() {
            @Override
            protected Object call() {
                if (toggleSwitch.isSelected()) {
                    sendStateToRemote(PowerState.State.OFF);
                } else {
                    sendStateToRemote(PowerState.State.ON);
                }
                return null;
            }
        }).start());

        toggleSwitch.setOnMouseClicked(event -> new Thread(new Task() {
            @Override
            protected Object call() {
                if (toggleSwitch.isSelected()) {
                    sendStateToRemote(PowerState.State.ON);
                } else {
                    sendStateToRemote(PowerState.State.OFF);
                }
                return null;
            }
        }).start());

        unknownForegroundIcon.setForegroundIconColor(Color.BLUE);
        unknownBackgroundIcon.setForegroundIconColor(Color.WHITE);

        headContent.setCenter(getUnitLabel());
        headContent.setAlignment(getUnitLabel(), Pos.CENTER_LEFT);
        headContent.prefHeightProperty().set(lightBulbIcon.getSize() + Constants.INSETS);
    }

    @Override
    protected void initContent() {
        //CHECKSTYLE.OFF: MagicNumber
        final double sliderWidth = 200;

        slider.setPrefHeight(25);
        slider.setMinHeight(25);
        //CHECKSTYLE.ON: MagicNumber
        slider.setMin(0);
        slider.setMax(Constants.ONE_HUNDRED);
        slider.setMinWidth(sliderWidth);
        slider.setMaxWidth(sliderWidth);

        this.recurrenceEventFilter = new RecurrenceEventFilter(Constants.FILTER_TIME) {
            @Override
            public void relay() {
                slider.setOnMouseDragged(sendBrightness);
                slider.setOnMouseClicked(sendBrightness);
            }
        };
        recurrenceEventFilter.trigger();

        progressBar.setMinWidth(sliderWidth);
        progressBar.setMaxWidth(sliderWidth);

        stackPane.getStyleClass().clear();
        stackPane.getStyleClass().add("dimmer-body");
        stackPane.getChildren().addAll(progressBar, slider);

        bodyContent.getChildren().add(stackPane);
        bodyContent.prefHeightProperty().set(slider.getPrefHeight() + Constants.INSETS);
    }

    @Override
    protected void initUnitLabel() {
        String unitLabel = Constants.UNKNOWN_ID;
        try {
            unitLabel = this.dimmableLightRemote.getData().getLabel();
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        setUnitLabelString(unitLabel);
    }

    @Override
    public AbstractIdentifiableRemote getDALRemoteService() {
        return dimmableLightRemote;
    }

    @Override
    void removeObserver() {
        this.dimmableLightRemote.removeObserver(this);
    }

    @Override
    public void update(final Observable observable, final Object dimmer) throws java.lang.Exception {
        Platform.runLater(() -> {
            final State powerState = ((DimmableLightData) dimmer).getPowerState().getValue();
            final double brightness = ((DimmableLightData) dimmer).getBrightnessState().getBrightness()/ Constants.ONE_HUNDRED;
            setEffectColorAndSlider(powerState, brightness);
        });
    }
}