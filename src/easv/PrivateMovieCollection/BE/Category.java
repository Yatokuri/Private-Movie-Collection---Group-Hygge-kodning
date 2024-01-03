/**
 * @author Daniel, Rune, og Thomas
 **/
package easv.PrivateMovieCollection.BE;

public class Category {

    private String categoryName;
    private int id;
    private int movieCount;
    private double movieTotalTime;

    public Category(int id, String categoryName, int movieCount, double movieTotalTime){
        this.id = id;
        this.categoryName = categoryName;
        this.movieCount = movieCount;
        this.movieTotalTime = movieTotalTime;
    }

    public String getSongLengthHHMMSS() { // This way you convert songTotalTime to HH:MM:SS format
        long hours = (long) (movieTotalTime / 3600);
        long minutes = (long) ((movieTotalTime % 3600) / 60);
        long remainingSeconds = (long) (movieTotalTime % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    public double getMovieTotalTime() { return movieTotalTime; }
    public void setMovieTotalTime(double movieTotalTime) {this.movieTotalTime = movieTotalTime;}
    public int getId() { return id;}
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getMovieCount() {return movieCount;}
    public void setMovieCount(int songCount) {this.movieCount = movieCount;}

    @Override
    public String toString() {
        return categoryName;
    }
}

