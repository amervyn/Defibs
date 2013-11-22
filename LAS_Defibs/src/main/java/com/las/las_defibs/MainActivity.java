package com.las.las_defibs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //private final static String DEFIB_SERVICE_URI = "http://hq-orion:404/DefibLocationsWS/DefibService.svc/GetNearestDefibsJson?";
    private final static String DEFIB_SERVICE_URI = "http://defib.londonambulance.nhs.uk:1534/GetNearestDefibs.ashx?";
    private final static String CURRENT_LAT="51.15";
    private final static String CURRENT_LNG="-0.19";

    private static TableLayout rTable;

    private static Context cont=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // Toast.makeText(getApplicationContext(),"test",100);
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        rTable=(TableLayout)findViewById(R.id.table_layout);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private android.os.Handler handler=new android.os.Handler();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Main".toUpperCase(l);
                case 1:
                    return "Map View".toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "1";

        //GPSTracker class
        GPSTracker gps;

        MyLocation.LocationResult locresult;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Button btn= (Button) rootView.findViewById(R.id.btnFind);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    //StrictMode.setThreadPolicy(policy);
                    //Toast.makeText(cont.getApplicationContext(),"test",1000).show();

                    String lat="";
                    String lng="";

                    try
                    {

                        locresult=new MyLocation.LocationResult(){

                            @Override
                            public void gotLocation(Location location)
                            {
                                String lt=String.valueOf(location.getLatitude());
                                String ln=String.valueOf(location.getLongitude());
                                //lt="52.3";
                                //ln="-0.19";
                                String link=DEFIB_SERVICE_URI + "lat=" + lt + "&lng=" + ln;
                                new DownloadDefibs().execute(new String[] {link});
                            }

                        };

                        /*String lt="52.3";
                        String ln="-0.19";
                        String link=DEFIB_SERVICE_URI + "lat=" + lt + "&lng=" + ln;
                        new DownloadDefibs().execute(new String[] {link});*/

                        /*//check if GPS enabled
                        if(gps.canGetLocation())
                        {
                            double latitude=gps.getLatitude();
                            double longitude=gps.getLongitude();
                            lat=String.valueOf(latitude);
                            lng=String.valueOf(longitude);
                        }
                        */

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }


                }
            });

            return rootView;
        }


        private class DownloadDefibs extends AsyncTask<String, Void, JSONArray> {

            private ProgressDialog dialog;

            @Override
            protected JSONArray doInBackground(String... url) {

                StrictMode.ThreadPolicy policy = new StrictMode.
                        ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                JSONArray jsonArray=null;
                //StringBuilder strings=new StringBuilder();
                try{

                    String link=url[0];
                    DefaultHttpClient client=new DefaultHttpClient();
                    HttpGet request=new HttpGet(link);
                    request.setHeader("Accept","application/json");
                    request.setHeader("Content-type","application/json");

                    try {
                        HttpResponse response=client.execute(request);
                        HttpEntity entity=response.getEntity();
                        //Toast.makeText(cont.getApplicationContext(),entity.toString(),1000).show();
                        if(entity!=null && entity.getContentLength()!=0)
                        {

                            Reader defibsreader=new InputStreamReader(response.getEntity().getContent());
                            char[] buffer=new char[(int) response.getEntity().getContentLength()];
                            defibsreader.read(buffer);
                            defibsreader.close();

                            //Toast.makeText(cont.getApplicationContext(),buffer.toString(),1000).show();

                            try {
                                JSONObject defibs=new JSONObject(new String(buffer));
                                jsonArray=(JSONArray) defibs.get("GetNearestDefibsJsonResult");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(getActivity(),"Error connecting: " + e.toString(),3000).show();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getActivity(),"Error connecting: " + e.toString(),3000).show();
                }

                return jsonArray;

            }


            protected void onPreExecute() {
                //Context cont2=getActivity().getApplicationContext();
                dialog=ProgressDialog.show(getActivity(),"Processing","Getting data...",true);
            }


            @Override
            protected void onPostExecute(JSONArray result) {

                TableLayout tbl1=(TableLayout) getActivity().findViewById(R.id.table_layout);
                tbl1.removeAllViews();
                //Toast.makeText(getActivity(), "",1000).show();
                if(result!=null)
                {
                    //Toast.makeText(getActivity(), result.toString(),1000).show();
                    for(int i=0;i<result.length();i++)
                    {
                        try {

                            JSONObject obj=new JSONObject(result.getString(i));

                            //Toast.makeText(cont.getApplicationContext(),result.getString(i),1000).show();

                            TableRow row=new TableRow(getActivity());
                            row.setLayoutParams(new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            ));

                            TextView textView1=new TextView(getActivity());
                            textView1.setText(obj.getString("address") + "\n");
                            textView1.setTextSize(10);
                            textView1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
                            textView1.setTextColor(Color.BLUE);

                            row.addView(textView1);
                            tbl1.addView(row, new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT
                            ));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),e.toString(),2000).show();
                        }

                    }

                    dialog.dismiss();

                }

            }

        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            cont=getActivity();

            //gps=new GPSTracker(getActivity());

            try
            {

                MyLocation myLocation=new MyLocation();
                myLocation.getLocation(getActivity(),locresult);

            } catch (Exception ex)
            {
                ex.printStackTrace();
                Toast.makeText(getActivity(),ex.getMessage().toString(),2000).show();
            }


        }
    }

}
