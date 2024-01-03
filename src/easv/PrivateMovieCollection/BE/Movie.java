/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.BE;

public class Movie {

    private String title, director, moviePath, lastWatched;
    private int year;
    private double movieLength, movieRating;
    private int id;
       public Movie(int id, int year, String title, String director, String moviePath, Double movieRating, Double movieLength, String lastWatched) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.movieRating = movieRating;
        this.movieLength = movieLength;
        this.moviePath = moviePath;
        this.lastWatched = lastWatched;
    }
    public double getMovieLength() {return movieLength;}

    public void setMovieLength(double movieLength) {this.movieLength = movieLength;}

    public String getMovieLengthHHMMSS() { // This way you convert movieTotalTime to HH:MM:SS format
        long hours = (long) (movieLength / 3600);
        long minutes = (long) ((movieLength % 3600) / 60);
        long remainingSeconds = (long) (movieLength % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    public int getId() {return id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public int getYear() {return year;}
    public void setYear(int year) {this.year = year;}
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getMoviePath() { return moviePath;}
    public void setMoviePath(String moviePath) { this.moviePath = moviePath;}
    public void setMovieRating(Double movieRating) {this.movieRating = movieRating;}
    public double getMovieRating(){return movieRating;}
    @Override
    public String toString()
    {
        return id + ": " + title + " ("+year+")";
    }

    public String getLastWatched() {return lastWatched;}
    public void setLastWatched(String lastWatched) {this.lastWatched = lastWatched;}
}