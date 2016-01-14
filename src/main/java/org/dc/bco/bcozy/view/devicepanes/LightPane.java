/**
 * ==================================================================
 *
 * This file is part of org.dc.bco.bcozy.
 *
 * org.dc.bco.bcozy is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.dc.bco.bcozy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.dc.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.dc.bco.bcozy.view.devicepanes;

import org.dc.bco.dal.remote.unit.DALRemoteService;
import org.dc.bco.dal.remote.unit.LightRemote;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.pattern.Observable;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.ToggleSwitch;
import org.dc.bco.bcozy.view.Constants;
import org.dc.bco.bcozy.view.SVGIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.state.PowerStateType.PowerState.State;
import rst.homeautomation.unit.LightType.Light;

/**
 * Created by timo on 08.01.16.
 */
public class LightPane extends UnitPane {
    private static final Logger LOGGER = LoggerFactory.getLogger(LightPane.class);

    private final LightRemote lightRemote;
    private final SVGIcon lightbulbIcon;
    private final ToggleSwitch toggleSwitch;
    private final BorderPane headContent;

    /**
     * Constructor for the LightPane.
     * @param lightRemote lightRemote
     */
    public LightPane(final DALRemoteService lightRemote) {
        this.lightRemote = (LightRemote) lightRemote;

        toggleSwitch = new ToggleSwitch();
        lightbulbIcon =
                new SVGIcon(MaterialDesignIcon.LIGHTBULB, MaterialDesignIcon.LIGHTBULB_OUTLINE, Constants.SMALL_ICON);
        headContent = new BorderPane();

        try {
            super.setUnitLabel(this.lightRemote.getData().getLabel());
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            super.setUnitLabel("UnknownID");
        }

        initTitle();
        initContent();
        createWidgetPane(headContent);

        try {
            initEffectAndSwitch();
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        this.lightRemote.addObserver(this);
    }

    private void initEffectAndSwitch() throws CouldNotPerformException {
        if (lightRemote.getPower().getValue().equals(State.ON)) {
            lightbulbIcon.setBackgroundIconColorAnimated(Constants.LIGHTBULB_COLOR);

            if (!toggleSwitch.isSelected()) {
                toggleSwitch.setSelected(true);
            }
        } else if (lightRemote.getPower().getValue().equals(State.OFF)) {
            lightbulbIcon.setBackgroundIconColorAnimated(Color.TRANSPARENT);

            if (toggleSwitch.isSelected()) {
                toggleSwitch.setSelected(false);
            }
        }
    }

    /**
     * Method creates the header content of the widgetPane.
     */
    @Override
    protected void initTitle() {
        lightbulbIcon.setBackgroundIconColorAnimated(Color.TRANSPARENT);
        toggleSwitch.setOnMouseClicked(event -> {
            if (toggleSwitch.isSelected()) {
                try {
                    lightRemote.setPower(State.ON);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            } else {
                try {
                    lightRemote.setPower(State.OFF);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        });

        headContent.setLeft(lightbulbIcon);
        headContent.setCenter(new Label(super.getUnitLabel()));
        headContent.setRight(toggleSwitch);
        //Padding values are not available here
        headContent.prefHeightProperty().set(lightbulbIcon.getSize() + Constants.INSETS);
    }

    /**
     * Method creates the body content of the widgetPane.
     */
    @Override
    protected void initContent() {
        //No body content.
    }

    @Override
    public DALRemoteService getDALRemoteService() {
        return lightRemote;
    }

    @Override
    void removeObserver() {
        this.lightRemote.removeObserver(this);
    }

    @Override
    public void update(final Observable observable, final Object light) throws java.lang.Exception {
        Platform.runLater(() -> {
            if (((Light) light).getPowerState().getValue().equals(State.ON)) {
                lightbulbIcon.setBackgroundIconColorAnimated(Constants.LIGHTBULB_COLOR);
                if (!toggleSwitch.isSelected()) {
                    toggleSwitch.setSelected(true);
                }
            } else {
                lightbulbIcon.setBackgroundIconColorAnimated(Color.TRANSPARENT);
                if (toggleSwitch.isSelected()) {
                    toggleSwitch.setSelected(false);
                }
            }
        });
    }
}