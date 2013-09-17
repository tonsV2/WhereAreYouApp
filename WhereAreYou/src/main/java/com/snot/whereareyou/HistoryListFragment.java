package com.snot.whereareyou;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;


import com.snot.whereareyou.database.History;
import com.snot.whereareyou.database.Provider;

public class HistoryListFragment extends ListFragment {


    public HistoryListFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new SimpleCursorAdapter(getActivity(),
            android.R.layout.simple_list_item_2,
            null,
	    //TODO: phone number to name...
            new String[] { History.COL_PHONE_NUMBER, History.COL_TIMESTAMP },
            new int[] { android.R.id.text1, android.R.id.text2 },
            0));

        // Load the content
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(), Provider.URI_HISTORYS, History.FIELDS, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
                ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
            }
        });
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
	// get cursor
	Cursor c = ((SimpleCursorAdapter)list.getAdapter()).getCursor();
	// move to the desired position
	c.moveToPosition(position);
	// pass it to our history object
	History history = new History(c);
	// create geo uri
	Uri uri = Uri.parse("geo:0,0?q=" + history.latitude + "," + history.longitude + "&z=10");
	// create intent
	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	// launch intent
	startActivity(intent);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//	inflater.inflate(R.menu.exercise_list, menu);
//    }
}

