package org.openbase.bco.bcozy.controller.powerterminal.chartattributes;

public enum VisualizationType {
    BAR, PIE, WEBVIEW, LINECHART;

    @Override
    public String toString() {
        return super.toString().substring(0, 1) + super.toString().substring(1).toLowerCase();
    }
}