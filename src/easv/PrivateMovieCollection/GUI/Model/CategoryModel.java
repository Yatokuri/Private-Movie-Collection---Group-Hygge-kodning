/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.GUI.Model;

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BLL.CategoryManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CategoryModel {

    private static ObservableList<Category> categoriesToBeViewed = null;

    private static CategoryManager categoryManager = null;

    public CategoryModel() throws Exception {
        categoryManager = new CategoryManager();
        categoriesToBeViewed = FXCollections.observableArrayList();
        categoriesToBeViewed.addAll(categoryManager.getAllCategory());
    }

    public boolean createNewCategory(Category newCategory) throws Exception { // Sends a request to the database to create a new category
        for (Category existingCategory : categoriesToBeViewed) {
            if (existingCategory.getCategoryName().equalsIgnoreCase(newCategory.getCategoryName())) {
                return false; // Category with the same name already exists
            }
        }
        Category p = categoryManager.createNewCategory(newCategory);
        categoriesToBeViewed.add(p); // update list
        return true;
    }

    public static ObservableList<Category> getObservableCategories() {return categoriesToBeViewed;} // Returns the playlists
    public void updateCategory(Category updatedCategory) throws Exception { // Sends a request to the database to update a category
        // Update movie in DAL layer (through the layers)
        categoryManager.updateCategory(updatedCategory);

        // update observable list (and UI)
        Category p = categoriesToBeViewed.get(categoriesToBeViewed.indexOf(updatedCategory));
        p.setCategoryName(updatedCategory.getCategoryName());
    }

    public void deleteCategory(Category selectedCategory) throws Exception { // Sends a request to the database to delete a category
        // delete movie in DAL layer (through the layers)
        categoryManager.deleteCategory(selectedCategory);

        // remove from observable list (and UI)
        categoriesToBeViewed.remove(selectedCategory);
    }
    // Returns a Category based on its id
    public static Category getCategoryById(int ctId) { return categoryManager.getCategoryById(ctId); }
}