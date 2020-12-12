package com.example.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class Chicken extends AppCompatActivity {

    private String TAG = Chicken.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView stepsLV;
    int rID;
    ArrayList<HashMap<String, Object>> stepsList;
    public static final String EXTRA_MESSAGE ="com.example.testapp.extra.MESSAGE";
    private String detailsText;
    String recipeName;
    private int stepsDone = 0;
//    TextView status;
//    String statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chicken);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recipeName = getIntent().getExtras().getString("name");
        rID = getIntent().getExtras().getInt("recipeId");
        Log.e("the ID", String.valueOf(rID));

        stepsList = new ArrayList<>();
        stepsLV = (ListView)findViewById(R.id.stepList);
/*
        int completedStepNum = getIntent().getExtras().getInt("stepNum");
        String activity = getIntent().getExtras().getString("class");
        Log.d("intent class", activity);

        if(activity.equals("Details")){
            View v = stepsLV.getChildAt(completedStepNum -
                    stepsLV.getFirstVisiblePosition());

            Log.d("intent class2", activity);
            if(v == null) return;

            status = v.findViewById(R.id.status);
            status.setText(statusText);
            Log.d("intent class sts", statusText);

        }
*/
        new GetSteps().execute();
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("intent class code", String.valueOf(resultCode));

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                statusText = data.getStringExtra("status");
            }
        }
    }
*/
    private class GetSteps extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Chicken.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String stepsStr = getStepsForRecipe(rID);

            if (stepsStr != null) {
                try {
                    JSONArray steps = new JSONArray(stepsStr);

                    // looping through All Steps
                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject c = steps.getJSONObject(i);

                        String id = c.getString("StepOrder");
                        String sum = c.getString("Summary");
                        String det = c.getString("details");

                        // tmp hash map for single recipe
                        HashMap<String, Object> stepDetails = new HashMap<>();

                        // adding each child node to HashMap key => value
                        stepDetails.put("StepOrder", id);
                        stepDetails.put("Summary", sum);
                        stepDetails.put("details", det);

                        // adding recipe to recipe list
                        stepsList.add(stepDetails);
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
                    Chicken.this, stepsList,
                    R.layout.step_item, new String[]{"StepOrder", "Summary"}, new int[]{R.id.stepID,
                    R.id.summary});

            stepsLV.setAdapter(adapter);
            Log.e("adapter set", "adapter set");

            stepsLV.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int id , long l) {
                    Log.e("bla","bla");
                    stepsDone++;
                    String temp = "Steps Done " + String.valueOf(stepsDone);
                    TextView tv1 = (TextView)findViewById(R.id.totalsteps);
                    String details =  String.valueOf(stepsList.get(id).get("details"));


                    // Invoke new activity
                    Intent intent2 = new Intent(Chicken.this, Details.class);
                    intent2.putExtra("details", details);
                    intent2.putExtra("position", id+1);
                    intent2.putExtra("name", recipeName);
                    tv1.setText(temp);
                    startActivity(intent2);
//                    startActivityForResult(intent2, 1);
                    //finish();
                }
            });
        }

    } // GetSteps

    public String getStepsForRecipe(int rID){
        HttpClient client = new DefaultHttpClient();
        String recipeID = Integer.toString(rID);
        StringBuilder sb = new StringBuilder();
        HttpPost post = new HttpPost("http://comet.cs.brynmawr.edu/~zainabb/hw6-360/config2.php?recipeID=" + recipeID);
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("stepNum",
                    "summary"));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
//                System.out.println(line);
                Log.d("chicken",line);
                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("bla2", "bla2");
        return sb.toString();
    }

    //to do: depending on listview clicked here, use that number to get rleevant hashmap from array stepsList
    //then get details from that hashmap
    //use that to populate third screen
    //alternately can create a new table for step_details then do it exactly like second screen
}
