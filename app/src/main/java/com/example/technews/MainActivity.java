package com.example.technews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String s="";
    ArrayList<String> IDs = new ArrayList<String>();
    ArrayList<String> Names = new ArrayList<String>();
    ArrayList<String> URLs = new ArrayList<String>();

    ListView listView;

public void openActivity2(String url){
    Intent intent = new Intent(this, MainActivity2.class);
    intent.putExtra("url", url);
    startActivity(intent);
}

    public class DownloadTaskJSON extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls){
            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            }catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String s){
            super.onPostExecute(s);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            SQLiteDatabase db = this.openOrCreateDatabase("mobiledb", MODE_PRIVATE, null);
//            db.execSQL("DROP TABLE websites");
            db.execSQL("CREATE TABLE IF NOT EXISTS websites (name VARCHAR, html VARCHAR)");
            Cursor c = db.rawQuery("Select * from websites", null);
            c.moveToFirst();

//            db.execSQL("delete from websites");

            if(c.getCount()>0){
                Log.i("data","available");

                int nameindex = c.getColumnIndex("name");
                int htmlindex = c.getColumnIndex("html");
                c.moveToFirst();

                while (c.getPosition() < c.getCount()) {
                    Names.add( c.getString(nameindex));
                    URLs.add( c.getString(htmlindex));
                    Log.i("in",Integer.toString(c.getPosition()));
                    c.moveToNext();
                }

            }else{

            String apiIdURL = "https://hacker-news.firebaseio.com/v0/topstories.json";

            DownloadTaskJSON GetID = new DownloadTaskJSON();


            try {
                String str_result = GetID.execute(apiIdURL).get();

                Pattern p = Pattern.compile(",(.*?),");
                Matcher m = p.matcher(str_result);
                int i = 0;

                while (m.find() && i < 20) {
                    if (m.group(1).equals("29251357") || m.group(1).equals("29252974") || m.group(1).equals("29251343") || m.group(1).equals("29254063") || m.group(1).equals("29253277") || m.group(1).equals("29251798") || m.group(1).equals("29254236") || m.group(1).equals("29255283")|| m.group(1).equals("29281468")) {
                        Log.i("found", "wrong");
                        //does not contain URL
                    } else {
                        IDs.add(m.group(1));
                        i++;
                    }
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < IDs.size(); i++) {
                String url = "https://hacker-news.firebaseio.com/v0/item/" + IDs.get(i) + ".json?print=pretty";
                Log.i("Url", url);
                DownloadTaskJSON GetJSON = new DownloadTaskJSON();

                try {
                    String Json = GetJSON.execute(url).get();

                    JSONObject json = new JSONObject(Json);
                    String URL = json.getString("url");
                    String name = json.getString("by");
                    URLs.add(URL) ;
                    Names.add(name);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

//            for (int i = 0; i < IDs.size(); i++) {
//
//            DownloadTaskJSON GetHtml = new DownloadTaskJSON();
//            String Html = null;
//            try {
//                Log.i("url", URLs.get(i));
//                Htmls.add(GetHtml.execute(URLs.get(i)).get());
//
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//                Log.i("html",Htmls.get(i));
//                Log.i("index",Integer.toString(i));
//
//        }
            for (int i = 0; i < IDs.size(); i++) {
                String n= Names.get(i);
                String u= URLs.get(i);
                db.execSQL("INSERT INTO websites(name,html) VALUES('"+n+"','"+u+"')");

            }
            }

            listView = (ListView) findViewById(R.id.listview);

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Names);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    openActivity2(URLs.get(i));
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}