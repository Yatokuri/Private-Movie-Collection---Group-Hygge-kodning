/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.BE;

public class Movie {

    private String title, artist, moviePath, movieCategory;
    private int year;
    private double movieLength;
    private int id;
       public Movie(int id, int year, String title, String artist, String moviePath, Double movieLength, String category) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.artist = artist;
        this.movieLength = movieLength;
        this.moviePath = moviePath;
        this.movieCategory = category;
    }
    public double getMovieLength() {return movieLength;}

    public void setMovieLength(double movieLength) {this.movieLength = movieLength;}

    public String getSongLengthHHMMSS() { // This way you convert songTotalTime to HH:MM:SS format
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
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getMoviePath() { return moviePath;}
    public void setMoviePath(String moviePath) { this.moviePath = moviePath;}
    @Override
    public String toString()
    {
        return id + ": " + title + " ("+year+")";
    }

    public String getMovieCategory() { return movieCategory; }
    public void setMovieCategory(String movieCategory){ this.movieCategory = movieCategory;}
}