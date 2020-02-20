package xyz.notifyforspotify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import user.*;


public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView v = (ScrollView) inflater.inflate(R.layout.fragment_help, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        infoPrinter();

        Button cButton = getView().findViewById(R.id.clear);
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = ((MainActivity)getActivity()).getUser();
                user.clearNews();
            }
        });
    }

    private void infoPrinter() {
        User user = ((MainActivity)getActivity()).getUser();
        TextView tv = getView().findViewById(R.id.tv);
        String info1 = "Followed Artists: " + user.aNum() + "/50";
        String heading = "How Notify for Spotify works:";
        String step1 = "1. Follow up to 50 artists on Spotify";
        String step2 = "2. Press the 'UPDATE ARTISTS' button at the top of the screen";
        String step3 = "3. Swipe down the 'News' tab to check for news";
        String info2 = "If you have any trouble using the app, please email us at royalsolutionshelp@gmail.com";
        tv.setText(info1 + "\n" + "\n" + heading + "\n" + step1 + "\n" + step2 + "\n" + step3 + "\n" + "\n" + info2);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }
}
