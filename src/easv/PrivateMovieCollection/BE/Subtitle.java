/**
 * @author Daniel, Naylin, og Thomas
 **/
package easv.PrivateMovieCollection.BE;

public class Subtitle {
    private String startTime, endTime, text;
    private StringBuilder textBuilder = new StringBuilder();

    public String getStartTime() {return startTime;}
    public void setStartTime(String startTime) {this.startTime = startTime;}
    public String getEndTime() {return endTime;}
    public void setEndTime(String endTime) {this.endTime = endTime;}
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}

    public void appendText(String additionalText) {
        if (!textBuilder.isEmpty()) {
            textBuilder.append("\n");
        }
            textBuilder.append(additionalText);
    }

}