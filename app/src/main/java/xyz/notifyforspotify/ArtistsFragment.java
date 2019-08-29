package xyz.notifyforspotify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import user.*;


public class ArtistsFragment extends Fragment {

    private RecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout l = (RelativeLayout) inflater.inflate(R.layout.fragment_artists, container, false);
        return l;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = initRecyclerView();
        dataPrinter();
    }

    private RecyclerViewAdapter initRecyclerView() {
        ArrayList<String> emptyInfo = new ArrayList<>();
        ArrayList<String> emptyImages = new ArrayList<>();
        ArrayList<String> emptyIcons = new ArrayList<>();
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view1);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), emptyInfo, emptyImages, emptyIcons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return adapter;
    }

    public void dataPrinter() {
        User user = ((MainActivity)getActivity()).getUser();
        adapter.reset();
        Iterator it = user.mapIterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            final Musician musician = (Musician) pair.getValue();
            final String icon = "https://i.imgur.com/NiibAMx.png";
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