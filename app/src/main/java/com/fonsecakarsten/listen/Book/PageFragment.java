package com.fonsecakarsten.listen.Book;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fonsecakarsten.listen.R;

/**
 * Created by Karsten on 7/23/2017.
 */

public class PageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_view, container, false);
        Bundle args = getArguments();
        TextView pageText = rootView.findViewById(R.id.pageTextView);
        pageText.setText(args.getString("PageText"));
        pageText.setMovementMethod(new ScrollingMovementMethod());
        return rootView;
    }
}
