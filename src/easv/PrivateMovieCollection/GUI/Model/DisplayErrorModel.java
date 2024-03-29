/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Model;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DisplayErrorModel {
    private static final Image programIcon = new Image ("/Icons/mainIcon.png");

    // This is used to make custom message from other Class
    public void displayErrorC(String t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setIcon(alert);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t);
        alert.showAndWait();
    }

    //This is used to show a specific error where movie could not be found
    public void displayErrorNMF(String movieNameNotFound) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String truncatedTitle = movieNameNotFound.substring(0, Math.min(movieNameNotFound.length(), 30));
        String errorMessage = truncatedTitle + (movieNameNotFound.length() > 30 ? "..." : "") + " could not be found\n(It might have been moved to a different location)";
        setIcon(alert);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(errorMessage);
        alert.showAndWait();
    }

    //This is used to make the auto generated error message
    public void displayError(Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setIcon(alert);

        if (t instanceof SQLServerException) {
            alert.setTitle("Something went wrong");
            alert.setHeaderText("The database could not be reached. Please try again.");
            alert.showAndWait();
            System.exit(0);
        } else {
            alert.setTitle("Something went wrong");
            alert.setHeaderText(t.getMessage());
            alert.showAndWait();
        }
    }

    private void setIcon(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(programIcon);
    }
}
