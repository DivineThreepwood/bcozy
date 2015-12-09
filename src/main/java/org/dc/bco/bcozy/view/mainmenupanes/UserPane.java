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
package org.dc.bco.bcozy.view.mainmenupanes;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.dc.bco.bcozy.view.Constants;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by hoestreich on 11/24/15.
 */
public class UserPane extends VBox {

    private final PaneElement userIcon;
    private final PaneElement login;
    private final PaneElement loginPane;
    private final PaneElement loggedInBottomPane;
    private final PaneElement loggedInUserPane;
    private final Button openLoginBtn;
    private final Button loginBtn;
    private final Button backBtn;
    private final Button logoutBtn;
    private final TextField nameTxt;
    private final PasswordField passwordField;
    private final Label loggedInUserLbl;

    /**
     * Enum to control the display state.
     */
    public enum State { NOLOGIN, LOGINACTIVE, LOGIN }

    /**
     * Constructor for the UserPane.
     */
    public UserPane() {

        final ResourceBundle languageBundle = ResourceBundle
                .getBundle("languages.languages", new Locale("de", "DE"));

        // Case: No user logged in
        final Image icon = new Image(getClass().getResourceAsStream("/icons/user_fa.png"));
        final ImageView userIconImageView = new ImageView(icon);
        userIconImageView.setFitHeight(Constants.BIG_ICON);
        userIconImageView.setFitWidth(Constants.BIG_ICON);
        userIcon = new PaneElement(userIconImageView);
        userIcon.setMaxWidth(Constants.MAX_MENU_WIDTH);
        openLoginBtn = new Button(languageBundle.getString("startLogin"));
        login = new PaneElement(openLoginBtn);
        login.setMaxWidth(Constants.MAX_MENU_WIDTH);

        // Case: Login active
        final Label nameLbl = new Label(languageBundle.getString("username"));
        nameTxt = new TextField();
        final Label pwLbl = new Label(languageBundle.getString("password"));
        passwordField = new PasswordField();
        loginBtn = new Button(languageBundle.getString("login"));
        final Image backIcon = new Image(getClass().getResourceAsStream("/icons/back.png"));
        final ImageView imageViewBackIcon = new ImageView(backIcon);
        imageViewBackIcon.setFitWidth(Constants.SMALL_ICON);
        imageViewBackIcon.setFitHeight(Constants.SMALL_ICON);
        backBtn = new Button("", imageViewBackIcon);
        final VBox loginLayout = new VBox(Constants.INSETS);
        final BorderPane loginFirstLineLayout = new BorderPane();
        loginFirstLineLayout.setLeft(nameLbl);
        loginFirstLineLayout.setRight(backBtn);
        loginLayout.getChildren().addAll(loginFirstLineLayout, nameTxt, pwLbl, passwordField, loginBtn);
        loginPane = new PaneElement(loginLayout);
        loginPane.setMaxWidth(Constants.MAX_MENU_WIDTH);

        //Case: User logged in
        final Image loggedInUserIcon = new Image(getClass().getResourceAsStream("/icons/user.png"));
        final ImageView loggedInUserIconImageView = new ImageView(loggedInUserIcon);
        loggedInUserIconImageView.setFitHeight(Constants.BIG_ICON);
        loggedInUserIconImageView.setFitWidth(Constants.BIG_ICON);
        loggedInUserPane = new PaneElement(loggedInUserIconImageView);
        loggedInUserPane.setMaxWidth(Constants.MAX_MENU_WIDTH);
        loggedInUserLbl = new Label("");
        final Image logoutIcon = new Image(getClass().getResourceAsStream("/icons/logout.png"));
        final ImageView imageViewLogoutIcon = new ImageView(logoutIcon);
        imageViewLogoutIcon.setFitWidth(Constants.SMALL_ICON);
        imageViewLogoutIcon.setFitHeight(Constants.SMALL_ICON);
        logoutBtn = new Button("", imageViewLogoutIcon);
        final BorderPane loggedInBottomLayout = new BorderPane();
        loggedInBottomLayout.setLeft(loggedInUserLbl);
        loggedInBottomLayout.setRight(logoutBtn);
        loggedInBottomPane = new PaneElement(loggedInBottomLayout);
        loggedInBottomPane.setMaxWidth(Constants.MAX_MENU_WIDTH);

        this.setFillWidth(true);
        this.setSpacing(Constants.INSETS);

        this.getChildren().addAll(userIcon, login);

    }

    /**
     * Getter for the Button which opens the login menu.
     * @return instance of the button
     */
    public Button getOpenLoginBtn() {
        return openLoginBtn;
    }

    /**
     * Getter for the login button which initiates the user login.
     * @return instance of the button
     */
    public Button getLoginBtn() {
        return loginBtn;
    }

    /**
     * Getter for the back button to abort a login.
     * @return instance of the button
     */
    public Button getBackBtn() {
        return backBtn;
    }

    /**
     * Getter for the logout which logs out the current user.
     * @return instance of the button
     */
    public Button getLogoutBtn() {
        return logoutBtn;
    }

    /**
     * Getter for the name textfield.
     * @return instance of the textfield
     */
    public TextField getNameTxt() {
        return nameTxt;
    }

    /**
     * Getter for the passwordfield.
     * @return instance of the passwordfield
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Getter for the Label that displays the currently user.
     * @return instance of the label
     */
    public Label getLoggedInUserLbl() {
        return loggedInUserLbl;
    }

    /**
     * GUI Method to switch the displayed panes.
     * @param state A state from the defined Enum
     */
    public void setState(final State state) {
        switch (state) {
            case NOLOGIN:
                this.getChildren().clear();
                this.getChildren().addAll(userIcon, login);
                break;

            case LOGINACTIVE:
                this.getChildren().clear();
                this.getChildren().addAll(loginPane);
                break;

            case LOGIN:
                this.getChildren().clear();
                this.getChildren().addAll(loggedInUserPane, loggedInBottomPane);
                break;

            default:
                this.getChildren().clear();
                this.getChildren().addAll(userIcon, login);
                break;

        }
    }
}