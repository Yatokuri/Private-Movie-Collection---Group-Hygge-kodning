/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.CategoryModel;
import easv.PrivateMovieCollection.GUI.Model.CategoryMovieModel;
import easv.PrivateMovieCollection.GUI.Model.DisplayErrorModel;
import easv.PrivateMovieCollection.GUI.Model.MovieModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MediaPlayerViewController implements Initializable {
    @FXML
    public MenuButton btnCategoryFilter, btnMinimumIMDB;
    @FXML
    private HBox vboxTblBtn, mediaViewBox, hBoxMediaPlayer, hBoxFilter;
    @FXML
    private VBox tblMoviesInCategoryVBOX;
    @FXML
    private TableView<Category> tblCategory;
    @FXML
    private MediaView mediaView;
    @FXML
    private TableView<Movie> tblMoviesInCategory, tblMovies;
    @FXML
    private TableColumn<Movie, String> colTitleInCategory, colArtistInCategory, colName, colIMDBRating, colPersonal;
    @FXML
    private TableColumn<Category, String> colCategoryName;
    @FXML
    private TableColumn<Movie, Integer> colYear, colMovieCount;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView btnPlayIcon, btnRepeatIcon, btnShuffleIcon;
    @FXML
    private Button btnCreateCategory, btnUpdateCategory, btnPlay, btnSpeed, btnFilterIMDBArrow;
    @FXML
    private TextField txtMovieSearch;
    @FXML
    private Label lblPlayingNow, lblMovieDuration, lblCurrentMovieProgress, lblVolume;
    @FXML
    private Slider sliderProgressMovie, sliderProgressVolume;
    @FXML
    private MediaPlayer currentVideo = null;
    private final Map<Integer, MediaPlayer> soundMap = new HashMap<>(); //Every movie has a unique id
    private List<Movie> currentMovieList = new ArrayList<>();
    private boolean isUserChangingSlider = false;
    private boolean isVideoPaused = false;
    private int repeatMode = 0; //Default repeat mode
    private int shuffleMode = 0; //Default shuffle
    private int currentIndex = 0;
    private boolean previousPress = false;
    private boolean isVideoModeActive = false;
    public List<Double> speeds = new ArrayList<>();
    private int currentSpeedIndex = 4; // This should be where 1.00
    private Double currentSpeed = 1.00;
    private Category currentCategory, currentCategoryPlaying; //The current playing selected and playing from
    private Movie currentMovie, currentMoviePlaying; //The current Movie selected and playing
    private MovieModel movieModel;
    private CategoryModel categoryModel;
    private CategoryMovieModel categoryMovieModel;
    private final DisplayErrorModel displayErrorModel;
    private MediaPlayerCUViewController mediaPlayerCUViewController;
    private Movie draggedMovie;
    private String currentTableview;
    private static MediaPlayerViewController instance;

    private static final Image shuffleIcon = new Image("Icons/shuffle.png");
    private static final Image shuffleIconDisable = new Image("Icons/shuffle-disable.png");
    private static final Image repeatIcon = new Image("Icons/repeat.png");
    private static final Image repeat1Icon = new Image("Icons/repeat-once.png");
    private static final Image repeatDisableIcon = new Image("/Icons/repeat-disable.png");
    private static final Image playIcon = new Image("Icons/play.png");
    private static final Image pauseIcon = new Image("Icons/pause.png");
    private static final Image mainIcon = new Image("Icons/mainIcon.png");
    private static final String playingGraphic = "-fx-background-color: rgb(42,194,42); -fx-border-color: #1aa115; -fx-background-radius: 15px; -fx-border-radius: 15px 15px 15px 15px;";


    public Movie getCurrentMovie() {
        return currentMovie;
    }

    public MediaPlayerViewController() {
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            movieModel = new MovieModel();
            categoryModel = new CategoryModel();
            categoryMovieModel = new CategoryMovieModel();
            currentCategory = null;
        } catch (Exception e) {
            displayErrorModel.displayError(e);
            e.printStackTrace();
        }
    }

    public static MediaPlayerViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerViewController();
        }
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mediaPlayerCUViewController = MediaPlayerCUViewController.getInstance();


        btnRepeatIcon.setImage(repeatDisableIcon); //We set picture here so the button know what is chosen
        btnShuffleIcon.setImage(shuffleIconDisable); // -||-
        sliderProgressMovie.setPickOnBounds(false); // So you only can use slider when actually touch it
        sliderProgressVolume.setPickOnBounds(false); // -||-
        tblMoviesInCategory.setPlaceholder(new Label("No movies found"));
        tblMovies.setPlaceholder(new Label("No movies found"));
        tblCategory.setPlaceholder(new Label("No category found"));
        tblMoviesInCategoryVBOX.setManaged(false); // Hide movies in category while no category is selected


        // Initializes the Observable list into a Filtered list for use in the search function
        FilteredList<Movie> filteredMovies = new FilteredList<>(FXCollections.observableList(MovieModel.getObservableMovies()));
        tblMovies.setItems(filteredMovies);

        // Adds a FilterList to the tblMovies that will automatically filter based on search input through the use
        // of a FilteredList made from our observable list of movies
        txtMovieSearch.textProperty().addListener((observable, oldValue, newValue) ->
                tblMovies.setItems(movieModel.filterList(MovieModel.getObservableMovies(), newValue.toLowerCase()))
        );

        // Initialize the tables with columns.
        initializeTableColumns();

        // Add data from observable list
        tblMovies.setItems(MovieModel.getObservableMovies());
        tblCategory.setItems(CategoryModel.getObservableCategories());

        colCategoryName.setSortType(TableColumn.SortType.ASCENDING);
        tblCategory.getSortOrder().add(colCategoryName);

        // Set default volume to 10% (↓) and updates movie progress
        sliderProgressVolume.setValue(0.1F);
        setVolume();
        updateProgressStyle();

        speeds.addAll(Arrays.asList(0.25, 0.50 ,0.75, 1.00, 1.25, 1.5, 1.75, 2.0, 3.0, 4.0, 5.0)); // We add the speeds to the list
        btnSpeed.setText(currentSpeed + "x");

        // Add tableview functionality
        playMovieFromTableViewCategory();
        playMovieFromTableView();
        clearSelectionForCategorySelect();
        contextSystem();
        initializeDragAndDrop();
        btnCategoryFilter();
        btnMinimumIMDBRating();

        //Open new window

        if (!MovieModel.getObservableMoviesOld().isEmpty())  {
            try {
                warningWindow("Warning");
                Platform.runLater(() -> {
                    stage.toFront();
                    stage.show();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        hBoxMediaPlayer.setVisible(false);
        AnchorPane.setBottomAnchor(vboxTblBtn, 5.0);
    }

    private void initializeTableColumns() {
        // Initialize the tables with columns.
        colName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colIMDBRating.setCellValueFactory(new PropertyValueFactory<>("movieRating"));
        colPersonal.setCellValueFactory(new PropertyValueFactory<>("personalRating"));

        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colMovieCount.setCellValueFactory(new PropertyValueFactory<>("movieCount"));

        colTitleInCategory.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtistInCategory.setCellValueFactory(new PropertyValueFactory<>("director"));
    }

    //********************************************Search&*Filter***********************************************
    private static final ArrayList<Integer> categoryFilter = new ArrayList<>();
    private static double minimumIMDBRating;
    private static String filterIMDBArrow = "⯅"; // So filter know we want result under or over a number
    public void btnCategoryFilter(){
        CategoryModel.getObservableCategories().forEach(category -> {
            CheckMenuItem categoryItem = new CheckMenuItem(category.getCategoryName());
            categoryItem.setUserData(category.getId()); // Store the category as user data id
            categoryItem.setOnAction(event);
            btnCategoryFilter.getItems().add(categoryItem);
        });
    }

    public void btnMinimumIMDBRating(){
        ToggleGroup imdbToggleGroup = new ToggleGroup();

        RadioMenuItem imdbOffItem = new RadioMenuItem("Off");
        imdbOffItem.setToggleGroup(imdbToggleGroup);
        btnMinimumIMDB.getItems().add(imdbOffItem);

        List<Double> decimalValues = new ArrayList<>();
        for (double i = 1.0; i <= 10.0; i += 1.0) {
            decimalValues.add(i);
        }
        decimalValues.forEach(imdbRating -> {
            RadioMenuItem imdbItem = new RadioMenuItem(String.valueOf(imdbRating));
            imdbItem.setToggleGroup(imdbToggleGroup);

            imdbOffItem.setOnAction(event -> {
                if (imdbOffItem.isSelected() && categoryFilter.isEmpty())   {
                    minimumIMDBRating = 0.0;
                    try {tblMovies.setItems(movieModel.updateMovieList());}
                    catch (Exception ex) { throw new RuntimeException(ex);}
                }
            });
            imdbItem.setOnAction(event -> {
                if (imdbItem.isSelected() || imdbOffItem.isSelected()) {
                    minimumIMDBRating = imdbRating;
                    try {tblMovies.setItems(movieModel.updateMovieListFilter());}
                    catch (Exception ex) { throw new RuntimeException(ex);}
                }
            });
            btnMinimumIMDB.getItems().add(imdbItem);
        });
    }

    public EventHandler<ActionEvent> event = e -> {
        if (((CheckMenuItem) e.getSource()).isSelected()) {
            // Parse the ID as an integer and add it to categoryFilter
            categoryFilter.add((Integer) ((CheckMenuItem) e.getSource()).getUserData());
            // Retrieve and print the Category object stored as user data
            try {tblMovies.setItems(movieModel.updateMovieListFilter());}
            catch (Exception ex) { throw new RuntimeException(ex); }
        }
        else {
            categoryFilter.remove((Integer) ((CheckMenuItem) e.getSource()).getUserData());
            if (categoryFilter.isEmpty()) {
                try {tblMovies.setItems(movieModel.updateMovieList());}
                catch (Exception ex) { throw new RuntimeException(ex);}
            }
            else
                try {tblMovies.setItems(movieModel.updateMovieListFilter());}
                catch (Exception ex) { throw new RuntimeException(ex); }
        }   // Make sure we search again what there is already in search text in the new list automatic
        tblMovies.setItems(movieModel.filterList(MovieModel.getObservableMovies(), txtMovieSearch.getText().toLowerCase()));
    };
    public static ArrayList<String> getCategoryFilter(){
        ArrayList<String> categoryFilterString = new ArrayList<>();
        for (Integer i: categoryFilter)
            categoryFilterString.add(String.valueOf(i));
        return categoryFilterString;
    }
    public static double getMinimumIMDBFilter(){
        return minimumIMDBRating;
    }

    public static String getFilterIMDBArrow(){
        return filterIMDBArrow;
    }

//*******************************************CONTEXT*MENU**************************************************

    ContextMenu contextMenuMoviesInCategory = new ContextMenu();
    ContextMenu contextMenuCategory = new ContextMenu();
    ContextMenu contextMenuMovies = new ContextMenu();
    MenuItem deleteCategory = new MenuItem("Delete");
    MenuItem deleteMovie = new MenuItem("Delete");
    MenuItem deleteMovieInCategory = new MenuItem("Delete");
    MenuItem createCategory = new MenuItem("Create Category");
    MenuItem updateCategory = new MenuItem("Update Category");
    MenuItem deleteAllMovies = new MenuItem("Delete All Movies");
    Menu categorySubMenu = new Menu("Add to Category");

    // Initializes the submenu for the contextmenu that allows you to add movies to category and have it update dynamically
    private void contextSystemSubMenuAddMovies() {
        categorySubMenu.getItems().clear();
        CategoryModel.getObservableCategories().forEach(category -> {
            MenuItem categoryItem = new MenuItem(category.getCategoryName());
            categoryItem.setUserData(category); // Store the category as user data
            categorySubMenu.getItems().add(categoryItem);
        });
    }

    // Setup for the context menu system in the program with it dynamically removing options unless you have a movie or category selected
    private void contextSystem() {
        MenuItem createMovie = new MenuItem("Create Movie");
        MenuItem updateMovie = new MenuItem("Update Movie");
        MenuItem playMovie = new MenuItem("Play Movie");

        contextMenuCategory.getItems().addAll(createCategory, updateCategory, deleteCategory, deleteAllMovies);
        contextMenuMovies.getItems().addAll(playMovie, createMovie, updateMovie, deleteMovie, categorySubMenu);
        contextMenuMoviesInCategory.getItems().addAll(deleteMovieInCategory);

        tblMovies.setContextMenu(contextMenuMovies);
        tblMoviesInCategory.setContextMenu(contextMenuMoviesInCategory);
        tblCategory.setContextMenu(contextMenuCategory);

        tblMovies.setRowFactory(tv -> {
            TableRow<Movie> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    currentMovie = row.getItem();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuMovies.getItems().clear();
                    currentMovie = row.getItem();
                    if (row.getIndex() >= tblMovies.getItems().size()) {
                        contextMenuMovies.getItems().addAll(createMovie);
                    } else {
                        contextSystemSubMenuAddMovies(); //We update the category list
                        contextMenuMovies.getItems().addAll(playMovie, createMovie, updateMovie, deleteMovie, categorySubMenu);
                    }
                }
            });
            return row;
        });

        createMovie.setOnAction((event) -> {
            try {
                btnNewWindowCreate();
                contextMenuMovies.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        playMovie.setOnAction((event) -> {
            PlayMovie(currentMovie);
            contextMenuMovies.hide();
        });

        updateMovie.setOnAction((event) -> {
            try {
                btnNewWindowUpdate();
                contextMenuMovies.hide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        createCategory.setOnAction((event) -> {
            try {
                btnCreateCategoryNow();
                contextMenuCategory.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        categorySubMenu.setOnAction(event -> {
            try {

                if (event.getSource() instanceof MenuItem) {
                    MenuItem selectedItem = (MenuItem) event.getTarget();
                    Category selectedCategory = (Category) selectedItem.getUserData();
                    currentCategory = categoryModel.getCategoryById(selectedCategory.getId());
                }
                categoryMovieModel.categoryMovies(currentCategory);

                if (categoryMovieModel.addMovieToCategory(currentMovie, currentCategory)) { //We first need to make sure it not already in the category
                    tblCategory.getSelectionModel().select(currentCategory);
                    categoryMovieModel.categoryMovies(tblCategory.getSelectionModel().getSelectedItem());
                    refreshCategories();
                    contextMenuMovies.hide();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Category System");
                    alert.setHeaderText("The movie is already in the category");
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);
                    alert.showAndWait();
                    contextMenuMovies.hide();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        updateCategory.setOnAction((event) -> {
            try {
                btnUpdateCategoryNow();
                contextMenuCategory.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteMovie.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuMovies.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteMovieInCategory.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuMovies.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteCategory.setOnAction((event) -> {
            try {
                handleDelete();
                contextMenuMovies.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        deleteAllMovies.setOnAction((event) -> {
            try {
                categoryMovieModel.deleteAllMoviesFromCategory(tblCategory.getSelectionModel().getSelectedItem());
                refreshCategories();
                contextMenuCategory.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


//*******************************************PROGRESS*SYSTEM**************************************************

    private void updateProgressStyle() { // We wait 1 second, after we listen to change in slider and take care of it.
        Timeline updater = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            setSliderVolumeStyle();
            setSliderMovieProgressStyle();
        }));
        //updater.setCycleCount(Timeline.INDEFINITE); //The way to run it so many times u want
        updater.play();

        sliderProgressVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderVolumeStyle();
            setVolume();
        });

        sliderProgressMovie.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSliderMovieProgressStyle();
            updateMovieProgressTimer();
        });
    }

    public void setVolume() { // Sets the volume for the media player to the standard 10%
        double progress = sliderProgressVolume.getValue();
        int percentage = (int) (progress * 100);
        lblVolume.setText(String.format("%d%%", percentage));

        if (currentVideo != null) {
            currentVideo.setVolume((sliderProgressVolume.getValue()));
        }
    }

    private void updateMovieProgressTimer() { // Update the slider for movies with time
        if (currentVideo != null) {
            double progressValue = sliderProgressMovie.getValue();
            long currentSeconds = (long) progressValue;
            lblCurrentMovieProgress.setText(String.format("%02d:%02d:%02d", currentSeconds / 3600, (currentSeconds % 3600) / 60, currentSeconds % 60)); //Format HH:MM:SS
            Duration totalDuration = currentVideo.getTotalDuration();
            long totalSeconds = (long) totalDuration.toSeconds();
            lblMovieDuration.setText(String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)); //Format HH:MM:SS
        } else {
            lblCurrentMovieProgress.setText("00:00:00");
            lblMovieDuration.setText("00:00:00");
        }
    }
//********************************MEDIA*PLAYER*FUNCTION**************************************************

    private CompletableFuture<Void> addMoviesToSoundMap(Movie s) { // Will try to add the selected movie to the sound map when trying to play it
        CompletableFuture<Void> future = new CompletableFuture<>(); // We use to make sure the Media player is 100% done before going next
        if (soundMap.get(s.getId()) == null) { // Check if the movie is not already in the soundMap
            Path filePath = Paths.get(s.getMoviePath());
            try {
                CompletableFuture.runAsync(() -> {
                    if (Files.exists(filePath)) {
                        MediaPlayer mp = new MediaPlayer(new Media(new File(String.valueOf(filePath)).toURI().toString()));
                        mp.setOnReady(() -> {
                            mp.getTotalDuration();
                            soundMap.put(s.getId(), mp);
                            future.complete(null); // Signal completion
                        });
                    } else { // File does not exist, use the error sound
                        MediaPlayer mp = new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString()));
                        mp.setOnReady(() -> {
                            mp.getTotalDuration();
                            soundMap.put(s.getId(), mp);
                            future.complete(null); // Signal completion
                        });
                    }
                }).exceptionally(ex -> null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        } else { //This is when movie is there already
            future.complete(null); // Signal completion
        }
        return future;
    }

    private void handleMoviePlay() { //Tries to figure out in which table view the movie you want to play is located
        Movie selectedMovie;
        if (currentVideo != null) {
            selectedMovie = null;
            togglePlayPause();
        } else if (tblMovies.getSelectionModel().getSelectedItem() != null)
            selectedMovie = tblMovies.getSelectionModel().getSelectedItem();
        else if (tblMoviesInCategory.getSelectionModel().getSelectedItem() != null) {
            selectedMovie = tblMoviesInCategory.getSelectionModel().getSelectedItem();
            currentMovieList = categoryMovieModel.getObservableCategoriesMovie();
        } else {
            selectedMovie = null;
        }
        if (selectedMovie != null) {
            addMoviesToSoundMap(selectedMovie).thenRun(() -> {

                MediaPlayer newMovie = soundMap.get(selectedMovie.getId());
                if (currentVideo != newMovie && newMovie != null) {
                    handleNewMovie(newMovie, selectedMovie);
                }
            });
        }
    }

    private void playMovieFromTableView() { //Plays a movie from the movie list table view
        tblMovies.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY)
                tblMoviesInCategory.getSelectionModel().clearSelection(); // Clears selection from movie in the category to stop delete from interacting weirdly
            if (event.getClickCount() == 3 && event.getButton() == MouseButton.PRIMARY) { // Check for double-click

                Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();
                sliderProgressMovie.setValue(0);
                isVideoPaused = false;
                PlayMovie(selectedMovie);
            }
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) { // Check for double-click
                currentMovieList = MovieModel.getObservableMovies();
                currentIndex = currentMovieList.indexOf(tblMovies.getSelectionModel().getSelectedItem());
                currentCategoryPlaying = null;
                Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();
                // Checks if a valid movie was selected and if it was, tries to play it
                if (selectedMovie != null) {
                    try {
                        currentMovie = selectedMovie;
                        MediaPlayerCUViewController.setTypeCU(2);
                        newInfoWindow(selectedMovie.getTitle());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void playMovieFromTableViewCategory() { // Plays a movie from the movieInCategory Table view
        tblMoviesInCategory.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                tblMovies.getSelectionModel().clearSelection(); // Clears the selection from the normal table view for movies to stop delete from deleting the wrong movie
            }
            if (event.getClickCount() == 2) { // Check for double-click
                currentMovieList = categoryMovieModel.getObservableCategoriesMovie();
                currentIndex = currentMovieList.indexOf(tblMoviesInCategory.getSelectionModel().getSelectedItem());
                currentCategoryPlaying = currentCategory;
                Movie selectedMovie = tblMoviesInCategory.getSelectionModel().getSelectedItem();
                // Checks if a movie exists and if it does, it plays the movie
                if (selectedMovie != null) {
                    try {
                        currentMovie = selectedMovie;
                        MediaPlayerCUViewController.setTypeCU(2);
                        newInfoWindow(selectedMovie.getTitle());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sliderProgressMovie.setValue(0);
                    isVideoPaused = false;
                    //PlayMovie(selectedMovie);

                }
            }
        });
    }

    private void clearSelectionForCategorySelect() { //Clears the selection from both movie tableview and movie in category table view when opening a new category
        tblCategory.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                tblMovies.getSelectionModel().clearSelection();
                tblMoviesInCategory.getSelectionModel().clearSelection();
            }
        });
    }

    public void togglePlayPause() { // This controls the play/Pause functionality of the program when listening ot movies
        if (currentVideo != null) {
            if (currentVideo.getStatus() == MediaPlayer.Status.PLAYING) { //If it was playing we pause it
                currentVideo.pause();
                isVideoPaused = true;
                btnPlayIcon.setImage(playIcon);
            } else { // If it was instead paused, we start playing the movie again
                currentVideo.seek(Duration.seconds(sliderProgressMovie.getValue()));
                currentVideo.play();
                isVideoPaused = false;
                btnPlayIcon.setImage(pauseIcon);
            }
        }
    }

    public static <T> void changeRowColor(TableView<T> tableView, int rowNumber) { // This method changes the color of the row where the playing movie is located
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                // Reset the style for all rows
                setStyle("");

                // Set the style for the row where the playing movie is located
                if (!empty && getIndex() == rowNumber) {
                    setStyle(playingGraphic);
                }
            }
        });
        tableView.refresh();
    }

    private void handlePlayingMovieColor() { // Handles colouring the current playing movie in the movie tableview
        if (currentCategoryPlaying == null && currentMoviePlaying != null) {
            changeRowColor(tblMovies, currentIndex);
        } else if (currentCategory == currentCategoryPlaying) {
            changeRowColor(tblMovies, -1);
        }
    }

    private void handleNewMovie(MediaPlayer newMovie, Movie selectedMovie) {
        if (currentVideo != null) {
            currentVideo.stop();
        }

        onStartMovieBtnClick();
        mediaView.setMediaPlayer(newMovie);

        currentMoviePlaying = selectedMovie;
        sliderProgressMovie.setDisable(false);
        currentVideo = newMovie;

        sliderProgressMovie.setMax(newMovie.getTotalDuration().toSeconds()); //Set our progress to the time so, we know maximum value
        lblPlayingNow.setText("Now playing: " + selectedMovie.getTitle() + " - " + selectedMovie.getDirector());
        currentVideo.seek(Duration.ZERO); //When you start a movie again it should start from start
        currentVideo.setVolume((sliderProgressVolume.getValue())); //We set the volume
        handlePlayingMovieColor();
        tblMoviesInCategory.refresh(); //So the movie in movie category get its color
        tblCategory.refresh(); //So the category in category get its color
        currentVideo.setRate(currentSpeed);

        // Play or pause based on the isVideoPaused flag
        if (isVideoPaused) {
            currentVideo.pause();
            btnPlayIcon.setImage(playIcon);
        } else {
            currentVideo.play();
            btnPlayIcon.setImage(pauseIcon);
        }
        currentVideo.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Update the slider value as the movie progresses
            if (!isUserChangingSlider) {
                sliderProgressMovie.setValue(newValue.toSeconds());
            }
        });
        //Do these when movie is finished
        currentVideo.setOnEndOfMedia(this::onEndOfMovie);
    }

    public void PlayMovie(Movie movie) {
        addMoviesToSoundMap(movie).thenRun(() -> {
            MediaPlayer newVideo = soundMap.get(movie.getId());
            if (newVideo == null) {// Checks if the selected video will work or if it should throw this error instead
                soundMap.put(movie.getId(), new MediaPlayer(new Media(new File("resources/Sounds/missingFileErrorSound.mp3").toURI().toString())));
                newVideo = soundMap.get(movie.getId());
                sliderProgressMovie.setValue(0);
            }
            handleNewMovie(newVideo, movie);
        });
    }

    private void handleMovieSwitch(int newIndex) {
        if (shuffleMode == 1) { //If shuffle is enable, the category will be played in a random order
            shuffleMode();
            return;
        }
        if (repeatMode == 0 && currentMovieList != MovieModel.getObservableMovies()) {//If repeat is disable do it
            if (repeatModeDisable()) {
                return;
            }
        }
        if (!currentMovieList.isEmpty()) {
            currentIndex = newIndex % currentMovieList.size();
            Movie switchedMovie = currentMovieList.get(currentIndex);
            PlayMovie(switchedMovie);
        }
    }

    public void onEndOfMovie(){
        if (repeatMode == 2) {//Repeat 1
            handleNewMovie(currentVideo, currentMoviePlaying);
            return;
        }
        if (shuffleMode == 1) { // If enabled, the shuffle mode will play a random movie from the selected table view, not including the category table view
            shuffleMode();
            return;
        }


        currentVideo = null;
        sliderProgressMovie.setValue(0);
        lblPlayingNow.setText("No movie playing");
        sliderProgressMovie.setDisable(true);
        updateMovieProgressTimer();
        btnPlayIcon.setImage(playIcon);
        onEndMovieBtnClick();
        if (repeatMode != -1) { // If enabled, the shuffle mode will play a random movie from the selected table view, not including the category table view
            handleMovieSwitch(currentIndex + 1); //Moves the user to the next movie in the table view index
        }
    }

    private void onEndMovieBtnClick() {
        hBoxMediaPlayer.setVisible(false);
        vboxTblBtn.setVisible(true);
        hBoxFilter.setVisible(true);
        mediaViewBox.setVisible(false);
        mediaView.setMediaPlayer(null);
        anchorPane.setStyle("");
        AnchorPane.setBottomAnchor(vboxTblBtn, 5.0);
        isVideoModeActive = true;
    }

    private void onStartMovieBtnClick() {
        vboxTblBtn.setVisible(false);
        hBoxMediaPlayer.setVisible(true);
        hBoxFilter.setVisible(false);
        AnchorPane.setBottomAnchor(vboxTblBtn, 117.0);
        mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
        mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
        mediaViewBox.setVisible(true);
        anchorPane.setStyle("-fx-background-color: #454b4f;");
        isVideoModeActive = false;
    }


    //********************************REPEAT*SHUFFLE*FUNCTION**************************************************
    public boolean repeatModeDisable() { // Method to run when repeat is not enabled
        if (currentCategoryPlaying != null) {
            Category nextCategoryToGoTo = null;
            // When user is on the first movie and go backwards we need to take the category before
            if (previousPress && currentIndex == 0) {
                Optional<Category> optionalNextCategory = CategoryModel.getObservableCategories().stream()
                        .filter(p -> p.getId() < currentCategoryPlaying.getId())
                        .max(Comparator.comparing(Category::getId));

                nextCategoryToGoTo = optionalNextCategory.orElse(CategoryModel.getObservableCategories().getLast());
                CategoryModel.getObservableCategories().stream().close();
                // When user is on the last movie and go forward we need to take the next category
            } else if (currentMovieList.indexOf(currentMoviePlaying) + 1 == currentMovieList.size() && !previousPress) {
                Optional<Category> optionalNextCategory = CategoryModel.getObservableCategories().stream()
                        .filter(p -> p.getId() > currentCategoryPlaying.getId())
                        .min(Comparator.comparing(Category::getId));
                nextCategoryToGoTo = optionalNextCategory.orElse(CategoryModel.getObservableCategories().getFirst());
                CategoryModel.getObservableCategories().stream().close();
            }

            if (nextCategoryToGoTo != null) {
                try { //In these lines we set some variable depend on the info from new category
                    categoryMovieModel.categoryMovies(nextCategoryToGoTo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                currentCategoryPlaying = nextCategoryToGoTo;
                currentCategory = nextCategoryToGoTo;
                currentMovieList = categoryMovieModel.getObservableCategoriesMovie();
                currentIndex = 0;

                if (!currentMovieList.isEmpty()) { // We take the next category to go have movies inside
                    // If there is movies we play the right one if no movie we start this method again to check a new category
                    if (previousPress) {
                        currentIndex = currentMovieList.size() - 1;
                        PlayMovie(currentMovieList.getLast());
                        return true;
                    }
                    PlayMovie(currentMovieList.getFirst());
                    return true;
                }
                return repeatModeDisable();
            }
            return false;
        }
        return false;
    }

    public void shuffleMode() {
        if (repeatMode == 0 && currentMovieList != MovieModel.getObservableMovies()) { // Get a list of categories with movies inside itself
            List<Category> nonEmptyCategories = CategoryModel.getObservableCategories().stream()
                    .filter(category -> category.getMovieCount() >= 1).toList();

            if (!nonEmptyCategories.isEmpty()) {
                Random random = new Random(); //Select a random category from categories
                currentCategory = nonEmptyCategories.get(random.nextInt(nonEmptyCategories.size()));
                currentCategoryPlaying = currentCategory;
                try {
                    categoryMovieModel.categoryMovies(currentCategory);
                    currentMovieList = categoryMovieModel.getObservableCategoriesMovie();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        currentIndex = getRandomMovie(); //Select random movie from the current movie list and play it
        currentIndex = (currentIndex) % currentMovieList.size();
        Movie randomMovie = currentMovieList.get(currentIndex);
        PlayMovie(randomMovie);
    }

//*****************************************CREATE*UPDATE*DELETE********************************************

    public void updateMoviePathSoundMap(Movie currentSelectedMovie) { //We remove old path and add new one
        soundMap.remove(currentSelectedMovie.getId());
        soundMap.put(currentSelectedMovie.getId(), new MediaPlayer(new Media(new File(currentSelectedMovie.getMoviePath()).toURI().toString()))); //We add new movie to the hashmap
    }

    public void addMovieToSoundMap(Movie newCreatedMovie) { //We add the movie to our hashmap, so it can be played
        soundMap.put(newCreatedMovie.getId(), new MediaPlayer(new Media(new File(newCreatedMovie.getMoviePath()).toURI().toString())));
    }

    public void handleDelete() { // Handles the delete functionality of the table views
        Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();
        Category selectedCategory = tblCategory.getSelectionModel().getSelectedItem();
        Movie selectedMovieInCategory = tblMoviesInCategory.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        // This sets up the schematic for deleting categories & movies

        if (selectedMovie != null & selectedMovieInCategory == null) {
            alert.setTitle("Movie");
            alert.setHeaderText("You want to delete " + currentMovie.getTitle());
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            // Sets up relevant information for knowing which movie you are trying to delete
            if (result.isPresent() && result.get() == okButton) {
                try {
                    for (Category c : CategoryModel.getObservableCategories()) { // This will check through each category and delete the movie from there since the movieId is a key in the DB
                        categoryMovieModel.deleteMovieFromCategory(selectedMovie, c);
                    }
                    movieModel.deleteMovie(selectedMovie); /// removes movie from database
                    refreshCategories(); // Refreshes the categories so the correct time and count is shown
                    refreshMovieList(); // Refreshes the movie list so the deleted movie is no longer there.
                } catch (Exception e) {
                    displayErrorModel.displayError(e);
                    e.printStackTrace();
                }
            }
            return;
        }
        if (selectedCategory != null & selectedMovieInCategory == null) {
            alert.setTitle("Category");
            alert.setHeaderText("You want to delete " + currentCategory.getCategoryName());
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            // Sets up relevant information for knowing which category you are trying to delete
            if (result.isPresent() && result.get() == okButton) {
                try {
                    categoryModel.deleteCategory(selectedCategory); // deletes the category from the database
                    tblCategory.refresh(); // refreshes category table view to no longer show the deleted one.
                } catch (Exception e) {
                    displayErrorModel.displayError(e);
                    e.printStackTrace();
                }
            }
            return;
        }
        if (selectedMovieInCategory != null) { // Deletes a movie from a category, will not show a warning since nothing permanent is done here, you can always re add the movie
            try {
                currentCategory.setMovieCount(currentCategory.getMovieCount() - 1);
                currentCategory.setMovieTotalTime(currentCategory.getMovieTotalTime() - selectedMovieInCategory.getMovieLength());
                categoryMovieModel.deleteMovieFromCategory(selectedMovieInCategory, selectedCategory);
                refreshCategories();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    //*****************************************WINDOWS********************************************
    private Stage stage;

    public Stage getUpdateStage() {
        return stage;
    }
    public void newUCWindow(String windowTitle) throws IOException { // Creates the second window that will allow you to update and create new movies
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerCU.fxml"));
        Parent root = loader.load();
        stage = new Stage();
        stage.getIcons().add(mainIcon);
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the first window until second is close
        // Add event handler to handle the close request, so it update correct
        stage.setOnCloseRequest(event -> {
            try {
                System.out.println("Refresh");
                refreshMovieList();
                refreshCategories();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        stage.show();
    }

    public void newInfoWindow(String windowTitle) throws IOException { // Creates the second window that will allow you to update and create new movies
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerInfo.fxml"));
        Parent root = loader.load();
        stage = new Stage();
        stage.getIcons().add(mainIcon);
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the first window until second is close
        stage.show();
    }

    public void warningWindow(String windowTitle) throws IOException { // Creates the second window that will allow you to update and create new movies
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MediaPlayerPopUp.fxml"));
        Parent root = loader.load();
        stage = new Stage();
        stage.getIcons().add(mainIcon);
        stage.setTitle(windowTitle);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL); //Lock the first window until second is close
    }

//******************************************HELPER*METHOD********************************************
    public void setSpeedMovie(int value) { // Method to change what speed we use
        if (currentSpeedIndex == speeds.size())
            currentSpeedIndex = -1;
        if (currentSpeedIndex == 0) {
            currentSpeedIndex = speeds.size();
        }
        currentSpeedIndex = (currentSpeedIndex + value) % speeds.size();
        currentSpeed = speeds.get(currentSpeedIndex);
        btnSpeed.setText(currentSpeed + "x");

        // Set the media player's playback speed to the new speed
        if (currentVideo != null) {
            currentVideo.setRate(currentSpeed);
        }
    }

    public void refreshCategories() throws Exception { // Refreshes the category and movies in category table views.
        if (currentCategory == null) {
            currentCategory = CategoryModel.getObservableCategories().getFirst();
        }
        categoryMovieModel.categoryMovies(currentCategory);
        tblCategory.getSelectionModel().select(currentCategory);
        tblMoviesInCategory.setItems(categoryMovieModel.getObservableCategoriesMovie());
        tblCategory.setItems(CategoryModel.getObservableCategories());
        tblCategory.refresh();
        tblMoviesInCategory.refresh();
    }

    public void refreshMovieList() throws Exception { // Refreshes the movie list by clearing all items and reinserting them
        tblMovies.getItems().clear();
        tblMovies.setItems(movieModel.updateMovieList());
        tblMovies.refresh();
    }

    public int getRandomMovie() { // Fetches a random movie from the currently selected movie list, which is either the movie list or movie in category
        int min = 0;
        int max = currentMovieList.size();
        int range = max - min;
        return (int) (Math.random() * range) + min;
    }

    public void seekCurrentVideo10Plus() { // Goes 10 seconds forwards in the currently playing movie
        if (currentVideo != null) {
            currentVideo.seek(Duration.seconds(sliderProgressMovie.getValue() + 10));
        }
    }

    public void seekCurrentVideo10Minus() { // Goes 10 seconds backwards in the currently playing movie
        if (currentVideo != null) {
            currentVideo.seek(Duration.seconds(sliderProgressMovie.getValue() - 10));
        }
    }

//******************************************DRAG*DROP************************************************
    @FXML
    private void initializeDragAndDrop() { // Sets up the drag & drop functionality for the entire program
        tblMovies.setOnDragDetected(event -> { //When user drag a movie from movie list
            Movie selectedMovie = tblMovies.getSelectionModel().getSelectedItem();
            currentTableview = "tblMovies";
            if (selectedMovie != null) {
                Dragboard db = tblMovies.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, Integer.toString(selectedMovie.getId()));
                db.setContent(content);
                event.consume();
            }
        });

        tblCategory.setOnDragOver(event -> { // Allowing drop on category only if the source not is tblMoviesInCategory and has a string
            if (event.getGestureSource() != tblMoviesInCategory && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tblMoviesInCategory.setOnDragOver(event -> { // Allowing drop on category movie list only if the source not is tblMoviesInCategory and has a string
            if (event.getGestureSource() != tblMoviesInCategory && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        anchorPane.setOnDragOver(event -> { // Allowing drop on anchor pane only if the source is tblMoviesInCategory and has a string
            if (Objects.equals(currentTableview, "tblMoviesInCategory")) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tblCategory.setRowFactory(tv -> new TableRow<>() {
            @Override
            public void updateItem(Category category, boolean empty) { // Adds the currently playing functionality to the tblCategory
                super.updateItem(category, empty);
                if (category != null) {
                    if (category.equals(currentCategoryPlaying) && currentCategoryPlaying == currentCategory) {
                        setStyle(playingGraphic);
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }

                setOnMouseClicked(event -> { // Adds dynamic functionality ofr the tblMoviesInCategory, so it gets hidden when no category is selected
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (getIndex() >= tblCategory.getItems().size()) {
                            tblMoviesInCategory.getItems().clear();
                            tblMoviesInCategoryVBOX.setManaged(false);
                            tblMoviesInCategoryVBOX.setVisible(false);

                        } else {
                            try { // Makes the tblMoviesInCategory visible when a category is selected
                                currentCategory = getItem();
                                tblMoviesInCategoryVBOX.setManaged(true);
                                tblMoviesInCategoryVBOX.setVisible(true);
                                refreshCategories();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (event.getButton() == MouseButton.SECONDARY) { // Adds the context menu functionality to the tblCategory
                        contextMenuCategory.getItems().clear();
                        currentCategory = getItem();
                        if (getIndex() >= tblCategory.getItems().size()) {
                            contextMenuCategory.getItems().addAll(createCategory);
                        } else {
                            contextMenuCategory.getItems().addAll(createCategory, updateCategory, deleteCategory, deleteAllMovies);
                        }
                    }
                });
                setOnDragDropped(event -> { // Allows you to drag a movie from tblMovie onto the tblCategory to add it to the category
                    Dragboard db = event.getDragboard();
                    boolean success = false;

                    if (db.hasString()) {
                        int movieId = Integer.parseInt(db.getString());
                        Movie selectedMovie = movieModel.getMovieById(movieId);

                        // Access the data associated with the target row
                        currentCategory = getItem();

                        try {
                            refreshCategories();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        if (selectedMovie != null) {
                            try {
                                if (categoryMovieModel.addMovieToCategory(selectedMovie, currentCategory)) { //We first need to make sure it not already in the category
                                    tblCategory.getSelectionModel().select(currentCategory);
                                    categoryMovieModel.categoryMovies(tblCategory.getSelectionModel().getSelectedItem());
                                    refreshCategories();
                                    success = true;

                                } else {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Category System");
                                    alert.setHeaderText("The movie is already in the category");
                                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                    stage.getIcons().add(mainIcon);
                                    alert.showAndWait();
                                }
                            } catch (Exception e) {
                                displayErrorModel.displayError(e);
                                e.printStackTrace();
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();

                });
            }
        });

        tblMoviesInCategory.setOnDragDropped(event -> { //When user drop a movie from movie list into category movie
            Dragboard db = event.getDragboard();
            boolean success = false;


            if (db.hasString() && Objects.equals(currentTableview, "tblMovies")) {
                int movieId = Integer.parseInt(db.getString());
                Movie selectedMovie = movieModel.getMovieById(movieId);

                if (selectedMovie != null) {
                    try { // We check is everything is what it should be display error if all its fine we add the movie
                        if (currentCategory == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Category System");
                            alert.setHeaderText("You need to choose a category");
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                            alert.showAndWait();
                        } else if (categoryMovieModel.addMovieToCategory(selectedMovie, currentCategory)) { //We first need to make sure it not already in the category
                            categoryMovieModel.categoryMovies(tblCategory.getSelectionModel().getSelectedItem());

                            tblCategory.refresh();
                            success = true;
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Category System");
                            alert.setHeaderText("The movie is already in the category");
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(mainIcon);
                            alert.showAndWait();
                        }
                    } catch (Exception e) {
                        displayErrorModel.displayError(e);
                        e.printStackTrace();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        anchorPane.setOnDragDropped(event -> { // When user drop a movie from category movie list into background
            if (draggedMovie != null) {
                currentCategory.setMovieCount(currentCategory.getMovieCount() - 1);
                currentCategory.setMovieTotalTime(currentCategory.getMovieTotalTime() - draggedMovie.getMovieLength());
                tblCategory.refresh(); // We remove in the category and update category movie count and total time
                try {
                    categoryMovieModel.deleteMovieFromCategory(draggedMovie, currentCategory);
                    draggedMovie = null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            anchorPane.requestFocus();
            event.setDropCompleted(true);
            event.consume();
        });

        tblMoviesInCategory.setRowFactory(tv -> new TableRow<>() {
            @Override
            public void updateItem(Movie movie, boolean empty) { // Enables the current movie playing colour function in the movies in category
                super.updateItem(movie, empty);
                if (movie != null) {
                    if (movie.equals(currentMoviePlaying) && currentCategoryPlaying == currentCategory) {
                        setStyle(playingGraphic);
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }


                // Handle mouse events here
                setOnMouseClicked(event -> { // Enables the dynamic context menu functionality
                    tblMoviesInCategory.refresh();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        contextMenuMoviesInCategory.getItems().clear();
                        currentMovie = getItem();
                        if (getIndex() >= getTableView().getItems().size()) {
                            contextMenuMoviesInCategory.getItems().clear();
                        } else {
                            contextMenuMoviesInCategory.getItems().addAll(deleteMovieInCategory);
                        }
                    }
                });

                setOnDragDetected(event -> { // Starts the drag function in the tblMoviesInCategory
                    if (event.getButton() == MouseButton.PRIMARY && !isEmpty()) {
                        draggedMovie = getTableView().getSelectionModel().getSelectedItem();
                        currentTableview = "tblMoviesInCategory";
                        int index = getIndex();
                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(Integer.toString(index));
                        db.setContent(cc);
                        event.consume();
                    }
                });

                setOnDragOver(event -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        int draggedIndex = Integer.parseInt(db.getString());
                        if (isEmpty() || getIndex() != draggedIndex) {
                            event.acceptTransferModes(TransferMode.MOVE);
                            event.consume();
                        }
                    }
                });

                setOnDragDropped(event -> {                     // Ensures you cant put movies from tblMovieInCategory back into the tblMovie,
                    if (!currentTableview.equals("tblMovies")) { // but only drop them on empty space to delete them or move them around the category itself
                        Dragboard db = event.getDragboard();
                        if (db.hasString()) {
                            int draggedIndex = Integer.parseInt(db.getString());
                            int dropIndex = isEmpty() ? getTableView().getItems().size() : getIndex();

                            if (dropIndex == getTableView().getItems().size()) { //Make sure you can not drop a movie to an empty spot
                                event.setDropCompleted(false);
                                event.consume();
                                return;
                            }
                            if (dropIndex != draggedIndex) { // Perform the reordering in your data model
                                moveMovieInCategory(draggedIndex, dropIndex);
                                event.setDropCompleted(true);
                                getTableView().getSelectionModel().select(dropIndex);
                                event.consume();
                            }
                        }
                    }
                });
            }
        });
        tblMoviesInCategory.refresh();
    }

    public void moveMovieInCategory(int fromIndex, int toIndex) { // Method for moving movies around the category to change the play order
        Movie selectedMovie = tblMoviesInCategory.getItems().get(fromIndex);  // Get the selected movie
        Movie oldMovie = categoryMovieModel.getObservableCategoriesMovie().get(toIndex);  // Get movie there was before

        if (toIndex == tblMoviesInCategory.getItems().size() - 1) {
            tblMoviesInCategory.getItems().add(tblMoviesInCategory.getItems().size(), selectedMovie);
            selectedMovie = tblMoviesInCategory.getItems().remove(fromIndex);
        } else if (toIndex != tblMoviesInCategory.getItems().size() - 1) {
            selectedMovie = tblMoviesInCategory.getItems().remove(fromIndex);
            tblMoviesInCategory.getItems().add(toIndex, selectedMovie);
        }
        try {
            categoryMovieModel.updateMovieInCategory(selectedMovie, oldMovie, currentCategory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //*******************************************KEYBOARD**************************************************
    Set<KeyCode> pressedKeys = new HashSet<>();

    @FXML
    private void keyboardKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    @FXML
    private void keyboardKeyPressed(KeyEvent event) throws Exception { // Controls keyboard Functionality for the window

        KeyCode keyCode = event.getCode(); //Get the button press value
        pressedKeys.add(event.getCode());
        if (event.getCode() == KeyCode.SPACE) { // Tries to pause the currently playing movie or start the playing movie if it was paused
            event.consume();
            tblMovies.getSelectionModel().clearSelection();
            tblCategory.getSelectionModel().clearSelection();
            tblMoviesInCategory.getSelectionModel().clearSelection();
            btnPlay.requestFocus();
            togglePlayPause();
        }

        if (keyCode == KeyCode.ESCAPE) { // Clears all selections when escape is pressed
            tblMovies.getSelectionModel().clearSelection();
            tblCategory.getSelectionModel().clearSelection();
            tblMoviesInCategory.getSelectionModel().clearSelection();
            anchorPane.requestFocus();
        }

        if (event.isControlDown()) { // Checks if control key is held down
            if (keyCode == KeyCode.LEFT) { // Tries to move 10 seconds backwards in the currently playing movie
                sliderProgressMovie.requestFocus();
                seekCurrentVideo10Minus();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.RIGHT) { // Tries to move 10 seconds forwards in the currently playing movie
                sliderProgressMovie.requestFocus();
                seekCurrentVideo10Plus();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.S) { // Opens the create movie window
                setSpeedMovie(1);
            }
        }

        if (event.isShiftDown()) {
            if (keyCode == KeyCode.S) { // Opens the create movie window
                setSpeedMovie(-1);
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.H) { // Enable/Disable shuffle movie function
                btnShuffleMovie();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.T) { // Enable/Disable repeat movie function
                btnRepeatMovie();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.B) { // Go to previously played movie
                btnBackwardMovie();
            }
        }
        if (event.isControlDown()) {
            if (keyCode == KeyCode.F) { // Go to next movie to be played
                btnForwardMovie();
            }
        }

        if (!isVideoModeActive) { //Disable key under media
            if (keyCode == KeyCode.DELETE) { // Tries to delete the selected movie, category or movie in category. Will still show relevant warning screen
                handleDelete();
            }

            if (event.isControlDown()) {
                if (keyCode == KeyCode.P) { // Opens the create new category dialog box
                    btnCreateCategoryNow();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.O) { // Tries to update the currently selected category
                    btnUpdateCategoryNow();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.C) { // Opens the create movie window
                    btnNewWindowCreate();
                }
            }
            if (event.isControlDown()) {
                if (keyCode == KeyCode.U) { // Tries to open the update movie window with information from the currently selected movie
                    btnNewWindowUpdate();
                }
            }
        }
    }
    //******************************************BUTTONS*SLIDERS************************************************
    public boolean createUpdateCategory(String buttonText) throws Exception { // Method for updating or creating categories
        TextInputDialog dialog = new TextInputDialog("");
        if (currentCategory == null && buttonText.equals(btnUpdateCategory.getText())) { // Checks if a valid category was selected when trying to update
            displayErrorModel.displayErrorC("You forgot choose a category");
            return false;
        }

        if (buttonText.equals(btnCreateCategory.getText())) { // If you clicked create it will open a dialog box and ask you for a name
            dialog.setTitle("New Category");
            dialog.setHeaderText("What do you want to call your new Category");
        }
        if (buttonText.equals(btnUpdateCategory.getText())) { // If you clicked update it will open a dialog box and ask you what the selected category should be called instead
            dialog.setTitle("Update Category " + currentCategory.getCategoryName());
            dialog.setHeaderText("What would you like to rename the Category");
        }

        // Set the icon for the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String inputValue = result.get().strip(); // Get the actual value from Optional and ensure that all white space is removed in front and behind the text input

            if (buttonText.equals(btnCreateCategory.getText()) && !inputValue.isEmpty()) {
                Category p = new Category(-1, inputValue, 0);
                if (!categoryModel.createNewCategory(p))    {
                    displayErrorModel.displayErrorC("This category already exist in the system");
                }
            }
            if (buttonText.equals(btnUpdateCategory.getText()) && !inputValue.isEmpty()) {
                currentCategory.setCategoryName(inputValue);
                categoryModel.updateCategory(currentCategory);
            }
            if (inputValue.isEmpty()) {
                displayErrorModel.displayErrorC("You need to input a valid name for your category");
            }
        }
        refreshCategories();
        tblCategory.getSortOrder().clear();
        colCategoryName.setSortType(TableColumn.SortType.ASCENDING);
        tblCategory.getSortOrder().add(colCategoryName);
        return true;
    }

    public void btnCreateCategoryNow() throws Exception { // Functionality for context menu
        createUpdateCategory(btnCreateCategory.getText());
    }


    public void btnUpdateCategoryNow() throws Exception { // Functionality for context menu
        createUpdateCategory(btnUpdateCategory.getText());
    }

    public void btnNewWindowCreate() throws IOException { // Opens the CU FXML window to create a new movie
        MediaPlayerCUViewController.setTypeCU(1);
        newUCWindow("Movie Creator");
    }

    public void btnNewWindowUpdate() throws IOException { // Opens the CU FXML window to update a new movie, attempts to get all relevant information to input into new window
        MediaPlayerCUViewController.setTypeCU(2);
        if (currentMovie == null) {
            btnNewWindowCreate();
            return;
        } //If user want to update but forgot a movie they can create a new one instead
        currentMovie = tblMovies.getSelectionModel().getSelectedItem();
        newUCWindow("Movie Updater");
    }

    public void btnDelete() { // Calls the delete function to try and delete the selected movie, category or movie in category with relevant warnings where applicable
        handleDelete();
    }

    public void btnShuffleMovie() { // Changes the shuffle mode and shuffle icon when clicked
        if (btnShuffleIcon.getImage().equals(shuffleIcon)) {
            btnShuffleIcon.setImage(shuffleIconDisable);
            shuffleMode = 0; //Execute shuffle disable method
        } else if (btnShuffleIcon.getImage().equals(shuffleIconDisable)) {
            btnShuffleIcon.setImage(shuffleIcon);
            shuffleMode = 1; //Execute shuffle method
        }
    }

    public void btnBackwardMovie() { // Moves backwards a movie in the currently selected table view
        previousPress = true;
        handleMovieSwitch(currentIndex - 1 + currentMovieList.size());
    }

    public void btnPlayMovie() {
        handleMoviePlay();
    }

    public void btnForwardMovie() { // Moves forward a movie in the currently selected table view
        previousPress = false;
        handleMovieSwitch(currentIndex + 1);
    }

    public void btnGoBack() {
        if (currentVideo != null) {
            repeatMode = -1;
            currentVideo.seek(Duration.millis(1000000000)); //So its 100% is done
        }
        btnRepeatMovie();
        onEndMovieBtnClick();
    }

    public void btnAFilterIMDBArrow() {
        if (Objects.equals(btnFilterIMDBArrow.getText(), "⯆")) {
            filterIMDBArrow = "U";
            btnFilterIMDBArrow.setText("⯅");
        }
        else {
            filterIMDBArrow = "D";
            btnFilterIMDBArrow.setText("⯆");
        }
        try {tblMovies.setItems(movieModel.updateMovieListFilter());}
        catch (Exception ex) { throw new RuntimeException(ex);}
    }

    public void btnRepeatMovie() { // Enables or disables the repeat mode and sets the icon to the relevant one
        if (btnRepeatIcon.getImage().equals(repeat1Icon)) {
            btnRepeatIcon.setImage(repeatDisableIcon);
            repeatMode = 0;     //Change repeat mode to disabled so system know
        } else if (btnRepeatIcon.getImage().equals(repeatDisableIcon)) {
            btnRepeatIcon.setImage(repeatIcon);
            repeatMode = 1;  //Change repeat mode to repeat same movie so system know
        } else if (btnRepeatIcon.getImage().equals(repeatIcon)) {
            btnRepeatIcon.setImage(repeat1Icon);
            repeatMode = 2;    //Change repeat mode to repeat category so system know
        }
    }

    public void btnSpeedMovie(MouseEvent event) { // Here we change the movies speed
        if (event.getButton() == MouseButton.SECONDARY) {
            setSpeedMovie(-1);
        }
        else
            setSpeedMovie(1);
    }

    public void onSlideProgressPressed() { // Tries to move the movie progress to the selected duration
        if (currentVideo != null) {
            isUserChangingSlider = true; // This prevents the system from trying to update itself
            currentVideo.seek(Duration.seconds(sliderProgressMovie.getValue()));
            currentVideo.pause();
        }
    }

    public void onSlideProgressReleased() { // Tries to start the movie when the user release the slider at a location
        if (currentVideo != null) {
            if (!isVideoPaused) {
                currentVideo.seek(Duration.seconds(sliderProgressMovie.getValue()));
                currentVideo.play();
                isUserChangingSlider = false; // This prevents the system from trying to update itself
                anchorPane.requestFocus();
            }
        }
    }

    //******************************************STYLING*SLIDERS************************************************
    private void setSliderVolumeStyle() { // Sets the CSS for the volume slider
        double percentage = sliderProgressVolume.getValue() / (sliderProgressVolume.getMax() - sliderProgressVolume.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #038878 0%%, #038878 %.2f%%, " +
                "#92dc9b %.2f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressVolume.lookup(".track").setStyle(color);
    }

    private void setSliderMovieProgressStyle() { // Sets the css for the movie progress slider
        double percentage = sliderProgressMovie.getValue() / (sliderProgressMovie.getMax() - sliderProgressMovie.getMin());
        String color = String.format(Locale.US, "-fx-background-color: linear-gradient(to right, #04a650 0%%, #04a650 %.10f%%, " +
                "#92dc9b %.10f%%, #92dc9b 100%%);", percentage * 100, percentage * 100);
        sliderProgressMovie.lookup(".track").setStyle(color);
    }

}