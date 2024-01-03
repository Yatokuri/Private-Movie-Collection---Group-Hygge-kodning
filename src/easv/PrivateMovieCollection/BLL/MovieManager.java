/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.BLL;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.DAL.IMovieDataAccess;
import easv.PrivateMovieCollection.DAL.db.MovieDAO_DB;
import easv.PrivateMovieCollection.GUI.Model.MovieModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class MovieManager {

    private final IMovieDataAccess movieDAO;
    private final MovieDAO_DB movieDao_DB;

    public MovieManager() throws Exception {
        movieDAO = new MovieDAO_DB();
        movieDao_DB = new MovieDAO_DB();
    }

    public Movie createNewMovie(Movie newMovie) throws Exception {
        return movieDAO.createMovie(newMovie);
    }

    public List<Movie> getAllMovies() throws Exception {
        return movieDao_DB.getAllMovies();
    }

    public void updateMovie(Movie selectedMovie) throws Exception {
        movieDAO.updateMovie(selectedMovie);
    }

    public void deleteMovie(Movie selectedMovie) throws Exception {
        movieDAO.deleteMovie(selectedMovie);
    }


    public Movie getMovieById(int movieId) {
        for (Movie s : MovieModel.getObservableMovies()) {
            if (s.getId() == movieId) {
                return s;
            }
        }
        return null;
    }

    private boolean searchFindsMovies(Movie movie, String searchText) { // Creates the search parameter for the title and artist column to use for the search filter
        return (movie.getTitle().toLowerCase().contains(searchText.toLowerCase())) || (movie.getArtist().toLowerCase().contains(searchText.toLowerCase()));
    }

    public ObservableList<Movie> filterList(List<Movie> movie, String searchText) { // Creates an observable list for the search function in the GUI that
        List<Movie> filterList = new ArrayList<>();                               // mirrors the movie list but changes based on search input from the above method
        for (Movie s : movie) {
            if (searchFindsMovies(s, searchText)) {
                filterList.add(s);
            }
        }
        return FXCollections.observableList(filterList);
    }

}
