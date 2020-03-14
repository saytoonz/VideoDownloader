package io.github.mthli.Ninja.downloaders.history_files.models;

import java.io.Serializable;

public class HistoryModel implements Serializable {
    private String id;
    private String link;
    private String downloaded;
    private String title;
    private String local_location;
    private String link_type;
    private String time;
    private String media_type;
    private String originalUrl;
    private int indexPosition;

    public HistoryModel(String id, String originalUrl, String link, String media_type,String link_type,
                        String downloaded, String title, String local_location, String time) {
        this.id = id;
        this.link = link;
        this.downloaded = downloaded;
        this.title = title;
        this.local_location = local_location;
        this.link_type = link_type;
        this.time= time;
        this.media_type = media_type;
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public int getIndexPosition() {
        return indexPosition;
    }

    public void setIndexPosition(int indexPosition) {
        this.indexPosition = indexPosition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink_type() {
        return link_type;
    }

    public void setLink_type(String link_type) {
        this.link_type = link_type;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocal_location() {
        return local_location;
    }

    public void setLocal_location(String local_location) {
        this.local_location = local_location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
