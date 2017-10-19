package com.james.animalsallhome;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;


public class AnimalActivity extends AppCompatActivity {
    private GridView mGridView;
    private BottomNavigationView navigation;
    private ProgressBar mProgressBar;
    private ImageAdapterGridView mGridAdapter;
    private String TAG = AnimalActivity.class.getSimpleName();
    private String result[] = new String[6];
    private ArrayList<Animals> mGridData;
    private Animals animals;
    private String ANIMAL_URL = "http://data.coa.gov.tw/Service/OpenData/AnimalOpenData.aspx?$filter=";
    private String WEB_URL = "http://163.29.36.110/html/Aml_animalCon.aspx?Aid=";
    private String apiUrlAll;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }

    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //goToHome();
        overridePendingTransition(R.anim.slide_in_right_1, R.anim.slide_in_right_2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        result = getActivityValue();
        Log.e(TAG, ANIMAL_URL +"animal_place+like+"+ result[0] +"+and+animal_kind+like+"+result[1]+"+and+animal_sex+like+" +result[2] +"+and+animal_age+like+"+ result[3]);
        apiUrlAll = ANIMAL_URL +"animal_place+like+"+ result[0] +"+and+animal_kind+like+"+result[1]+"+and+animal_sex+like+" +result[2] +"+and+animal_age+like+"+ result[3];

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setVisibility(View.GONE);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("F618803C89E1614E3394A55D5E7A756B").build(); //Nexus 5
        mAdView.loadAd(adRequest);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridData = new ArrayList<>();
        mGridAdapter = new ImageAdapterGridView(this, R.layout.grid_item, mGridData);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                animals = mGridData.get(position);
                openWebView(WEB_URL + animals.getWebId() + "&Tid=" + animals.getTid(), animals.getName());
                overridePendingTransition(R.anim.slide_in_left_1, R.anim.slide_in_left_2);
                //Toast.makeText(AnimalActivity.this, animals.getTid()+" . " + animals.getWebId(), Toast.LENGTH_SHORT).show();
            }
        });
            new AsyncHttpTask().execute(apiUrlAll);

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void openWebView(String url, String name) {
        Intent intentWV = new Intent(AnimalActivity.this, WebViewActivity.class);
        intentWV.putExtra("URL", url);
        intentWV.putExtra("name", animals.getName());
        intentWV.putExtra("acceptnum", animals.getAcceptnum());
        intentWV.putExtra("type", result[0]);
        startActivity(intentWV);
        overridePendingTransition(R.anim.slide_in_left_1, R.anim.slide_in_left_2);
    }


    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            Integer result = 0;
            getData(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                //Toast.makeText(AnimalActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mGridData.clear();
        }
    }

    public void getData(String url) {
        try {
            String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
            if (json.indexOf("{")!=-1) {
                String output = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
                Log.e(TAG, " json output : " + output);
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String place = jsonObject.getString("animal_place");
                    String pic = jsonObject.getString("album_file");
                    String tid = jsonObject.getString("animal_subid");
                    String acceptnum = jsonObject.getString("shelter_name");
                    String webid = jsonObject.getString("animal_update");
                    Log.e(TAG,place);
                    Log.e(TAG,pic);
                    Log.e(TAG,tid);
                    Log.e(TAG,acceptnum);
                    Log.e(TAG,webid);
                    mGridData.add(new Animals(place, pic, tid, acceptnum, webid));
                }
            }else{
                mGridData.add(new Animals("此頁面沒有搜尋到寵物資訊", "無", "無", "無", "無"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String[] getActivityValue() {
        Intent i = getIntent();
        result[0] = i.getStringExtra("area");
        result[1] = i.getStringExtra("type");
        result[2] = i.getStringExtra("sex");
        result[3] = i.getStringExtra("age");
        return result;
    }

    public void goToHome() {
        Intent i = new Intent(AnimalActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
