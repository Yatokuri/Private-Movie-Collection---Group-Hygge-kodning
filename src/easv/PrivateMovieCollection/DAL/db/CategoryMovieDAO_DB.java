/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.DAL.db;

// Project imports
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.BE.Category;

// Java imports
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
                WHERE S1.PlayListId = ?
                ORDER BY S1.categoryorder ASC""";
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
        String sql = "INSERT INTO dbo.CategoryMovies (MovieId, CategoryId, PlayListOrder) VALUES (?,?,?)";
        String sql2 = "SELECT MAX(PlayListOrder) AS highest_value FROM dbo.CategoryMovies  WHERE PlayListId = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmt2 = conn.prepareStatement(sql2))
        {
            stmt2.setInt(1, category.getId());
            ResultSet rs2 = stmt2.executeQuery();

            int nextIdNumber = 1;
            while (rs2.next()) {
                //Map DB row to category object
                nextIdNumber = rs2.getInt("highest_value");
            }

            // Bind parameters
            stmt.setInt(1, movie.getId());
            stmt.setInt(2, category.getId());
            stmt.setInt(3, nextIdNumber+1);

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
        String sql = "UPDATE dbo.CategoryMovies SET PlayListOrder = ? WHERE MovieId = ? AND CategoryId = ?";
        String sqlOldMovie = "SELECT PlayListOrder FROM dbo.CategoryMovies WHERE MovieId = ? AND CategoryId = ?";
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
                playOrderOld = rs.getInt("PlayListOrder");
            }
            while (rs2.next()){
                playOrderNewMovie = rs2.getInt("PlayListOrder");
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
        String sqlMoviesPlayOrder = "SELECT PlayListOrder FROM dbo.CategoryMovies WHERE MovieId = ? AND CategoryId = ?";
        String sqlDeleteMovie = "DELETE FROM dbo.CategoryMovies WHERE MovieID = ? AND CategoryId = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmtDeleteMovie = conn.prepareStatement(sqlDeleteMovie);
             PreparedStatement  stmtMoviesPlayOrder = conn.prepareStatement(sqlMoviesPlayOrder)) {
            // Bind parameters
            stmtDeleteMovie.setInt(1, movie.getId());
            stmtDeleteMovie.setInt(2, category.getId());
            stmtMoviesPlayOrder.setInt(1, movie.getId());
            stmtMoviesPlayOrder.setInt(2, category.getId());
            // Run the specified SQL statement
            ResultSet rs = stmtMoviesPlayOrder.executeQuery();
            stmtDeleteMovie.executeUpdate(); //We delete movie after cause otherwise we cannot find the right category order

            int playOrder = -1; //Default if no one is found
            while (rs.next()) {
                //Map DB row to category object
                playOrder = rs.getInt("PlayListOrder");
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