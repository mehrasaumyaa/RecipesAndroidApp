package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://comet.cs.brynmawr.edu/~zainabb/hw6-360/config.php";

    ArrayList<HashMap<String, Object>> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        new GetRecipes().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetRecipes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            //String jsonStr = "[{\"RecipeID\":\"1\",\"MealType\":\"Main\",\"Servings\":\"6\",\"ImageLink\":null,\"name\":\"Enchiladas chicken\"},{\"RecipeID\":\"2\",\"MealType\":\"Main\",\"Servings\":\"4\",\"ImageLink\":null,\"name\":\"Tarragon Peach and Pork Kabobs\"},{\"RecipeID\":\"3\",\"MealType\":\"Appetizer\",\"Servings\":\"4\",\"ImageLink\":null,\"name\":\"Glass Noodel salad\"},{\"RecipeID\":\"4\",\"MealType\":\"Appetizer\",\"Servings\":\"4\",\"ImageLink\":null,\"name\":\"Slow-Cooker Tomato Soup\"},{\"RecipeID\":\"5\",\"MealType\":\"Main\",\"Servings\":\"2\",\"ImageLink\":null,\"name\":\"Veal Osso Buco\"}]";

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray recipes = new JSONArray(jsonStr);

                    // Getting JSON Array node

                    // looping through All Recipes
                    for (int i = 0; i < recipes.length(); i++) {
                        JSONObject c = recipes.getJSONObject(i);

                        String id = c.getString("RecipeID");
                        String name = c.getString("name");
                        String mealType = "Meal Type: "+ c.getString("MealType");
                        String servings = "Servings: " + c.getString("Servings");
                        //String url_string = "https://source.unsplash.com/random";
                        String url_string = c.getString("ImageLink");

                        //convert to bitmap object then add that to recipe then create adapter
                        InputStream in =null;
                        Bitmap bmp=null;
                        int responseCode = -1;
                        try{

                            URL url = new URL(url_string);//"http://192.xx.xx.xx/mypath/img1.jpg
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setDoInput(true);
                            con.connect();
                            responseCode = con.getResponseCode();
                            if(responseCode == HttpURLConnection.HTTP_OK)
                            {
                                //download
                                in = con.getInputStream();
                                bmp = BitmapFactory.decodeStream(in);
                                in.close();
                            }

                        }
                        catch(Exception ex){
                            Log.e("Exception",ex.toString());
                        }

                        // tmp hash map for single recipe
                        HashMap<String, Object> recipe = new HashMap<>();

                        // adding each child node to HashMap key => value
                        recipe.put("RecipeId", id);
                        recipe.put("name", name);
                        recipe.put("MealType", mealType);
                        recipe.put("Servings", servings);
                        recipe.put("Image", bmp);

                        // adding recipe to recipe list
                        recipeList.add(recipe);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ExtendedSimpleAdapter adapter = new ExtendedSimpleAdapter(
                    MainActivity.this, recipeList,
                    R.layout.list_item, new String[]{"name", "MealType",
                    "Servings", "Image"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile, R.id.dish_image});

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int id , long l) {
                    Log.e("click in main", "clikc in main");
                    String strId =  String.valueOf(recipeList.get(id).get("RecipeId"));
                    int recipeId = Integer.parseInt(strId);

                    String name = String.valueOf(recipeList.get(id).get("name"));


                    /*switch(id) {
                        case 0:
                            Intent chicken = new Intent(MainActivity.this, Chicken.class);
                            chicken.putExtra("recipeId", recipeId);
                            startActivity(chicken);
                            break;
                        case 1:
                            Intent kebobs = new Intent(MainActivity.this, Kebobs.class);
                            startActivity(kebobs);
                            break;
                        case 2:
                            Intent noodle = new Intent(MainActivity.this, Noodle.class);
                            startActivity(noodle);
                            break;
                        case 3:
                            Intent soup = new Intent(MainActivity.this, Soup.class);
                            startActivity(soup);
                            break;
                        case 4:
                            Intent veal = new Intent(MainActivity.this, Veal.class);
                            startActivity(veal);
                            break;
                    }*/

                    // Invoke new activity
                    Intent intent = new Intent(MainActivity.this, Chicken.class);
                    intent.putExtra("recipeId", recipeId);
                    intent.putExtra("name", name);
//                    intent.putExtra("class", "Main");
                    startActivity(intent);
                    //finish();
//                    Toast.makeText(MainActivity.this, recipeId, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}