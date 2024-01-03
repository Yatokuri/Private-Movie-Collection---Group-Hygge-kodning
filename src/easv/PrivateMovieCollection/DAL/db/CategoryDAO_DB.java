/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.DAL.db;

// Project imports
import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.DAL.ICategoryDataAccess;

//Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO_DB implements ICategoryDataAccess {

    private final MyDatabaseConnector databaseConnector;

    public CategoryDAO_DB() throws IOException {
        databaseConnector = new MyDatabaseConnector();
    }

    public List<Category> getAllCategories() throws Exception { // Returns all categorys from the database

        ArrayList<Category> allCategories = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Category;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {

                //Map DB row to Category object
                int id = rs.getInt("Id");
                String title = rs.getString("Name");
                int movieCount = rs.getInt("MovieCount");
                double movieTotalTime = rs.getDouble("MovieTotalTime");
                Category category = new Category(id, title, movieCount, movieTotalTime);
                allCategories.add(category);
            }
            return allCategories;

        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get categorys from database", ex);
        }
    }

    public Category createCategory(Category category) throws Exception { // Creates a category in the database with the given name

        // SQL command
        String sql = "INSERT INTO dbo.Category (Name) VALUES (?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, category.getCategoryName());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create category object and send up the layers
            Category createdCategory;
            createdCategory = new Category(id, category.getCategoryName(), category.getMovieCount(), category.getMovieTotalTime());

            return createdCategory;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create category", ex);
        }

    }

    public void updateCategory(Category category) throws Exception { // Updates the name of a category in the database where the id matches the given category

        // SQL command
        String sql = "UPDATE dbo.Category SET Name = ? WHERE ID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not update category", ex);
        }
    }

    public void deleteCategory(Category category) throws Exception { // Deletes a category from the database
        // SQL command
        String  sql = "DELETE FROM dbo.CategoryMovies WHERE CategoryId = ?;";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))

        {
            // Bind parameters
            stmt.setInt(1, category.getId());
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete category", ex);
        }


        // SQL command
        sql = "DELETE FROM dbo.Category WHERE ID = ?;";


        // DELETE FROM dbo.CategoryMovies WHERE CategoryId = ?;

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setInt(1, category.getId());
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete category", ex);
        }
    }


}
