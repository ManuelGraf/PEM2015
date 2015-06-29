package pem.yara.entity;

/**
 * Created by yummie on 29.06.2015.
 */
public class YaraTrack {
    private int id;
    private String title;
    private String path;
    private String date_created;
    private int length;


    public YaraTrack(int id, String title, String path, String date_created, int length) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.date_created = date_created;
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
