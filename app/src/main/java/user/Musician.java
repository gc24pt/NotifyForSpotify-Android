package user;

import java.io.Serializable;

public class Musician implements Serializable, Cloneable {

    private String id;
    private String name;
    private String lastAlbum;
    private String lastSingle;
    private String image;
    private boolean news;

    public Musician(String id, String name, String image) {
        this.id = id;
        this.name = name;
        lastAlbum = "";
        lastSingle = "";
        this.image = image;
        news = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastAlbum() {
        return lastAlbum;
    }

    public String getLastSingle() {
        return lastSingle;
    }

    public String getImage() {
        return image;
    }

    public void setLastAlbum(String lastAlbum) {
        this.lastAlbum = lastAlbum;
    }

    public void setLastSingle(String lastSingle) {
        this.lastSingle = lastSingle;
    }

    public void news() {
        news = !news;
    }

    public boolean hasNews() {
        return news;
    }

    @Override
    public Musician clone() {
        Musician musician = null;
        try {
            musician = (Musician)super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Failed to clone: " + e);
        }
        return musician;
    }
}