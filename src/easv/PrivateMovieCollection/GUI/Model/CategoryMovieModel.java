package easv.PrivateMovieCollection.GUI.Model;

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BLL.CategoryMovieManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class CategoryMovieModel {
    private final CategoryMovieManager categoryMovieManager;
    private final ObservableList<Movie> categoriesToBeViewed;

    public CategoryMovieModel() throws Exception {
        categoryMovieManager = new CategoryMovieManager();
        categoriesToBeViewed = FXCollections.observableArrayList();
        for (Category c: CategoryModel.getObservableCategories()) {
            categoriesToBeViewed.addAll(categoryMovieManager.getAllMoviesCategory(c));
        }
    }
    public void categoryMovies(Category category) throws Exception { // changes the playlist you are viewing and inserts the relevant songs
        categoriesToBeViewed.clear();
        categoriesToBeViewed.addAll(categoryMovieManager.getAllMoviesCategory(category));
    }
    public boolean addMovieToCategory(Movie newMovie, Category category) throws Exception { // Sends a request to the database to add a song to a playlist
        for (Movie m : categoriesToBeViewed) {
            if (newMovie.getId() == m.getId()) {
                return false; // Exit the method fast
            }
        }
        categoryMovieManager.addMovieToCategory(newMovie, category);
        categoriesToBeViewed.add(newMovie); // update list // Adds the new song to the playlist observable list
        return true;
    }

    public void updateMovieInCategory (Movie movie, Movie oldMovie, Category category) throws Exception { // Sends a request to the database to update a song in a category
        categoryMovieManager.updateMovieInCategory(movie, oldMovie, category);
    }

    public void deleteMovieFromCategory (Movie movie, Category category) throws Exception { // Sends a request to the database to delete a song from a category
        categoryMovieManager.deleteMovieFromCategory(movie, category);
        categoriesToBeViewed.clear();
        categoriesToBeViewed.addAll(categoryMovieManager.getAllMoviesCategory(category)); // Updates the category observable list with the changes
    }

    public void deleteAllMoviesFromCategory (Category category) throws Exception { // Sends a request to the database to empty the category of all movies
        categoryMovieManager.deleteAllMoviesFromCategory(category);
    }
    public ObservableList<Movie> getObservableCategoriesMovie() {return categoriesToBeViewed;} // Returns the category
}
