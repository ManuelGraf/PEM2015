package pem.yara.echonest;

public class Config {

    public static final String API_KEY = "YUNP8GLWKN942WKOF";

    public static final String CONSUMER_KEY = "39c9bbe82d4098391799ef8eccc930ee";

    public static final String SHARED_SECRET = "xca0NPJwR6yCK9xaXyYleA";

    public static final String USER = "yara";

    public static final String EMAIL = "grafm@cip.ifi.lmu.de";

    public static final String PASSWORD = "49766794";

    public static final String BASE_URL = "http://developer.echonest.com/api/v4/";

    public static final String SONG_SEARCH_URL = BASE_URL + "song/search?api_key=" + API_KEY + "&format=json&results=1&artist=%s&title=%s&bucket=audio_summary";
}
