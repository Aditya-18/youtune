package com.example.admin.gui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button bt_open,bt_record;
    String FilePath;
    Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_open = (Button) findViewById(R.id.bt_open);

        // add a onclick listner to button here
        bt_open.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getDownloadCacheDirectory().getPath().toString());
                chooser.addCategory(Intent.CATEGORY_OPENABLE);
                chooser.setDataAndType(uri, "gagt/sdf");

                try {
                    startActivityForResult(chooser, 2);

                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        bt_record = (Button) findViewById(R.id.bt_record);

        // add a onclick listner to button here
        bt_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Audio_Record.class);
                i.putExtra("msg","abc");
                startActivity(i);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Fix no activity available
        if (data == null)
            return;
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    FilePath = data.getData().getPath();
                    Toast.makeText(MainActivity.this, FilePath, Toast.LENGTH_SHORT).show();
                    Log.i("Adress is: ", FilePath);

                    i = new Intent(MainActivity.this, xy_graph.class);
                    i.putExtra("data", FilePath);
                    startActivity(i);
                }
        }

    }
}
