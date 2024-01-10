/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Controller;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.GUI.Model.CategoryModel;
import easv.PrivateMovieCollection.GUI.Model.CategoryMovieModel;
import easv.PrivateMovieCollection.GUI.Model.MovieModel;
import easv.PrivateMovieCollection.GUI.Model.ValidateModel;
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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    private TextField txtInputAPI, txtInputName, txtInputDirector, txtInputYear, txtInputFilepath, txtInputTime, txtInputCategories, txtInputIMDBRating, txtInputPersonal;
    @FXML
    private Button btnSave;
    private MediaPlayerViewController mediaPlayerViewController;
    private long currentMovieLength;
    private final MovieModel movieModel;
    private final CategoryModel categoryModel;
    private final CategoryMovieModel categoryMovieModel;
    private final ValidateModel validateModel = new ValidateModel();
    private final BooleanProperty isNameValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isArtistValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isFilepathValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isYearValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isTimeValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isMyRateValid = new SimpleBooleanProperty(true);
    private final BooleanProperty isIMDBRateValid = new SimpleBooleanProperty(true);
    private static final Image mainIcon = new Image ("Icons/mainIcon.png");
    private static int typeCU = 0;
    private static Movie currentSelectedMovie = null;
    private static MediaPlayerCUViewController instance;

    private final List<Category> categoryNames = new ArrayList<>();
    private List<Category> categoryNamesOld;

    private static String API_KEY;
    private static final String configFile = "config/config.settings";

    public static void setTypeCU(int typeCU) {MediaPlayerCUViewController.typeCU = typeCU;}

    public MediaPlayerCUViewController() {
        try {
            Properties APIProperties = new Properties();
            APIProperties.load(new FileInputStream((configFile)));
            API_KEY = (APIProperties.getProperty("API"));
            movieModel = new MovieModel();
            categoryModel = new CategoryModel();
            categoryMovieModel = new CategoryMovieModel();
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
        addValidationListener(txtInputPersonal, isMyRateValid);
        addValidationListener(txtInputIMDBRating, isIMDBRateValid);

        //contextSystem();


        // Add a listener to the filepath input to make sure its valid and update time automatic
        txtInputFilepath.textProperty().addListener((observable, oldValue, newValue) -> {
            txtInputTime.setText("00:00:00"); //Also mean not valid file
            if (validateModel.isValidMediaPath(newValue)) {
                updateTimeText();
            }
        });
        startupSetup();
    }


    @FXML
    private void btnAPI()   {
        if (!txtInputAPI.getText().isEmpty())    {
            findGenreForFilm(txtInputAPI.getText());
        }
    }

    private void findGenreForFilm(String filmTitle) {
        try {
            /*
            OkHttpClient client = new OkHttpClient(); //We make a request to API
            Request request = new Request.Builder()
                    .url("https://www.omdbapi.com/?t=" + filmTitle + "&apikey=" + API_KEY)
                    .build();

            Response response = client.newCall(request).execute(); //We got answer
            assert response.body() != null;
            String responseData = response.body().string();
*/
            filmTitle = filmTitle.substring(filmTitle.indexOf("org") + 4);

            categoryNames.clear();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/" + filmTitle + "?api_key=" + API_KEY + "&append_to_response=external_id"))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            HttpRequest requestCrew = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.themoviedb.org/3/" + filmTitle + "/credits?api_key=" + API_KEY))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> responseCrew = HttpClient.newHttpClient().send(requestCrew, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonCrew = new JSONObject(responseCrew.body());
            JSONObject json = new JSONObject(response.body());
            /*
            System.out.println(json);
            System.out.println(jsonCrew);
             */

            String titleString = "N/A";
            String imdbId = "N/A";
            String posterPath = "N/A";
            String yearString = "N/A";
            String directorName = "N/A";

            if (json.has("genres") && json.has("original_title")) { //We look for Genre in the json cause that what we need know in comma separated string
                //Movie
                titleString = json.getString("original_title");
                imdbId = json.getString("imdb_id");
                posterPath = "https://image.tmdb.org/t/p/original/" + json.getString("poster_path");
                yearString = json.getString("release_date");
                directorName = "N/A";
                yearString = yearString.substring(0, yearString.indexOf("-"));
                JSONArray crewArray = jsonCrew.getJSONArray("crew");
                JSONArray genresArray = json.getJSONArray("genres");

                for (int i = 0; i < crewArray.length(); i++) {
                    JSONObject crewMember = crewArray.getJSONObject(i);

                    // Check if the crew member has the job title "Director"
                    if ("Director".equals(crewMember.getString("job"))) {
                        directorName = crewMember.getString("name");
                        System.out.println("Director: " + directorName);
                        break;  // Break the loop once the director is found
                    }
                }

                for (int i = 0; i < genresArray.length(); i++) {
                    JSONObject genreObject = genresArray.getJSONObject(i);
                    String genre = genreObject.getString("name");
                    Category p = new Category(-1, genre, 0);
                    categoryNames.add(p);
                    lstCategory.getSelectionModel().select(p);
                    if (categoryModel.createNewCategory(p)) {
                        System.out.println("Sejt du skal brugte noget nyt " + p);
                        lstCategory.getItems().add(p);
                        lstCategory.getSelectionModel().select(p);
                    }
                }

            }

            if (json.has("genres") && json.has("original_name")) { //We look for Genre in the json cause that what we need know in comma separated string
                //Movie
                titleString = json.getString("original_name");
                //imdbId = json.getString("imdb_id");
                posterPath = "https://image.tmdb.org/t/p/original/" +json.getString("poster_path");
                yearString = json.getString("first_air_date");
                directorName = "N/A";
                yearString = yearString.substring(0, yearString.indexOf("-"));
                JSONArray crew1Array = jsonCrew.getJSONArray("crew");
                JSONArray genres1Array = json.getJSONArray("genres");

                for (int i = 0; i < crew1Array.length(); i++) {
                    JSONObject crewMember = crew1Array.getJSONObject(i);

                    // Check if the crew member has the job title "Director or Producer"
                    if ("Director".equals(crewMember.getString("job")) || "Producer".equals(crewMember.getString("job"))) {
                        directorName = crewMember.getString("name");
                        System.out.println("Director: " + directorName);
                        break;  // Break the loop once the director is found
                    }
                }

                for (int i = 0; i < genres1Array.length(); i++) {
                    JSONObject genreObject = genres1Array.getJSONObject(i);
                    String genre = genreObject.getString("name");
                    Category p = new Category(-1, genre, 0);
                    categoryNames.add(p);
                    lstCategory.getSelectionModel().select(p);
                    if (categoryModel.createNewCategory(p)) {
                        System.out.println("Sejt du skal brugte noget nyt " + p);
                        lstCategory.getItems().add(p);
                        lstCategory.getSelectionModel().select(p);
                    }
                }

            }

            if (titleString.equals("N/A"))    {
                System.out.println("Error");
                return;
            }

            System.out.println("\nMovie Information:\n" + "Title: " + titleString + " Genre(s): " + categoryNames + "Director" + directorName + " IMDBId: " + imdbId + " Poster Path:" + posterPath + ":");
            txtInputName.setText(titleString);
            txtInputYear.setText(yearString);
            txtInputCategories.setText(String.valueOf(categoryNames));
            txtInputDirector.setText(directorName);

            //txtInputIMDBRating.setText(String.valueOf(currentSelectedMovie.getMovieRating()));

            // Automatically select items in lstCategory that match categoryNames
            /*
            for (Category c : categoryNames) {
                lstCategory.getSelectionModel().select(c);
                System.out.println(categoryNames);
                System.out.println(lstCategory.getSelectionModel().getSelectedItems());
            }
             */
            categorySystem();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startupSetup() {
        lstCategory.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstCategory.getItems().addAll(CategoryModel.getObservableCategories());
        categorySystem();

        if (typeCU == 1) { // If TypeCU is 1 we create Movie
            btnSave.setText("Create");
            txtInputPersonal.setText(String.valueOf("0"));
        }
        if (typeCU == 2 & currentSelectedMovie != null) { // If TypeCU is 2 we update Movie
            btnSave.setText("Update");
            txtInputName.setText(currentSelectedMovie.getTitle());
            txtInputYear.setText(String.valueOf(currentSelectedMovie.getYear()));
            txtInputDirector.setText(currentSelectedMovie.getDirector());
            txtInputFilepath.setText(currentSelectedMovie.getMoviePath());
            txtInputIMDBRating.setText(String.valueOf(currentSelectedMovie.getMovieRating()));
            txtInputPersonal.setText(String.valueOf(currentSelectedMovie.getPersonalRating()));

            try { //Duplicate from another Controller?
                List<Integer> categoryIds = categoryMovieModel.getMovieCatList(currentSelectedMovie);
                List<String> categoryNames = new ArrayList<>();
                categoryNamesOld = new ArrayList<>();

                for (Integer categoryId : categoryIds) {
                    categoryNames.add(categoryModel.getCategoryById(categoryId).getCategoryName());
                    categoryNamesOld.add(categoryModel.getCategoryById(categoryId));
                }

                // Automatically select items in lstCategory that match categoryNamesOld
                for (Category categoryNamesOld : categoryNamesOld) {
                    lstCategory.getSelectionModel().select(categoryNamesOld);
                    lstCategory.refresh();
                    System.out.println("" +lstCategory.getSelectionModel().getSelectedItems());
                }

                if (categoryNames.isEmpty()) {
                    txtInputCategories.setPromptText("Missing");
                    return;
                }
                txtInputCategories.setText(String.valueOf(categoryNames));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void categorySystem() { //We pass the info to ValidateModel class
        lstCategory.setOnMouseClicked(event -> {
            // Get selected items
            var selectedItems = lstCategory.getSelectionModel().getSelectedItems();

            // Print selected items
            System.out.println("Selected Items:");

            for (Category item : selectedItems) {
                System.out.println("- " + item);
                categoryNames.add(item);


            }
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
//*******************************************CONTEXT*MENU**************************************************
    /*
    private void contextSystem() { //Here we create the context menu for the category combo box
        ContextMenu contextMenu = new ContextMenu();
        MenuItem createCategory = new MenuItem("Create Category");
        MenuItem deleteCategory = new MenuItem("Delete Category");
        contextMenu.getItems().addAll(deleteCategory,createCategory);
        comCategory.setContextMenu(contextMenu);

        createCategory.setOnAction((event) -> { // Opens the create category dialog box
            try {
                btnMoreCategory();
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteCategory.setOnAction((event) -> { // deletes the selected category and sends a delete request to the database
            try {
                categoryModel.deleteCategory(comCategory.getSelectionModel().getSelectedItem());
                comCategory.getItems().remove(comCategory.getSelectionModel().getSelectedItem());
                contextMenu.hide();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
     */

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
        //    findGenreForFilm(txtInputName.getText()); //The way to test temp API should be another place

        mediaPlayerViewController.createUpdateCategory("Create Category");
        lstCategory.getItems().clear(); //Should just add new one so select don't get loose
        lstCategory.getItems().addAll(CategoryModel.getObservableCategories());
        mediaPlayerViewController.refreshCategories();
    }
    public void btnSave() throws Exception { // Validate all inputs before saving
        boolean isNameValid = validateModel.validateInput(txtInputName, txtInputName.getText());
        boolean isArtistValid = validateModel.validateInput(txtInputDirector, txtInputDirector.getText());
        boolean isFilepathValid = validateModel.validateInput(txtInputFilepath, txtInputFilepath.getText());
        boolean isYearValid = validateModel.validateInput(txtInputYear, txtInputYear.getText());
        boolean isTimeValid = validateModel.validateInput(txtInputTime, txtInputTime.getText());
        boolean isIMDBRateValid = validateModel.validateInput(txtInputIMDBRating, txtInputIMDBRating.getText());
        boolean isMyRateValid = validateModel.validateInput(txtInputPersonal, txtInputPersonal.getText());

        if (isNameValid && isArtistValid && isFilepathValid && isYearValid && isTimeValid && isIMDBRateValid && isMyRateValid) {
            if (typeCU == 1) {
                createNewMovie();
            }
            if (typeCU == 2) {
                updateMovie();
            }
        }
    }


    private void createNewMovie() { //Here the movie gets created
        int year = Integer.parseInt(txtInputYear.getText());
        String title = txtInputName.getText();
        String director = txtInputDirector.getText();
        String moviePath = txtInputFilepath.getText();
        double movieRating = Double.parseDouble(txtInputIMDBRating.getText());
        double movieTime = currentMovieLength;
        String category = String.valueOf(txtInputCategories.getText());
        double personalRating = 0;
        // Inputs the values from above into a new movie and tries to send it up the layers into the DB, table view and sound map
        Movie movie = new Movie(-1, year, title, director, moviePath, movieRating, movieTime, personalRating, null, category);

        try {
            Movie newCreatedMovie = movieModel.createNewMovie(movie);
            mediaPlayerViewController.addMovieToSoundMap(newCreatedMovie);



            System.out.println(categoryNames + "Bien flyver");

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
            currentSelectedMovie.setPersonalRating(Double.parseDouble(txtInputPersonal.getText()));
            currentSelectedMovie.setCategory(txtInputCategories.getText());

            if (categoryNames != null) {
                for (Category c : categoryNamesOld) {
                    categoryMovieModel.deleteMovieFromCategory(currentSelectedMovie, c);
                }
                for (Category c : categoryNames) {
                    categoryMovieModel.addMovieToCategoryBypass(currentSelectedMovie, c);
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