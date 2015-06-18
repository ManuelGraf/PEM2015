package pem.yara.entity;

public class YaraSong {

    private String title;
    private String artist;
    private String uri;
    private Double tempo;

    public YaraSong(String title, String artist, String uri) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
    }

    public YaraSong(String title, String artist, String uri, Double tempo) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.tempo = tempo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Double getTempo() {
        return tempo;
    }

    public void setTempo(Double tempo) {
        this.tempo = tempo;
    }

    @Override
    public String toString() {
        return artist + "  \"" + title + "\" at " + uri;
    }
}
