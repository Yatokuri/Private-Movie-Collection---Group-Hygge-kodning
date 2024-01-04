/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.DAL.db;

// Project imports

import easv.PrivateMovieCollection.BE.Category;
import easv.PrivateMovieCollection.BE.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryMovieDAO_DB {

    private final MyDatabaseConnector databaseConnector;
    private final MovieDAO_DB movieDAO_db;

    public CategoryMovieDAO_DB() throws Exception {
        databaseConnector = new MyDatabaseConnector();
        movieDAO_db = new MovieDAO_DB();
    }

    public List<Movie> getAllMoviesCategory(Category category) throws Exception { // Gets all the movies in each category to get the movie count and movie length

        category.setMovieTotalTime(0);
        category.setMovieCount(0);

        ArrayList<Movie> allMoviesInCategory = new ArrayList<>();

        String sql = """
                SELECT Movies.MovieId, Movies.MovieName FROM Movies
                JOIN CategoryMovies S1 ON Movies.MovieId = S1.MovieId
                WHERE S1.CategoryId = ?
                ORDER BY S1.CategoryOrder ASC""";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, category.getId());
            ResultSet rs = stmt.executeQuery();
            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to category object
                int id = rs.getInt("MovieId");

                for (Movie s : movieDAO_db.getMoviesArray())    {
                    if (s.getId() == id)    {
                        category.setMovieCount(category.getMovieCount() + 1);
                        category.setMovieTotalTime(category.getMovieTotalTime() + s.getMovieLength());
                        allMoviesInCategory.add(s);
                    }
                }
            }
            return allMoviesInCategory;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get movies in category from database", ex);
        }
    }

    public void addMovieToCategory(Movie movie, Category category) throws Exception { // Attempts to add a movie to a category

        // SQL command
        String sql = "INSERT INTO dbo.CategoryMovies (MovieId, CategoryId, MovieCategoryId, CategoryOrder) VALUES (?,?,?,?)";

        String sql3 = "SELECT MAX(CategoryOrder) AS highest_value FROM dbo.CategoryMovies  WHERE CategoryId = ?";

        String sql2 = "SELECT MIN(t.RN) AS next_value " + //The problem here is the ID can be 0?
                "FROM (SELECT ROW_NUMBER() OVER (ORDER BY MovieCategoryId) AS RN " +
                "      FROM dbo.CategoryMovies) t " +
                "WHERE NOT EXISTS (SELECT 1 FROM dbo.CategoryMovies WHERE MovieCategoryId = t.RN)";

        String sqlAddCount = "UPDATE dbo.Category SET CategoryCount = CategoryCount + 1 WHERE CategoryId = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmt2 = conn.prepareStatement(sql2);
              PreparedStatement stmt3 = conn.prepareStatement(sql3);
              PreparedStatement stmtAddCount = conn.prepareStatement(sqlAddCount))
        {
        //    stmt2.setInt(1, category.getId());
            ResultSet rs2 = stmt2.executeQuery();

            stmtAddCount.setInt(1, category.getId());
            stmtAddCount.execute();
            stmt3.setInt(1, category.getId());
            ResultSet rs3 = stmt3.executeQuery();

            int nextIdNumber = 1;
            while (rs2.next()) {
                //Map DB row to category object
                nextIdNumber = rs2.getInt("next_value");
                //System.out.println(nextIdNumber + " Næste ID (Debug)");
            }

            int nextOrderNumber = 1;
            while (rs3.next()) {
                //Map DB row to category object
                nextOrderNumber = rs3.getInt("highest_value");
            }

            // Bind parameters
            stmt.setInt(1, movie.getId());
            stmt.setInt(2, category.getId());
            stmt.setInt(3, nextIdNumber);
            stmt.setInt(4, nextOrderNumber+1);

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not add movie to category", ex);
        }
    }

    public void updateMovieInCategory(Movie movie, Movie oldMovie, Category category) throws Exception { // Updates the movie category order when it gets moved up or down in the list
        // SQL commands
        String sql = "UPDATE dbo.CategoryMovies SET CategoryOrder = ? WHERE MovieId = ? AND CategoryId = ?";
        String sqlOldMovie = "SELECT CategoryOrder FROM dbo.CategoryMovies WHERE MovieId = ? AND CategoryId = ?";
        String sqlNewMovie = "SELECT CategoryOrder FROM dbo.CategoryMovies WHERE MovieId = ? AND CategoryId = ?";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement oldMoviePlayOrder = conn.prepareStatement(sqlOldMovie);
             PreparedStatement newMoviePlayOrder = conn.prepareStatement(sqlNewMovie);
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            oldMoviePlayOrder.setInt(1, oldMovie.getId());
            oldMoviePlayOrder.setInt(2, category.getId());
            newMoviePlayOrder.setInt(1, movie.getId());
            newMoviePlayOrder.setInt(2, category.getId());

            // Makes a result set of both the old and new play order position
            ResultSet rs = oldMoviePlayOrder.executeQuery();
            ResultSet rs2 = newMoviePlayOrder.executeQuery();
            int playOrderOld = 1;
            int playOrderNewMovie = 1;
            while (rs.next()) {
                //Map DB row to category object
                playOrderOld = rs.getInt("CategoryOrder");
            }
            while (rs2.next()){
                playOrderNewMovie = rs2.getInt("CategoryOrder");
            }

            // Bind parameters
            stmt.setInt(1, playOrderOld);
            stmt.setInt(2, movie.getId());
            stmt.setInt(3, category.getId());

            // Iterates through the database to update the play order of all the movies when you move the movie upwards in the
            // category, so it will play in the correct order and remember for next time you open it
            if (playOrderNewMovie < playOrderOld) {
                String sqlUpdateCategory = "UPDATE dbo.CategoryMovies SET CategoryOrder = CategoryOrder - 1 WHERE CategoryId =? AND CategoryOrder <= ? AND CategoryOrder >=?";
                try (PreparedStatement stmt4 = conn.prepareStatement(sqlUpdateCategory)) {
                    stmt4.setInt(1, category.getId());
                    stmt4.setInt(2, playOrderOld);
                    stmt4.setInt(3, playOrderNewMovie);
                    stmt4.executeUpdate();
                }
            }
            //Iterates through the database to update the play order of all the movies when you move the movie downwards in the category, so it will play in the correct order
            if (playOrderNewMovie > playOrderOld){
                String sqlUpdateCategory = "UPDATE dbo.CategoryMovies SET CategoryOrder = CategoryOrder + 1 WHERE CategoryId = ? AND CategoryOrder >= ? AND CategoryOrder <=?";
                try (PreparedStatement stmt4 = conn.prepareStatement(sqlUpdateCategory)) {
                    stmt4.setInt(1, category.getId());
                    stmt4.setInt(2, playOrderOld);
                    stmt4.setInt(3, playOrderNewMovie);
                    stmt4.executeUpdate();
                }
            }
            // Run the specified SQL statement
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not update movie", ex);
        }
    }

    public void deleteMovieFromCategory(Movie movie, Category category) throws Exception { // deletes a movie from a category
        //When we delete a movie we also need to change there order

        // SQL command
        String sqlMoviesPlayOrder = "SELECT CategoryOrder FROM dbo.CategoryMovies WHERE MovieId = ? AND CategoryId = ?";
        String sqlDeleteMovie = "DELETE FROM dbo.CategoryMovies WHERE MovieID = ? AND CategoryId = ?";

        String sqlUpdateCount = "UPDATE dbo.Category SET CategoryCount = CategoryCount - 1 WHERE CategoryId = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmtDeleteMovie = conn.prepareStatement(sqlDeleteMovie);
             PreparedStatement stmtMoviesPlayOrder = conn.prepareStatement(sqlMoviesPlayOrder);
             PreparedStatement stmtUpdateCount = conn.prepareStatement(sqlUpdateCount)) {
            // Bind parameters
            stmtDeleteMovie.setInt(1, movie.getId());
            stmtDeleteMovie.setInt(2, category.getId());
            stmtMoviesPlayOrder.setInt(1, movie.getId());
            stmtMoviesPlayOrder.setInt(2, category.getId());
            stmtUpdateCount.setInt(1, category.getId());
            // Run the specified SQL statement
            ResultSet rs = stmtMoviesPlayOrder.executeQuery();
            stmtDeleteMovie.executeUpdate(); //We delete movie after cause otherwise we cannot find the right category order
            stmtUpdateCount.execute();
            int playOrder = -1; //Default if no one is found
            while (rs.next()) {
                //Map DB row to category object
                playOrder = rs.getInt("CategoryOrder");
            }
            if (playOrder > -1) {
                String sqlUpdatePlayOrder = "UPDATE dbo.CategoryMovies SET CategoryOrder = CategoryOrder - 1 WHERE CategoryId = ? AND CategoryOrder >= ?";
                try (PreparedStatement stmt3 = conn.prepareStatement(sqlUpdatePlayOrder)) {
                    stmt3.setInt(1, category.getId());
                    stmt3.setInt(2, playOrder);
                    stmt3.executeUpdate();
                }
            }
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete category", ex);
        }
    }

    public void deleteAllMoviesFromCategory(Category category) throws Exception { // Empties out the category by deleting all movies within without deleting the category itself
        // SQL command
        String sql = "DELETE FROM dbo.CategoryMovies WHERE CategoryId = ?";

        String sql2 = "UPDATE dbo.Category SET CategoryCount = 0 WHERE CategoryId = ?";
        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             PreparedStatement stmt2 = conn.prepareStatement(sql2))
        {
            // Bind parameters
            stmt.setInt(1, category.getId());
            // Run the specified SQL statement
            stmt.executeUpdate();

            stmt2.setInt(1, category.getId());
            stmt2.executeUpdate();

        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete category", ex);
        }
    }

    public List<Integer>  getMoviesCategories(Movie movie) throws Exception { // Empties out the category by deleting all movies within without deleting the category itself
        List<Integer> categoryNames = new ArrayList<>();

        // SQL command
        String sql = "SELECT CategoryId FROM dbo.CategoryMovies WHERE MovieId = ?;";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parameters
            stmt.setInt(1, movie.getId());
            // Run the specified SQL statement
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                int categoryId = rs.getInt("CategoryId");
                categoryNames.add((categoryId));
            }


        }


        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete category", ex);
        }

        return categoryNames;
    }


}