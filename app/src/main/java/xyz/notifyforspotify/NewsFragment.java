package xyz.notifyforspotify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import user.*;


public class NewsFragment extends Fragment {

    private static final String NA = "FOLLOW SOME ARTISTS ON SPOTIFY AND CLICK ON THE 'UPDATE ARTISTS' BUTTON";
    private static final String NN = "NOTHING NEW";

    private static boolean hasNews = false;

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private static User user;
    private RecyclerViewAdapter adapter;
    private SwipeRefreshLayout pullToRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout l = (RelativeLayout) inflater.inflate(R.layout.fragment_news, container, false);
        return l;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pullToRefresh = getView().findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                pullToRefresh.setRefreshing(false);
            }
        });
        adapter = initRecyclerView();
        user = ((MainActivity)getActivity()).getUser();
        dataPrinter();
    }

    private RecyclerViewAdapter initRecyclerView() {
        ArrayList<String> emptyInfo = new ArrayList<>();
        ArrayList<String> emptyImages = new ArrayList<>();
        ArrayList<String> emptyIcons = new ArrayList<>();
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), emptyInfo, emptyImages, emptyIcons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return adapter;
    }

    private void refresh() {
        user = ((MainActivity)getActivity()).getUser();
        if (user.isEmpty()) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), NA, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        else {
            String token = ((MainActivity)getActivity()).getToken();
            final CountDownLatch countDownLatch = new CountDownLatch(user.aNum());
            adapter.reset();
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
                            if(response.code() == 200) {
                                final JSONObject jsonObject = new JSONObject(response.body().string());
                                final Paging<AlbumSimplified> albumSimplifiedPaging = rExecute(jsonObject.toString());
                                AlbumSimplified[] albums = albumSimplifiedPaging.getItems();
                                dataHandler(albums, id);
                                if (musician.hasNews()) {
                                    Musician musician1 = musician.clone();
                                    user.addLN(musician1);
                                    musician.news();
                                }
                            }
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
            dataPrinter();
            if(!hasNews) {
                Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), NN, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            else
                hasNews = false;
        }
    }

    private Paging<AlbumSimplified> rExecute(String json) {
        return (new AlbumSimplified.JsonUtil()).createModelObjectPaging(json);
    }

    private void dataHandler(AlbumSimplified[] albums, String id) {
        Musician musician = user.getMusician(id);
        String currentLastSingle = musician.getLastSingle();
        String name = albums[0].getName();
        if (albums[0].getAlbumType().toString().equals("SINGLE")) {
            musician.setLastSingle(name);
            if (!currentLastSingle.equals(name))
                musician.news();
        }
        else {
            String currentLastAlbum = musician.getLastAlbum();
            musician.setLastAlbum(name);
            int stop = 0;
            for (int i = 1; i < albums.length && stop == 0; i++) {
                if (albums[i].getAlbumType().toString().equals("SINGLE")) {
                    musician.setLastSingle(albums[i].getName());
                    stop = 1;
                }
            }
            if (!currentLastAlbum.equals(name) || !currentLastSingle.equals(musician.getLastSingle()))
                musician.news();
        }
    }

    private void dataPrinter() {
        ListIterator<Musician> it = user.stackIterator();
        while(it.hasPrevious()) {
            final Musician musician = it.previous();
            final String icon;
            if (musician.hasNews()) {
                icon = "https://i.imgur.com/5STz2E4.png";
                musician.news();
                hasNews = true;
            }else {
                icon = "https://i.imgur.com/NiibAMx.png";
            }
            final String info = musician.getName() + "\n\n" + "LAST ALBUM: " + musician.getLastAlbum() + "\n" + "LAST SINGLE: " + musician.getLastSingle();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(info, musician.getImage(), icon);
                }
            });
        }
    }
}