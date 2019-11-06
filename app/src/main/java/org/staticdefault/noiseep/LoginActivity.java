package org.staticdefault.noiseep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    public static String getResponseFromUrl(String urlPath) {
        String fullString = "";
        try {
            URL url = new URL(urlPath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    fullString += line;
                }
            }catch (Exception e){

            }
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullString;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editId = (EditText) findViewById(R.id.editId);
        final EditText editPw = (EditText) findViewById(R.id.editPw);

        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (new Thread
                    (new Runnable() {
                        @Override
                        public void run() {

                            final String content = getResponseFromUrl("http://guruem82.dothome.co.kr/Login_Select.php?id=" + editId.getText() + "&pw=" + editPw.getText());


                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    if(content.indexOf("complete") != -1){

                                        final int LOGINED_ID = Integer.valueOf(editId.getText().toString());
                                        LocalData.initialize(LoginActivity.this);
                                        LocalData.edit(new LocalData.LocalDataRunnable() {
                                            @Override
                                            public void run(SharedPreferences.Editor editor) {
                                                editor.putInt("ID", LOGINED_ID);
                                            }
                                        });
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "정보가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                                    }


                                }
                            });


                        }
                    })
                ).start();

            }
        });




    }


}
