/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Model;

import easv.PrivateMovieCollection.BE.Movie;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ValidateModel {
    private String setupUpdateOriginalName = "";
    private boolean setupUpdateOriginal = true;
    private static final String[]validFiles  = {"mp4" , "mpeg4"};

    public ValidateModel()  {
    }
    public boolean validateInput(TextField textField, String value) { // Method to check if data in CU window is valid
        switch (textField.getId()) {
            case "txtInputName":
                return !value.isEmpty() && value.length() <= 150; // The same number we have set in the nvarchar in SQL
            case "txtInputArtist":
                return !value.isEmpty() && value.length() <= 100; // -||-
            case "txtInputPersonalRating", "txtInputIMDBRating":
                try {
                    double rate = Double.parseDouble(value);
                    return rate >= 0.0 && rate <= 10.0;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "txtInputFilepath":
                if (setupUpdateOriginal) {
                    setupUpdateOriginalName = value;
                    setupUpdateOriginal = false;
                }
                if (setupUpdateOriginalName.equals(value))  { // When updating a movie the filepath can be the same as before of cause
                    return isValidMediaPath(value);
                }
                for  (Movie m : MovieModel.getObservableMovies()) { // We don't want people to have the same movie path twice
                    if (m.getMoviePath().equals(value)) {
                        return false;
                    }
                }
                return isValidMediaPath(value); // If there time is 00:00:00 that mean the movie is invalid
            case "txtInputTime":
                return !value.equals("00:00:00");

            case "txtInputYear": //You can add a movie from year 1 to current year
                try {
                    int year = Integer.parseInt(value);
                    int currentYear = Year.now().getValue();
                    return year >= 1 && year <= currentYear;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return true;
        }
    }

    public String btnChoose() {   // Method to choose valid
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Movie Files", validFiles2);
        fileChooser.getExtensionFilters().add(extFilter);
        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();  // Get the selected file path and save it
        }
        return "";
    }

    public boolean isValidMediaPath(String path) { // Method to check if a file is valid
        List<String> supportedExtensions = Arrays.asList(validFiles);
        try {
            Path filePath = FileSystems.getDefault().getPath(path);
            String fileName = filePath.getFileName().toString();
            int lastIndexOf = fileName.lastIndexOf(".");
            String extension = (lastIndexOf != -1 && lastIndexOf != 0) ? fileName.substring(lastIndexOf + 1).toLowerCase() : "";
            return filePath.toFile().isFile() && supportedExtensions.contains(extension);
        } catch (Exception e) {
            return false;
        }
    }

    public void updateTimeText(MediaPlayer newMovie, Consumer<String> onReadyCallback) {
        updateMovieTime(newMovie, onReadyCallback, 3);
    }
    private void updateMovieTime(MediaPlayer newMovie, Consumer<String> onReadyCallback, int remainingTries) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
            newMovie.setOnReady(() -> {
                long totalSeconds = (long) newMovie.getTotalDuration().toSeconds();
                String formattedTime = String.format("%02d:%02d:%02d " + "-" + totalSeconds, totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
                // Check if the time is valid (you may need to modify this condition based on your requirements)
                if (!formattedTime.equals("00:00:00 -0")) {
                    // Execute the callback with the formatted time
                    if (onReadyCallback != null) {
                        onReadyCallback.accept(formattedTime);
                    }
                } else if (remainingTries > 0) { // Retry if the time is not valid and there are remaining tries
                    updateMovieTime(newMovie, onReadyCallback, remainingTries - 1);
                }
            });
    }

    private static final String[] validFiles2 = generateValidFiles2(); //Convert validFiles where mp3 be to *.mp3 etc.
    private static String[] generateValidFiles2() {
        String[] validFiles2 = new String[ValidateModel.validFiles.length];
        for (int i = 0; i < ValidateModel.validFiles.length; i++) {
            validFiles2[i] = "*." + ValidateModel.validFiles[i];
        }
        return validFiles2;
    }

}