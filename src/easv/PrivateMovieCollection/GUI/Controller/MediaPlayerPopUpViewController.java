/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.CategoryModel;
import easv.PrivateMovieCollection.GUI.Model.CategoryMovieModel;
import easv.PrivateMovieCollection.GUI.Model.MovieModel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class MediaPlayerPopUpViewController implements Initializable {

    @FXML
    public TableView<Movie> tblMoviesOld;
    @FXML
    public TableColumn<Movie, Double> colPersonalOld;
    @FXML
    public TableColumn<Movie, String> colNameOld;
    @FXML
    public TableColumn<Movie, Date> colLastViewedOld;
    @FXML
    public Button btnDelete;
    private MediaPlayerViewController mediaPlayerViewController;
    private final MovieModel movieModel;
    private Movie currentMovie;
    private final CategoryMovieModel categoryMovieModel;
    private static final Image mainIcon = new Image ("Icons/mainIcon.png");

    public MediaPlayerPopUpViewController() {
        try {
            movieModel = new MovieModel();
            categoryMovieModel = new CategoryMovieModel();
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
        contextSystem();
        startupSetup();
        initializeTableColumns();
    }

    private void initializeTableColumns() {
        // Initialize the tables with columns.
        colNameOld.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLastViewedOld.setCellValueFactory(new PropertyValueFactory<>("lastWatched"));
        colPersonalOld.setCellValueFactory(new PropertyValueFactory<>("personalRating"));
    }

    public void startupSetup() {
        tblMoviesOld.setItems(MovieModel.getObservableMoviesOld());
    }

//*******************************************CONTEXT*MENU**************************************************

    private void contextSystem() { //Here we create the context menu for the category combo box
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteAllMovies = new MenuItem("Delete  all movies");
        MenuItem deleteMovie = new MenuItem("Delete movie");
        contextMenu.getItems().addAll(deleteAllMovies,deleteMovie);
        tblMoviesOld.setContextMenu(contextMenu);

        tblMoviesOld.setRowFactory(tv -> {
            TableRow<Movie> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    currentMovie = row.getItem();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.getItems().clear();
                    currentMovie = row.getItem();
                    if (row.getIndex() >= tblMoviesOld.getItems().size()) {
                        contextMenu.getItems().addAll(deleteAllMovies);
                    } else {
                        contextMenu.getItems().addAll(deleteMovie, deleteAllMovies);
                    }
                }
            });
            return row;
        });

        deleteAllMovies.setOnAction((event) -> { // Deletes all movie in the list and close window
            try {
                btnDeleteAll();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteMovie.setOnAction((event) -> { // deletes the selected movie by calling the delete function
            try {
                btnDelete();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

//*******************************************KEYBOARD**************************************************
    @FXML
    private void keyboardKeyPressed(KeyEvent event) throws Exception { // Adds keyboard functionality
        KeyCode keyCode = event.getCode(); //Get the button press value

        if (keyCode == KeyCode.ESCAPE) { // Closes the window if escape is pressed
            btnCloseWindow();
        }

        if (keyCode == KeyCode.DELETE) { // Delete currently selected movie
            btnDelete();
        }
    }
//*******************************BUTTONS***********************************************

    public void btnDelete() throws Exception {
        Movie currentSelectedMovie = tblMoviesOld.getSelectionModel().getSelectedItem();

        try {
            for (Category c : CategoryModel.getObservableCategories()) { // This will check through each category and delete the movie from there since the movieId is a key in the DB
                categoryMovieModel.deleteMovieFromCategory(currentSelectedMovie, c);
            }
            movieModel.deleteMovie(currentSelectedMovie);
            tblMoviesOld.getItems().clear();
            tblMoviesOld.setItems(MovieModel.getObservableMoviesOld());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mediaPlayerViewController.refreshMovieList();
        mediaPlayerViewController.refreshCategories();
        if (tblMoviesOld.getItems().isEmpty()){
            btnCloseWindow();
        }
    }

    public void btnDeleteAll() throws Exception {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        alert.setTitle("Movie");
        alert.setHeaderText("You want to delete all these movies");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == okButton) {

            for (Movie m : MovieModel.getObservableMoviesOld()) {
                try {
                    for (Category c : CategoryModel.getObservableCategories()) { // This will check through each category and delete the movie from there since the movieId is a key in the DB
                        categoryMovieModel.deleteMovieFromCategory(m, c);
                    }
                    movieModel.deleteMovie(m);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            mediaPlayerViewController.refreshCategories();
            mediaPlayerViewController.refreshMovieList();
            btnCloseWindow();
        }
    }

    public void btnCancel() {
        btnCloseWindow();
    }

    public void btnCloseWindow() { //Close the window
        Stage parent = (Stage) tblMoviesOld.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}