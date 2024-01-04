/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.BLL;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.DAL.db.CategoryMovieDAO_DB;

import java.util.List;

public class CategoryMovieManager {

    private final CategoryMovieDAO_DB categoryMovieDAO;

    public CategoryMovieManager() throws Exception {
        categoryMovieDAO = new CategoryMovieDAO_DB();
    }

    public void addMovieToCategory(Movie movie, Category category) throws Exception {
        categoryMovieDAO.addMovieToCategory(movie, category);
    }

    public List<Movie> getAllMoviesCategory(Category category) throws Exception {
        return categoryMovieDAO.getAllMoviesCategory(category);
    }

    public void updateMovieInCategory(Movie movie, Movie oldMovie, Category category) throws Exception {
        categoryMovieDAO.updateMovieInCategory(movie, oldMovie, category);
    }

    public void deleteMovieFromCategory(Movie movie, Category category) throws Exception {
        categoryMovieDAO.deleteMovieFromCategory(movie, category);
    }

    public void deleteAllMoviesFromCategory(Category category) throws Exception {
        categoryMovieDAO.deleteAllMoviesFromCategory(category);
    }

    public List<Integer> moviesCategories(Movie selectedMovie) throws Exception {
        return categoryMovieDAO.getMoviesCategories(selectedMovie);
    }

}
