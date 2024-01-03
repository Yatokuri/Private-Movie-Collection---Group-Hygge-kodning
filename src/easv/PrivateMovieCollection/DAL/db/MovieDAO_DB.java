/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.DAL.db;

// Project imports
import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.DAL.IMovieDataAccess;

// Java imports
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO_DB implements IMovieDataAccess {

    private final MyDatabaseConnector databaseConnector;
    ArrayList<Movie> allMovies;

    public MovieDAO_DB() throws Exception {
        databaseConnector = new MyDatabaseConnector();
        getAllMovies();
    }

    public List<Movie> getMoviesArray() { return allMovies; } // Returns the arraylist of all movies to send up the layers


    public List<Movie> getAllMovies() throws Exception { // Queries the database for all movies to insert them in an arraylist

        allMovies = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Movies;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to Movie object
                int id = rs.getInt("Id");
                String movieName = rs.getString("MovieName");
                String artist = rs.getString("MovieArtist");
                int year = rs.getInt("MovieYear");
                String moviePath = rs.getString("MovieFilepath");
                double movieRating = rs.getDouble("MovieRating");
                String movieCategory = rs.getString("MovieCategory");
                Movie movie = new Movie(id, year, movieName, artist, moviePath, movieRating, movieCategory);
                allMovies.add(movie);
            }
            return allMovies;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            throw new Exception("Could not get movies from database", ex);
        }
    }

    public Movie createMovie(Movie movie) throws Exception { // creates a movie and adds it to the database

        // SQL command
        String sql = "INSERT INTO dbo.Movies (MovieName, MovieArtist, MovieYear, MovieFilepath, movieLength, movieCategory) VALUES (?,?,?,?,?,?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getArtist());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getMoviePath());
            stmt.setDouble(5, movie.getMovieLength());
            stmt.setString(6, movie.getMovieCategory());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Movie object and send up the layers

            Movie newMovie = new Movie(id, movie.getYear(), movie.getTitle(), movie.getArtist(), movie.getMoviePath(), movie.getMovieLength(), movie.getMovieCategory());
            return newMovie;
        }

        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not create Movie", ex);
        }
    }

    public void updateMovie(Movie movie) throws Exception { // updates an existing movie in the database with new data

        // SQL command
        String sql = "UPDATE dbo.Movies SET MovieName = ?, MovieArtist = ?, MovieYear = ?, MovieFilepath = ?, MovieLength = ?, MovieCategory = ? WHERE MovieID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getArtist());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getMoviePath());
            stmt.setBigDecimal(5, BigDecimal.valueOf(movie.getMovieLength()));
            stmt.setString(6, movie.getMovieCategory());
            stmt.setInt(7, movie.getId());

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

    public void deleteMovie(Movie movie) throws Exception { // deletes a movie from the database

        // SQL command
        String sqlMovies = "DELETE FROM dbo.Movies WHERE MovieID = ?;";
        String sqlCategoryMovies = "DELETE FROM dbo.CategoryMovies WHERE MovieID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlMovies);
             PreparedStatement stmt2 = conn.prepareStatement(sqlCategoryMovies)) {
            // Bind parameters
            stmt.setInt(1, movie.getId());
            stmt2.setInt(1, movie.getId());

            // Run the specified SQL statement
            stmt2.executeUpdate();
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete Movie", ex);
        }
    }
}
