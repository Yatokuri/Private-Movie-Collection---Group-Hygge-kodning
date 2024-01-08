/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.DAL;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;

import java.util.List;

public interface IMovieDataAccess {

    List<Movie> getAllMovies() throws Exception;

    Movie createMovie(Movie movie) throws Exception;

    void updateMovie(Movie movie) throws Exception;

    void deleteMovie(Movie movie) throws Exception;


    List<Movie> getAllMoviesOld() throws Exception;

    List<Movie> getAllMoviesFilter(List<String> categoriesFilter) throws Exception;
}
