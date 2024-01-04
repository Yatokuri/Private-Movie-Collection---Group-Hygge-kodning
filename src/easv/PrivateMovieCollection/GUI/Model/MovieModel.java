/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Model;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.BLL.MovieManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class MovieModel {

    private static ObservableList<Movie> moviesToBeViewed = null;
    private final MovieManager movieManager;

    public MovieModel() throws Exception {
        movieManager = new MovieManager();
        moviesToBeViewed = FXCollections.observableArrayList();
        moviesToBeViewed.addAll(movieManager.getAllMovies());
    }
    public Movie createNewMovie(Movie newMovie) throws Exception { // Sends a request to the database to add a new movie
        Movie s = movieManager.createNewMovie(newMovie);
        moviesToBeViewed.add(s); // update list
        return s;
    }
    public static ObservableList<Movie> getObservableMovies() { return moviesToBeViewed; } // Returns the movies from the database
    public void updateMovie(Movie updatedMovie) throws Exception { // Sends a request to the database to update a movie
        // update movie in DAL layer (through the layers)
        movieManager.updateMovie(updatedMovie);
        moviesToBeViewed.clear();
        moviesToBeViewed.addAll(movieManager.getAllMovies());
    }
    public ObservableList<Movie> updateMovieList() throws Exception { // Updates the movie list from the database to be accurate again
        moviesToBeViewed.clear();
        moviesToBeViewed.addAll(movieManager.getAllMovies());
        return moviesToBeViewed;
    }
    public void deleteMovie(Movie selectedMovie) throws Exception { // Sends a request to the database to delete a movie
        // delete movie in DAL layer (through the layers)
        movieManager.deleteMovie(selectedMovie);
        // remove from observable list (and UI)
        moviesToBeViewed.remove(selectedMovie);
    }
    public ObservableList<Movie> filterList(List<Movie> movie, String searchText){ // Gets the search filter from the Movie Manager to give to the Controller
        return movieManager.filterList(movie, searchText);

    }
    public Movie getMovieById(int movieId){ // Returns a movie by its id
        return movieManager.getMovieById(movieId);
    }
}
