/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MediaPlayerInfoViewController implements Initializable {
    @FXML
    private TextField txtInputName, txtInputArtist, txtInputYear, txtInputCategories, txtInputFilepath, txtInputTime, txtInputDate, txtInputPersonal, txtInputIMDBRating;
    @FXML
    private ImageView movieIcon;
    private MediaPlayerViewController mediaPlayerViewController;
    private final MovieModel movieModel;
    private final CategoryModel categoryModel;
    private final CategoryMovieModel categoryMovieModel;
    private final DisplayErrorModel displayErrorModel;
    private final ValidateModel validateModel = new ValidateModel();
    private static final Image mainIcon = new Image ("Icons/mainIcon.png");
    private final BooleanProperty isRateValid = new SimpleBooleanProperty(true);
    private static Movie currentSelectedMovie = null;

    public MediaPlayerInfoViewController() {
        try {
            movieModel = new MovieModel();
            categoryModel = new CategoryModel();
            categoryMovieModel = new CategoryMovieModel();
            displayErrorModel = new DisplayErrorModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            mediaPlayerViewController = MediaPlayerViewController.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentSelectedMovie = mediaPlayerViewController.getCurrentMovie();
        addValidationListener(txtInputPersonal, isRateValid);
        startupSetup();
    }

    public void startupSetup() {
        if (currentSelectedMovie != null) { //We set the movie text info in
            txtInputName.setText(currentSelectedMovie.getTitle());
            txtInputYear.setText(String.valueOf(currentSelectedMovie.getYear()));
            txtInputArtist.setText(currentSelectedMovie.getDirector());
            txtInputFilepath.setText(currentSelectedMovie.getMoviePath());
            txtInputTime.setText(currentSelectedMovie.getMovieLengthHHMMSS());
            txtInputDate.setText(currentSelectedMovie.getLastWatched());
            txtInputIMDBRating.setText(String.valueOf(currentSelectedMovie.getMovieRating()));
            txtInputPersonal.setText(String.valueOf(currentSelectedMovie.getPersonalRating()));

            try {
                List<Integer> categoryIds;
                List<String> categoryNames = new ArrayList<>();
                categoryIds = categoryMovieModel.getMovieCatList(currentSelectedMovie);
                for (Integer categoryId: categoryIds )  {
                    categoryNames.add(categoryModel.getCategoryById(categoryId).getCategoryName());
                }

                if (categoryNames.isEmpty())    {
                    txtInputCategories.setPromptText("Missing");
                    return;
                }
                txtInputCategories.setText(String.valueOf(categoryNames));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void addValidationListener(TextField textField, BooleanProperty validationProperty) { //Detect change and validate it
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validateModel.validateInput(textField, newValue);
            validationProperty.set(isValid);
            setBorderStyle(textField, isValid);
        });
    }

//*******************************************KEYBOARD**************************************************
    @FXML
    private void keyboardKeyPressed(KeyEvent event) throws Exception { // Adds keyboard functionality
        KeyCode keyCode = event.getCode(); //Get the button press value

        if (keyCode == KeyCode.ESCAPE) { // Closes the window if escape is pressed
            btnCloseWindow();
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.R) { // Saves the rating
                btnUpdateRate();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.U) { // Open selected song in update window
                btnUpdateFile();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.P) { // attempts to play the movie in user standard program
                btnPlay();
            }
        }

    }
//*******************************BUTTONS***********************************************

    public void btnUpdateRate() throws Exception { //Use to add a new category
        boolean isRateValid = validateModel.validateInput(txtInputPersonal, txtInputPersonal.getText());
        if (isRateValid) {
        currentSelectedMovie.setPersonalRating(Double.parseDouble((txtInputPersonal.getText())));
        movieModel.updateMovie(currentSelectedMovie);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        }
    }

    public void btnUpdateFile() throws Exception { //Open movie in update window
        mediaPlayerViewController.newUCWindow("Update");
        Stage updateStage = mediaPlayerViewController.getUpdateStage();
        updateStage.setOnHidden(event -> startupSetup());
    }

    public void btnPlayDirect() { // Play the movie in the computer default media player
        System.out.print("Play Direct");
    }
    public void btnPlay() throws Exception { // Play the movie in the computer default media player
        File videoFile = new File(currentSelectedMovie.getMoviePath());
        if (videoFile.exists()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(videoFile);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            currentSelectedMovie.setLastWatched(dtf.format(now));
            txtInputDate.setText(dtf.format(now));
            movieModel.updateMovie(currentSelectedMovie);
        }
        else {
            displayErrorModel.displayErrorC("Movie could not be found\n(It might have been moved to a different location)");
        }
    }

    public void btnCloseWindow() { //Close the window
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        parent.close();
    }

//*******************************STYLING***********************************************
    private void setBorderStyle(TextField textField, boolean isValid) { //We get the styling from the CSS file
        if (isValid) {
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Invalid"), false);  // Valid style
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Valid"), true);  // Valid style
        } else {
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Valid"), false);  // Valid style
            textField.pseudoClassStateChanged(PseudoClass.getPseudoClass("Invalid"), true);  // Valid style
        }
    }
}