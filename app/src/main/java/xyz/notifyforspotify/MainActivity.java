package xyz.notifyforspotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PagingCursorbased;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import user.*;


public class MainActivity extends AppCompatActivity {

    private static final String ASL = "FOLLOWED ARTISTS SUCCESSFULLY LOADED";
    private static final String NAS = "NO ARTISTS FOLLOWED ON SPOTIFY";

    private static String token;
    private static User user;
    private static OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        this.getWindow().setStatusBarColor(Color.parseColor("#212121"));

        Bundle b = getIntent().getExtras();
        token = b.getString("token");
        user = load(getApplicationContext());
        mOkHttpClient = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean("isFirstRun", false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("isFirstRun", Boolean.TRUE);
            edit.apply();
            update();
        }

        Button uButton = findViewById(R.id.update);
        uButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
                Fragment cf = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(cf instanceof ArtistsFragment)
                    ((ArtistsFragment) cf).dataPrinter();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new NewsFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_news:
                            selectedFragment = new NewsFragment();
                            break;
                        case R.id.nav_artists:
                            selectedFragment = new ArtistsFragment();
                            break;
                        case R.id.nav_help:
                            selectedFragment = new HelpFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onPause() {
        super.onPause();
        save(getApplicationContext());
    }

    private static void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("save", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(user);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("ERROR SAVING");
        }
    }

    private static User load(Context context) {
        User user;
        try {
            FileInputStream fis = context.openFileInput("save");
            ObjectInputStream is = new ObjectInputStream(fis);
            user = (User) is.readObject();
            is.close();
            fis.close();
            return user;
        } catch (IOException | ClassNotFoundException e) {
            user = new User();
        }
        return user;
    }

    //Get followed artists (w/Thread)
    private void update() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                final Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/me/following?type=artist&limit=50")
                        .addHeader("Authorization","Bearer " + token)
                        .build();
                Call mCall = mOkHttpClient.newCall(request);
                try {
                    Response response = mCall.execute();
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        final PagingCursorbased<Artist> artistPagingCursorbased = uExecute(jsonObject.toString());
                        Artist[] artists = artistPagingCursorbased.getItems();
                        user.resetMap();
                        for (int i = 0; i < artists.length; i++)
                            createArtist(user, artists[i]);
                    } catch (JSONException e) {
                        System.out.println("Failed to parse data: " + e);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to fetch data: " + e);
                }
            }
        });
        thread.start();
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupt Occurred");
        }
        update2();
    }

    private PagingCursorbased<Artist> uExecute(String json) {
        return (new Artist.JsonUtil()).createModelObjectPagingCursorbased(json, "artists");
    }

    private void createArtist(User user, Artist artist) {
        String id = artist.getId();
        String name = artist.getName();
        Image[] images = artist.getImages();
        String image = images[0].getUrl();
        Musician musician = new Musician(id, name, image);
        user.addArtist(id, musician);
    }

    //Get artists' latest album and single
    private void update2() {
        if(user.isEmpty()) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), NAS, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        else {
            final CountDownLatch countDownLatch = new CountDownLatch(user.aNum());
            Iterator it = user.mapIterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                final Musician musician = (Musician) pair.getValue();
                final String id = musician.getId();
                final Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/artists/" + id + "/albums?offset=0&include_groups=album,single&limit=50")
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                Call mCall = mOkHttpClient.newCall(request);
                mCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println("Failed to fetch data: " + e);
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject jsonObject = new JSONObject(response.body().string());
                            final Paging<AlbumSimplified> albumSimplifiedPaging = rExecute(jsonObject.toString());
                            AlbumSimplified[] albums = albumSimplifiedPaging.getItems();
                            dataHandler(albums, id);
                            countDownLatch.countDown();
                        } catch (JSONException e) {
                            System.out.println("Failed to parse data: " + e);
                        }
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), ASL, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private Paging<AlbumSimplified> rExecute(String json) {
        return (new AlbumSimplified.JsonUtil()).createModelObjectPaging(json);
    }

    private void dataHandler(AlbumSimplified[] albums, String id) {
        Musician musician = user.getMusician(id);
        String name = albums[0].getName();
        if (albums[0].getAlbumType().toString().equals("SINGLE"))
            musician.setLastSingle(name);
        else {
            musician.setLastAlbum(name);
            int stop = 0;
            for (int i = 1; i < albums.length && stop == 0; i++) {
                if (albums[i].getAlbumType().toString().equals("SINGLE")) {
                    musician.setLastSingle(albums[i].getName());
                    stop = 1;
                }
            }
        }
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}