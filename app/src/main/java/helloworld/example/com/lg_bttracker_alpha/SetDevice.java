package helloworld.example.com.lg_bttracker_alpha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class SetDevice extends Activity {

    private String device_address;
    private String device_name;
    private EditText userDevice;

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    final static String dbName = "c.db"; //내가 지정한 database 이름.
    final static int dbVersion = 1;

    public Context now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_device);

        userDevice = (EditText)findViewById(R.id.user_device);

        now = getApplicationContext();

        final Intent intent = getIntent();
        device_address = intent.getStringExtra("deviceAddress");

    }

    public void setCancel(View v){

        startActivity(new Intent(this, BT_List.class));
        this.finish();
    }

    public void letSet(View v){
        device_name = userDevice.getText().toString();

        dbHelper = new DBHelper(now, dbName, null, dbVersion);
        db = dbHelper.getWritableDatabase();

        //중복체크
        String sql = "SELECT * FROM BTtrackers WHERE device_address='"+device_address+"';";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.getCount() == 0){ //첫 등록시
            String add_sql = "INSERT INTO BTtrackers VALUES(NULL,'"+ device_name+"','"+device_address+"','true');";
            db.execSQL(add_sql);
        }

        startActivity(new Intent(this, BT_List.class));
        this.finish();
    }
}