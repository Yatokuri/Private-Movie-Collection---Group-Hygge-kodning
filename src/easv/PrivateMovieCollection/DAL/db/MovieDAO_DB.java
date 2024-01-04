/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.DAL.db;

// Project imports

import easv.PrivateMovieCollection.BE.Movie;
import easv.PrivateMovieCollection.DAL.IMovieDataAccess;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO_DB implements IMovieDataAccess {

    private final MyDatabaseConnector databaseConnector;
    private static ArrayList<Movie> allMovies;

    public MovieDAO_DB() throws Exception {
        databaseConnector = new MyDatabaseConnector();
        allMovies = new ArrayList<>();
        getAllMovies();
    }

    public List<Movie> getMoviesArray() {
        return allMovies; } // Returns the arraylist of all movies to send up the layers


    public List<Movie> getAllMovies() throws Exception { // Queries the database for all movies to insert them in an arraylist
        allMovies.clear(); // Clear existing data
        try (Connection conn = databaseConnector.getConnection();
             Statement stmt = conn.createStatement())
        {
            String sql = "SELECT * FROM dbo.Movies;";
            ResultSet rs = stmt.executeQuery(sql);
            // Loop through rows from the database result set
            while (rs.next()) {
                //Map DB row to Movie object
                int id = rs.getInt("MovieId");
                String movieName = rs.getString("MovieName");
                String director = rs.getString("MovieDirector");
                int year = rs.getInt("MovieYear");
                String moviePath = rs.getString("MovieFilepath");
                double imdbRating = rs.getDouble("MovieRating");
                double movieLength = rs.getDouble("MovieLength");
                double personalRating = rs.getDouble("Personal");
                String lastWatched = rs.getString("MovieLastViewed");
                Movie movie = new Movie(id, year, movieName, director, moviePath, imdbRating, movieLength, personalRating, lastWatched);
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
        String sql = "INSERT INTO dbo.Movies (MovieName, MovieDirector, MovieYear, MovieFilepath, movieLength, movieRating, Personal, movieLastViewed) VALUES (?,?,?,?,?,?,?,?);";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind parameters
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getMoviePath());
            stmt.setDouble(5, movie.getMovieLength());
            stmt.setDouble(6, movie.getMovieRating());
            stmt.setDouble(7, movie.getPersonalRating());
            stmt.setString(8, movie.getLastWatched());
            //stmt.setString(6, movie.getMovieCategory());
            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create Movie object and send up the layers

            Movie newMovie = new Movie(id, movie.getYear(), movie.getTitle(), movie.getDirector(), movie.getMoviePath(), movie.getMovieRating(), movie.getMovieLength(), movie.getPersonalRating(), movie.getLastWatched());
            allMovies.add(newMovie);
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
        String sql = "UPDATE dbo.Movies SET MovieName = ?, MovieDirector = ?, MovieYear = ?, MovieFilepath = ?, MovieLength = ?, MovieRating = ?, Personal = ?, MovieLastViewed = ? WHERE MovieID = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind parameters
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getMoviePath());
            stmt.setBigDecimal(5, BigDecimal.valueOf(movie.getMovieLength()));
            stmt.setBigDecimal(6, BigDecimal.valueOf(movie.getMovieRating()));
            stmt.setBigDecimal(7, BigDecimal.valueOf(movie.getPersonalRating()));
            stmt.setString(8, movie.getLastWatched());
            //stmt.setString(6, movie.getMovieCategory());
            stmt.setInt(9, movie.getId());
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
            allMovies.removeIf(m -> m.getId() == movie.getId());
        }
        catch (SQLException ex)
        {
            // create entry in log file
            ex.printStackTrace();
            throw new Exception("Could not delete Movie", ex);
        }
    }





}
