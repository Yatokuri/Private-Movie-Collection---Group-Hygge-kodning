/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class MediaPlayerCUViewController implements Initializable {

    @FXML
    public ListView<Category> lstCategory;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    private TextField txtInputAPI, txtInputName, txtInputDirector, txtInputYear, txtInputFilepath, txtInputTime, txtInputCategories, txtInputIMDBRating, txtInputPersonalRating;
    @FXML
    private Button btnSave;
    private MediaPlayerViewController mediaPlayerViewController;
    private long currentMovieLength;
    private MovieModel movieModel;
    private CategoryModel categoryModel;
    private CategoryMovieModel categoryMovieModel;
    private DisplayErrorModel displayErrorModel;
    private final ValidateModel validateModel = new ValidateModel();
    private final BooleanProperty isNameValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isArtistValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isFilepathValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isYearValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isTimeValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isMyRateValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isIMDBRateValid = new SimpleBooleanProperty(true);
    private static int typeCU = 0;
    private static Movie currentSelectedMovie = null;
    private static MediaPlayerCUViewController instance;

    private List<Category> categoryNames = new ArrayList<>();
    private final List<Category> categoryNamesOld = new ArrayList<>();

    private static String TMDBAPI_KEY, OMDBAPI_KEY;
    private String posterPath, imdbId, movieDescription = "N/A";

    private static final String configFile = "config/config.settings";

    public static void setTypeCU(int typeCU) {MediaPlayerCUViewController.typeCU = typeCU;}

    public MediaPlayerCUViewController() {
        try {
            Properties APIProperties = new Properties();
            APIProperties.load(new FileInputStream((configFile)));
            TMDBAPI_KEY = (APIProperties.getProperty("TMDBAPI"));
            OMDBAPI_KEY = (APIProperties.getProperty("OMDBAPI"));

            if (typeCU != 0) {
                movieModel = new MovieModel();
                categoryModel = new CategoryModel();
                displayErrorModel = new DisplayErrorModel();
                categoryMovieModel = new CategoryMovieModel();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MediaPlayerCUViewController getInstance() {
        if (instance == null) {
            instance = new MediaPlayerCUViewController();
        }
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            mediaPlayerViewController = MediaPlayerViewController.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentSelectedMovie = mediaPlayerViewController.getCurrentMovie();

        // Add validation listeners for all inputs & starts context system
        addValidationListener(txtInputName, isNameValid);
        addValidationListener(txtInputDirector, isArtistValid);
        addValidationListener(txtInputFilepath, isFilepathValid);
        addValidationListener(txtInputYear, isYearValid);
        addValidationListener(txtInputTime, isTimeValid);
        addValidationListener(txtInputPersonalRating, isMyRateValid);
        addValidationListener(txtInputIMDBRating, isIMDBRateValid);

        startupSetup();
    }

    private void findInformationForMovie(String filmTitle) {
        try {

            filmTitle = filmTitle.substring(filmTitle.indexOf("org/") + 4, filmTitle.indexOf("-", filmTitle.indexOf("org/") + 4));

            categoryNames.clear();
            // Setup for the TMDB API to get the credits and the actual movie information
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/" + filmTitle + "?api_key=" + TMDBAPI_KEY + "&append_to_response=external_id"))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            HttpRequest requestCrew = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/" + filmTitle + "/credits?api_key=" + TMDBAPI_KEY))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> responseCrew = HttpClient.newHttpClient().send(requestCrew, HttpResponse.BodyHandlers.ofString());

            // Inserts the results from the HttpRequests into Json objects
            JSONObject jsonCrew = new JSONObject(responseCrew.body());
            JSONObject json = new JSONObject(response.body());

            // Define strings outside the if statements
            String titleString= "N/A";
            String yearString = "N/A";
            String directorName = "N/A";

            if (json.has("genres") && json.has("original_title")) { //We look for Genre in the json cause that what we need know in comma separated string
                //Movie
                // Grabbing the relevant data from the json results and inserting them into the strings to later be inserted into the movie object
                titleString = json.getString("original_title");
                imdbId = json.getString("imdb_id");
                posterPath = "https://image.tmdb.org/t/p/original/" + json.getString("poster_path");
                yearString = json.getString("release_date");
                directorName = "N/A";
                yearString = yearString.substring(0, yearString.indexOf("-"));
                movieDescription = json.getString("overview");
                JSONArray crewArray = jsonCrew.getJSONArray("crew");
                JSONArray genresArray = json.getJSONArray("genres");

                for (int i = 0; i < crewArray.length(); i++) {
                    JSONObject crewMember = crewArray.getJSONObject(i);

                    // Check if the crew member has the job title "Director"
                    if ("Director".equals(crewMember.getString("job"))) {
                        directorName = crewMember.getString("name");
                        break;  // Break the loop once the director is found
                    }
                }
                updateGenreFromAPI(genresArray);
            }

            if (json.has("genres") && json.has("original_name")) { //We look for Genre in the json cause that what we need know in comma separated string
                //TV-Series
                titleString = json.getString("original_name");
                imdbId = json.getString("imdb_id");
                posterPath = "https://image.tmdb.org/t/p/original/" +json.getString("poster_path");
                yearString = json.getString("first_air_date");
                directorName = "N/A";
                yearString = yearString.substring(0, yearString.indexOf("-"));
                movieDescription = json.getString("overview");
                JSONArray crew1Array = jsonCrew.getJSONArray("crew");
                JSONArray genres1Array = json.getJSONArray("genres");

                for (int i = 0; i < crew1Array.length(); i++) {
                    JSONObject crewMember = crew1Array.getJSONObject(i);
                    // Check if the crew member has the job title "Director or Producer"
                    if ("Director".equals(crewMember.getString("job")) || "Producer".equals(crewMember.getString("job"))) {
                        directorName = crewMember.getString("name");
                        break;  // Break the loop once the director is found
                    }
                }
                updateGenreFromAPI(genres1Array);
            }
            if (titleString.equals("N/A"))    {
                displayErrorModel.displayErrorC("Error - API could not find movie/Tv Series");
                return;
            }

            OkHttpClient client = new OkHttpClient(); //We make a request to API
            Request requestIMDBRating = new Request.Builder()
                    .url("https://www.omdbapi.com/?i=" + imdbId + "&apikey=" + OMDBAPI_KEY) // We use the imdbId we got from the HTTPRequests above to make a request from the OMDBAPI so we can get IMDBRating
                    .build();
            String responseData;
            try (Response responseIMDBRating = client.newCall(requestIMDBRating).execute()) {
                assert responseIMDBRating.body() != null;
                responseData = responseIMDBRating.body().string();
            } //We got answer

            JSONObject jsonIMDBRating = new JSONObject(responseData);

            String imdbRating = jsonIMDBRating.getString("imdbRating");

            txtInputName.setText(titleString);
            txtInputYear.setText(yearString);
            txtInputCategories.setText(String.valueOf(categoryNames));
            txtInputDirector.setText(directorName);
            txtInputIMDBRating.setText(imdbRating);
            updateListviewSelected();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGenreFromAPI(JSONArray genreArray) throws Exception {
        // We go through all genre and add it to the lists, and if the genre (category) don't exist we create it.
        for (int i = 0; i < genreArray.length(); i++) {
            JSONObject genreObject = genreArray.getJSONObject(i);
            String genre = genreObject.getString("name");
            Category p = new Category(-1, genre, 0);
            categoryNames.add(p);
            if (categoryModel.createNewCategory(p)) {
                lstCategory.getItems().add(p);
            }
        }
    }

    private void updateListviewSelected()  {
        // Automatically select items in lstCategory that match categoryNames
        lstCategory.getItems().clear(); //Should just add new one so select don't get loose
        lstCategory.getItems().addAll(CategoryModel.getObservableCategories());
      for (Category c : categoryNames) { //Find the right instance there is in the list view and select it
            lstCategory.getItems().stream()
                    .filter(category -> category.getCategoryName().equals(c.getCategoryName()))
                    .findFirst().ifPresent(correspondingCategory -> lstCategory.getSelectionModel().select(correspondingCategory));
        }
      lstCategory.getItems().stream().close();
    }

    public void startupSetup() {
        lstCategory.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstCategory.getItems().addAll(CategoryModel.getObservableCategories());
        categorySystem();
        //if (typeCU == 1)
        if (typeCU == 1) { // If TypeCU is 1 we create Movie
            btnSave.setText("Create");
            txtInputPersonalRating.setText("0");
        }
        //if (typeCU == 2 & currentSelectedMovie != null)
        if (typeCU == 2 & currentSelectedMovie != null) { // If TypeCU is 2 we update Movie
            btnSave.setText("Update");
            txtInputName.setText(currentSelectedMovie.getTitle());
            txtInputYear.setText(String.valueOf(currentSelectedMovie.getYear()));
            txtInputDirector.setText(currentSelectedMovie.getDirector());
            txtInputTime.setText(currentSelectedMovie.getMovieLengthHHMMSS());
            txtInputFilepath.setText(currentSelectedMovie.getMoviePath());
            txtInputIMDBRating.setText(String.valueOf(currentSelectedMovie.getMovieRating()));
            txtInputPersonalRating.setText(String.valueOf(currentSelectedMovie.getPersonalRating()));
            imdbId = currentSelectedMovie.getImdbId();
            posterPath = currentSelectedMovie.getPosterPath();
            movieDescription = currentSelectedMovie.getMovieDescription();

            try {
                List<Integer> categoryIds = CategoryMovieModel.getMovieCatList(currentSelectedMovie);
                List<String> categoryNamesTemp = new ArrayList<>();

                for (Integer categoryId : categoryIds) {
                    categoryNamesTemp.add(categoryModel.getCategoryById(categoryId).getCategoryName());
                    categoryNamesOld.add(categoryModel.getCategoryById(categoryId));
                }

                // Automatically select items in lstCategory that match categoryNamesOld
                for (Category categoryNamesOld : categoryNamesOld) {
                    lstCategory.getSelectionModel().select(categoryNamesOld);
                }

                if (categoryNamesTemp.isEmpty()) {
                    txtInputCategories.setPromptText("Missing");
                    return;
                }
                txtInputCategories.setText(String.valueOf(categoryNamesTemp));
                categoryNames = new ArrayList<>(categoryNamesOld); // We let it copy a clone, so they don't pointing to the same underlying list.

                txtInputFilepath.textProperty().addListener((observable, oldValue, newValue) -> {
                    txtInputTime.setText("00:00:00"); // Also mean not valid file
                    if (validateModel.isValidMediaPath(newValue)) {
                        updateTimeText();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void categorySystem() { //We pass the info to ValidateModel class
        lstCategory.setOnMouseClicked(event -> {
            // Get selected items
            var selectedItems = lstCategory.getSelectionModel().getSelectedItems();
            categoryNames.clear();
            categoryNames.addAll(selectedItems);
            if (!categoryNames.isEmpty())
                txtInputCategories.setText(categoryNames.toString());
            else {
                txtInputCategories.clear();
                txtInputCategories.setPromptText("Categories");
            }
        });
    }

    private void addValidationListener(TextField textField, BooleanProperty validationProperty) { //Detect change and validate it
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validateModel.validateInput(textField, newValue);
            validationProperty.set(isValid);
            setBorderStyle(textField, isValid);
        });
    }

    private void updateTimeText() { //We pass the info to ValidateModel class
        MediaPlayer newMovie = new MediaPlayer(new Media(new File(txtInputFilepath.getText()).toURI().toString()));
        validateModel.updateTimeText(newMovie, formattedTime   -> { //This is because we need to wait because setOnReady is an asynchronous operation,
            String[] parts = formattedTime.split("-"); //We need to split the return because we got time in HH:MM:SS and just seconds
            txtInputTime.setText(parts[0]);
            currentMovieLength = Long.parseLong(parts[1]);
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
            if (keyCode == KeyCode.F) { // Opens the file chooser
                btnChooseFile();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.C) { // Tries to open the creates a new category dialog box
                btnMoreCategory();
            }
        }

        if (event.isControlDown()) {
            if (keyCode == KeyCode.S || keyCode == KeyCode.U) { // attempts to save the Movie or update the Movie depending on the window opened
                btnSave();
            }
        }
    }
 //*******************************BUTTONS***********************************************
     public void btnChooseFile() { // Use to choose file - We pass the info to ValidateModel class
         txtInputFilepath.setText(validateModel.btnChoose());  //
         if(!txtInputFilepath.getText().isEmpty())
             updateTimeText();
     }

    public void btnMoreCategory() throws Exception { //Use to add a new category
        if (mediaPlayerViewController.createUpdateCategory("Create Category"))  {
            updateListviewSelected();
            mediaPlayerViewController.refreshCategories();
            categoryNames.add(lstCategory.getItems().getLast()); //Update stuff
            lstCategory.getSelectionModel().selectLast();
            txtInputCategories.setText(String.valueOf(categoryNames));
        }
    }
    public void btnSave() throws Exception { // Validate all inputs before saving
        boolean isNameValid = validateModel.validateInput(txtInputName, txtInputName.getText());
        boolean isArtistValid = validateModel.validateInput(txtInputDirector, txtInputDirector.getText());
        boolean isFilepathValid = validateModel.validateInput(txtInputFilepath, txtInputFilepath.getText());
        boolean isYearValid = validateModel.validateInput(txtInputYear, txtInputYear.getText());
        boolean isTimeValid = validateModel.validateInput(txtInputTime, txtInputTime.getText());
        boolean isIMDBRateValid = validateModel.validateInput(txtInputIMDBRating, txtInputIMDBRating.getText());
        boolean isMyRateValid = validateModel.validateInput(txtInputPersonalRating, txtInputPersonalRating.getText());
        if (isNameValid && isArtistValid && isFilepathValid && isYearValid && isTimeValid && isIMDBRateValid && isMyRateValid) {
            if (typeCU == 1) {
                createNewMovie();
            }
            if (typeCU == 2) {
                updateMovie();
            }
        }
    }

    @FXML
    private void btnAPI() { // The Button that tries to get all relevant info using the API through the link input in the textField
        if (!txtInputAPI.getText().isEmpty() && txtInputAPI.getText().contains("www.themoviedb.org/")) {
            findInformationForMovie(txtInputAPI.getText());
        }
        else {
            displayErrorModel.displayErrorC("Error - Please use TMDB to fetch information E.g \nhttps://www.themoviedb.org/movie/771-home-alone \nhttps://www.themoviedb.org/tv/2287-batman");
        }
    }

    private void createNewMovie() { // Here the movie gets created
        int year = Integer.parseInt(txtInputYear.getText());
        String title = txtInputName.getText();
        String director = txtInputDirector.getText();
        String moviePath = txtInputFilepath.getText();
        double movieRating = Double.parseDouble(txtInputIMDBRating.getText());
        double movieTime = currentMovieLength;
        String category = String.valueOf(txtInputCategories.getText());
        double personalRating = 0;
        String moviePosterPath, movieImdbId, movieTMDBDescription;
        if (posterPath != null && !posterPath.isEmpty()) moviePosterPath = posterPath; // Checks the posterPath to see if there is a poster to save for the info window
        else moviePosterPath = "";
        if (movieDescription != null && !movieDescription.isEmpty()) movieTMDBDescription = movieDescription; // Checks if the API found a description, and if it  did not it sets it to empty
        else movieTMDBDescription = "";
        if (imdbId != null && !imdbId.isEmpty()) movieImdbId = imdbId; // Checks if the IMDBID is not null or empty, so it can possibly be used by the OMDBAPI in other parts of the program
        else movieImdbId = "";
        // Inputs the values from above into a new movie and tries to send it up the layers into the DB, table view and sound map
        Movie movie = new Movie(-1, year, title, director, moviePath, movieRating, movieTime, personalRating, null, category, moviePosterPath, movieImdbId, movieTMDBDescription);

        try {
            Movie newCreatedMovie = movieModel.createNewMovie(movie);
            mediaPlayerViewController.addMovieToSoundMap(newCreatedMovie);

            var selectedItems = lstCategory.getSelectionModel().getSelectedItems();
            categoryNames.clear();
            categoryNames.addAll(selectedItems);
            if (categoryNames != null) {
                for (Category c : categoryNames) { //Add all selected category to the movie
                    categoryMovieModel.addMovieToCategoryBypass(newCreatedMovie, c);
                }
            }
            mediaPlayerViewController.refreshCategories();
            mediaPlayerViewController.refreshMovieList();
            btnCloseWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMovie() throws Exception { //Here the movie gets updated
        if (currentSelectedMovie != null) {
            currentSelectedMovie.setTitle(txtInputName.getText());
            currentSelectedMovie.setDirector(txtInputDirector.getText());
            currentSelectedMovie.setYear(Integer.parseInt(txtInputYear.getText()));
            currentSelectedMovie.setMoviePath(txtInputFilepath.getText());
            currentSelectedMovie.setMovieLength(currentMovieLength);
            currentSelectedMovie.setMovieRating(Double.valueOf(txtInputIMDBRating.getText()));
            currentSelectedMovie.setPersonalRating(Double.parseDouble(txtInputPersonalRating.getText()));
            currentSelectedMovie.setCategory(txtInputCategories.getText());
            currentSelectedMovie.setImdbId(imdbId);
            currentSelectedMovie.setPosterPath(posterPath);
            currentSelectedMovie.setMovieDescription(movieDescription);

            var selectedItems = lstCategory.getSelectionModel().getSelectedItems(); // Gets all selected Categories
            categoryNames.clear(); // Clears the ArrayList, so it can be reused to add movie to each category
            categoryNames.addAll(selectedItems); // Adds the selected items from the listview to the ArrayList

            if (categoryNames != null) {
                for (Category c : categoryNamesOld) {
                    categoryMovieModel.deleteMovieFromCategory(currentSelectedMovie, c); // Deletes movie from all the old categories
                }
                for (Category c : categoryNames) {
                    categoryMovieModel.addMovieToCategoryBypass(currentSelectedMovie, c); // Adds them to all the new ones
                }
            }
            // Updates the movie data and sends it up the layers to the DAL layer and updates the movie path in the sound map in case it got changed
            try {
                movieModel.updateMovie(currentSelectedMovie);
                mediaPlayerViewController.updateMoviePathSoundMap(currentSelectedMovie);
                mediaPlayerViewController.refreshMovieList();
                mediaPlayerViewController.refreshCategories();
                btnCloseWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void btnCloseWindow() { //Close the window
        typeCU = 0; //Reset back
        Stage parent = (Stage) txtInputYear.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
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