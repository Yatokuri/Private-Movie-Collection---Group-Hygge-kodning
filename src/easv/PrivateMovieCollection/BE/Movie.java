/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.BE;

public class Movie {

    private String title, director, moviePath, lastWatched, category, posterPath, imdbId, movieDescription;
    private int year, id;
    private double movieLength, movieRating, personalRating;

       public Movie(int id, int year, String title, String director, String moviePath, Double movieRating, Double movieLength, Double personalRating, String lastWatched, String category, String posterPath, String imdbId, String movieDescription) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.movieRating = movieRating;
        this.movieLength = movieLength;
        this.moviePath = moviePath;
        this.personalRating = personalRating;
        this.lastWatched = lastWatched;
        this.category = category;
        this.posterPath = posterPath;
        this.imdbId = imdbId;
        this.movieDescription = movieDescription;
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

    public double getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(double personalRating) {
        this.personalRating = personalRating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }
}