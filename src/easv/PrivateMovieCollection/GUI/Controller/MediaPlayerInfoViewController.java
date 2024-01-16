/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MediaPlayerInfoViewController implements Initializable {
    @FXML
    private SVGPath starSVGPath;
    @FXML
    private TextField txtInputPersonalRating;
    @FXML
    private Label lblInputName, lblInputDirector, lblInputYear, lblInputCategories, lblInputTime, lblInputDate, lblInputIMDBRating, lblInputDesc;
    @FXML
    private ImageView movieIcon;
    private MediaPlayerViewController mediaPlayerViewController;
    private final MovieModel movieModel;
    private final DisplayErrorModel displayErrorModel;
    private final ValidateModel validateModel = new ValidateModel();
    private static final Image mainIcon = new Image ("Icons/mainIcon.png");
    private static final  String svgPathData = "M12 17.27l4.15 2.51c.76.46 1.69-.22 1.49-1.08l-1.1-4.72 3.67-3.18c.67-.58.31-1.68-.57-1.75l-4.83-.41" +
            "-1.89-4.46c-.34-.81-1.5-.81-1.84 0L9.19 8.63l-4.83.41c-.88.07-1.24 1.17-.57 1.75l3.67 3.18-1.1 4.72c-.2.86.73 1.54 1.49 1.08l4.15-2.5z";
    private static Movie currentSelectedMovie = null;

    public MediaPlayerInfoViewController() {
        try {
            movieModel = new MovieModel();
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
        try {
            startupSetup();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startupSetup() throws MalformedURLException {
        String posterPath;
        starSVGPath.setContent(svgPathData);
        if (currentSelectedMovie != null) { //We set the movie text info in
            lblInputName.setText(currentSelectedMovie.getTitle());
            lblInputDirector.setText("Director: " + currentSelectedMovie.getDirector());
            lblInputYear.setText("(" + (currentSelectedMovie.getYear()) + ")");
            lblInputTime.setText(currentSelectedMovie.getMovieLengthHHMMSS());
            if (currentSelectedMovie.getLastWatched() == null)
                lblInputDate.setText("Last seen: Never");
            else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                lblInputDate.setText("Last seen: " + sdf.format(Timestamp.valueOf(currentSelectedMovie.getLastWatched())));
            }
            if (currentSelectedMovie.getMovieDescription() == null)
                lblInputDesc.setText("N/A");
            else {
                lblInputDesc.setText(currentSelectedMovie.getMovieDescription());}
            lblInputIMDBRating.setText((currentSelectedMovie.getMovieRating()) + "/10");
            txtInputPersonalRating.setText(String.valueOf(currentSelectedMovie.getPersonalRating()));
            setRatingStarGUI(currentSelectedMovie.getMovieRating(), starSVGPath); //We update the star color

            posterPath = currentSelectedMovie.getPosterPath();
            if (posterPath != null && !posterPath.isEmpty()) {
                InputStream stream;
                try {
                    stream = new URL(posterPath).openStream();
                    Image image = new Image(stream);
                    stream.close();
                    movieIcon.setImage(image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                List<Integer> categoryIds;
                List<String> categoryNames = new ArrayList<>();
                categoryIds = CategoryMovieModel.getMovieCatList(currentSelectedMovie);
                for (Integer categoryId: categoryIds )  {
                    categoryNames.add(CategoryModel.getCategoryById(categoryId).getCategoryName());
                }
                if (categoryNames.isEmpty())    {
                    lblInputCategories.setText("N/A");
                    return;
                }
                lblInputCategories.setText(String.valueOf(categoryNames));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setRatingStarGUI(double rating, SVGPath starSVGPath) {
        // Ensure the rating is valid and set the percentage
        double percentage = Math.max(0.0, Math.min(10.0, rating))/10.0;
        // Update the gradient based on the percentage
        LinearGradient gradient = new LinearGradient(0, 1, 0, 0, true, null,
                new javafx.scene.paint.Stop(percentage, javafx.scene.paint.Color.YELLOW),
                new javafx.scene.paint.Stop(percentage, javafx.scene.paint.Color.WHITE));
        starSVGPath.setFill(gradient);
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
            if (keyCode == KeyCode.U) { // Open selected Movie in update window
                btnUpdateFile();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.P) { // attempts to play the movie in user standard program
                btnPlay();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.D) { // attempts to play the movie in user standard program
                btnPlayDirect();
            }
        }
    }
//*******************************BUTTONS***********************************************
    public void btnUpdateRate() throws Exception { //Use to add a new category
        boolean isRateValid = validateModel.validateInput(txtInputPersonalRating,  txtInputPersonalRating.getText().replaceAll(",", "."));
        if (isRateValid) {
        currentSelectedMovie.setPersonalRating(Double.parseDouble((txtInputPersonalRating.getText().replaceAll(",", "."))));
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
        updateStage.setOnHidden(event -> {
            try {
                startupSetup();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void btnPlayDirect() throws Exception{ // Play the movie in the computer default media player
        File videoFile = new File(currentSelectedMovie.getMoviePath());
        if (videoFile.exists()) {
            mediaPlayerViewController.PlayMovie(currentSelectedMovie);
            LocalDateTime truncatedDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            currentSelectedMovie.setLastWatched(String.valueOf(truncatedDateTime));
            movieModel.updateMovie(currentSelectedMovie);
            btnCloseWindow();
        }
        else {
            displayErrorModel.displayErrorC("Movie could not be found\n(It might have been moved to a different location)");
        }
    }
    public void btnPlay() throws Exception { // Play the movie in the computer default media player
        File videoFile = new File(currentSelectedMovie.getMoviePath());
        if (videoFile.exists()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(videoFile);
            LocalDateTime truncatedDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            currentSelectedMovie.setLastWatched(String.valueOf(truncatedDateTime));
            movieModel.updateMovie(currentSelectedMovie);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            lblInputDate.setText("Last seen : " + truncatedDateTime.format(formatter));
        }
        else {
            displayErrorModel.displayErrorC("Movie could not be found\n(It might have been moved to a different location)");
        }
    }

    public void btnCloseWindow() throws Exception{ //Close the window
        Stage parent = (Stage) lblInputYear.getScene().getWindow();
        btnUpdateRate();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}