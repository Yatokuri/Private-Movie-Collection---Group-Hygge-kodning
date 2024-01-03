  /**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.BLL;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.DAL.ICategoryDataAccess;
import easv.PrivateMovieCollection.DAL.db.CategoryDAO_DB;
import easv.PrivateMovieCollection.GUI.Model.CategoryModel;

import java.io.IOException;
import java.util.List;

public class CategoryManager {

    private final ICategoryDataAccess categoryDAO;

    public CategoryManager() throws IOException {
        categoryDAO = new CategoryDAO_DB();
    }

    public Category createNewCategory(Category newCategory) throws Exception {
        return categoryDAO.createCategory(newCategory);
    }

    public List<Category> getAllCategory() throws Exception {
        return categoryDAO.getAllCategories();
    }

    public void updateCategory(Category selectedCategory) throws Exception {
        categoryDAO.updateCategory(selectedCategory);
    }

    public void deleteCategory(Category selectedCategory) throws Exception {
        categoryDAO.deleteCategory(selectedCategory);
    }

    public Category getCategoryById(int cId) { // Returns a category with the given id
        for (Category c : CategoryModel.getObservableCategories()) {
            if (c.getId() == cId) {
                return c;
            }
        }
        return null;
    }
}
