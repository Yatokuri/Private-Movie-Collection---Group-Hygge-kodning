/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.DAL;

import easv.PrivateMovieCollection.BE.Category;

import java.util.List;

public interface ICategoryDataAccess {

    List<Category> getAllCategories() throws Exception;

    Category createCategory(Category category) throws Exception;

    void updateCategory(Category category) throws Exception;

    void deleteCategory(Category category) throws Exception;

}
