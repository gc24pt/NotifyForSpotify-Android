package user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

public class User implements Serializable {

    private Map<String, Musician> map;
    private SizedStackClass<Musician> latestNews;

    public User() {
        resetMap();
        latestNews = new SizedStackClass<>(15);
    }

    public void resetMap() {
        map = new HashMap<>();
    }

    public void addArtist(String id, Musician musician) {
        map.put(id, musician);
    }

    public void addLN(Musician musician) {
        latestNews.push(musician);
    }

    public Musician getMusician(String id) {
        return map.get(id);
    }

    public Iterator mapIterator() {
        return map.entrySet().iterator();
    }

    public ListIterator<Musician> stackIterator() {
        return latestNews.listIterator(latestNews.size());
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int aNum() {
        return map.size();
    }

    public void clearNews() {
        latestNews.clear();
    }
}