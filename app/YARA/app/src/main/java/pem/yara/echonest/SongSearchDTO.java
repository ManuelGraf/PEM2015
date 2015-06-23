package pem.yara.echonest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

class SongSearchDTO {

    @SerializedName("response")
    private ResponseDTO response;

    SongSearchDTO() {
    }

    ResponseDTO getResponse() {
        return response;
    }

    void setResponse(ResponseDTO response) {
        this.response = response;
    }

    class ResponseDTO {

        @SerializedName("status")
        private StatusDTO statusDTO;

        @SerializedName("songs")
        private List<SongsDTO> songsDTO;

        ResponseDTO() {
        }

        public StatusDTO getStatusDTO() {
            return statusDTO;
        }

        public void setStatusDTO(StatusDTO statusDTO) {
            this.statusDTO = statusDTO;
        }

        public List<SongsDTO> getSongsDTO() {
            return songsDTO;
        }

        public void setSongsDTO(List<SongsDTO> songsDTO) {
            this.songsDTO = songsDTO;
        }
    }

    class StatusDTO {

        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String success;

        @SerializedName("version")
        private String version;

        StatusDTO() {
        }

        int getCode() {
            return code;
        }

        void setCode(int code) {
            this.code = code;
        }

        String getSuccess() {
            return success;
        }

        void setSuccess(String success) {
            this.success = success;
        }

        String getVersion() {
            return version;
        }

        void setVersion(String version) {
            this.version = version;
        }
    }

    class SongsDTO {

        @SerializedName("artist_id")
        private String artistId;

        @SerializedName("id")
        private String songId;

        @SerializedName("artist_name")
        private String artist;

        @SerializedName("title")
        private String title;

        @SerializedName("audio_summary")
        private AudioSummaryDTO audioSummary;

        SongsDTO() {
        }

        String getArtistId() {
            return artistId;
        }

        void setArtistId(String artistId) {
            this.artistId = artistId;
        }

        String getSongId() {
            return songId;
        }

        void setSongId(String songId) {
            this.songId = songId;
        }

        String getArtist() {
            return artist;
        }

        void setArtist(String artist) {
            this.artist = artist;
        }

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        AudioSummaryDTO getAudioSummary() {
            return audioSummary;
        }

        void setAudioSummary(AudioSummaryDTO audioSummary) {
            this.audioSummary = audioSummary;
        }
    }

    class AudioSummaryDTO {

        @SerializedName("acousticness")
        private Double acousticness;

        @SerializedName("analysis_url")
        private String analysisUrl;

        @SerializedName("audio_md5")
        private String md5;

        @SerializedName("danceability")
        private Double danceability;

        @SerializedName("duration")
        private Double duration;

        @SerializedName("energy")
        private Double energy;

        @SerializedName("key")
        private Double key;

        @SerializedName("liveness")
        private Double liveness;

        @SerializedName("loudness")
        private Double loudness;

        @SerializedName("mode")
        private Double mode;

        @SerializedName("speechiness")
        private Double speechiness;

        @SerializedName("tempo")
        private Double tempo;

        @SerializedName("time_signature")
        private Double timeSignature;

        @SerializedName("valence")
        private Double valence;

        AudioSummaryDTO() {
        }

        Double getAcousticness() {
            return acousticness;
        }

        void setAcousticness(Double acousticness) {
            this.acousticness = acousticness;
        }

        String getAnalysisUrl() {
            return analysisUrl;
        }

        void setAnalysisUrl(String analysisUrl) {
            this.analysisUrl = analysisUrl;
        }

        String getMd5() {
            return md5;
        }

        void setMd5(String md5) {
            this.md5 = md5;
        }

        Double getDanceability() {
            return danceability;
        }

        void setDanceability(Double danceability) {
            this.danceability = danceability;
        }

        Double getDuration() {
            return duration;
        }

        void setDuration(Double duration) {
            this.duration = duration;
        }

        Double getEnergy() {
            return energy;
        }

        void setEnergy(Double energy) {
            this.energy = energy;
        }

        Double getKey() {
            return key;
        }

        void setKey(Double key) {
            this.key = key;
        }

        Double getLiveness() {
            return liveness;
        }

        void setLiveness(Double liveness) {
            this.liveness = liveness;
        }

        Double getLoudness() {
            return loudness;
        }

        void setLoudness(Double loudness) {
            this.loudness = loudness;
        }

        Double getMode() {
            return mode;
        }

        void setMode(Double mode) {
            this.mode = mode;
        }

        Double getSpeechiness() {
            return speechiness;
        }

        void setSpeechiness(Double speechiness) {
            this.speechiness = speechiness;
        }

        Double getTempo() {
            return tempo;
        }

        void setTempo(Double tempo) {
            this.tempo = tempo;
        }

        Double getTimeSignature() {
            return timeSignature;
        }

        void setTimeSignature(Double timeSignature) {
            this.timeSignature = timeSignature;
        }

        Double getValence() {
            return valence;
        }

        void setValence(Double valence) {
            this.valence = valence;
        }
    }
}