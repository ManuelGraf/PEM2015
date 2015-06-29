package pem.yara.entity;

public class YaraSong {

    private int id;
    private String title;
    private String artist;
    private String uri;
    private Double tempo;
    private Double score;
    private int blocked;
    private int playCount;

    public YaraSong(int id,String title, String artist, String uri, Double tempo, Double score, int blocked, int playCount) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.tempo = tempo;
        this.score = score;
        this.blocked = blocked;
        this.playCount = playCount;
    }

    public YaraSong(String title, String artist, String uri, Double tempo) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.tempo = tempo;
    }
    public YaraSong(String title, String artist, String uri) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    @Override
    public String toString() {
        return artist + "  \"" + title + "\" at " + uri;
    }
}
