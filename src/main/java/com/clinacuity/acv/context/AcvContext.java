package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class AcvContext {
    private static final AcvContext instance = new AcvContext();
    private AppMainController mainController = null;
    private Font mainFont;
    private double fontPixelHeight = -1.0d;
    private double fontPixelWidth = -1.0d;

    public static AcvContext getInstance() {
        return instance;
    }

    public AppMainController getMainController() {
        return mainController;
    }

    public void setMainController(AppMainController controller) {
        mainController = controller;
    }

    public Font getFont() {
        return mainFont;
    }

    public void setFont(Font font) {
        mainFont = font;
    }

    public double getFontPixelHeight() {
        if (fontPixelHeight < 0) {
            Text text = new Text("A");
            text.applyCss();
            fontPixelHeight = text.getLayoutBounds().getHeight();
        }

        return fontPixelHeight;
    }

    public double getFontPixelWidth() {
        if (fontPixelHeight < 0) {
            Text text = new Text("A");
            text.applyCss();
            fontPixelHeight = text.getLayoutBounds().getWidth();
        }

        return fontPixelWidth;
    }
}
