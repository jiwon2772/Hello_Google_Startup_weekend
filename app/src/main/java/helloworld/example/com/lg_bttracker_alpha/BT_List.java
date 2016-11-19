package helloworld.example.com.lg_bttracker_alpha;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BT_List extends AppCompatActivity {

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private String sql;
    private Cursor cursor;
    private String table_name = "BTtrackers";

    Intent alarmIntent = null;
    private boolean isDoing = false;

    final static String dbName = "c.db"; //내가 지정한 database 이름.
    final static int dbVersion = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean flag = false;
    private ProgressBar bar;
    private ImageButton btn;
    private TextView dName;
    private TextView dAddress;

    private String mDevice;
    private String mAddress;

    private Handler mHandler;
    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register__bt);

        bar = (ProgressBar) findViewById(R.id.progressBar);
        btn = (ImageButton) findViewById(R.id.plusBtn);
        dName = (TextView) findViewById(R.id.nameLocation);
        dAddress = (TextView) findViewById(R.id.addressLocation);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        dbHelper = new DBHelper(this, dbName, null, dbVersion);
        db = dbHelper.getWritableDatabase();
        sql = "SELECT * FROM " + table_name + ";";

        cursor = db.rawQuery(sql, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            dName.setText(cursor.getString(1));
            dAddress.setText(cursor.getString(2));

            mDevice=cursor.getString(1);
            mAddress=cursor.getString(2);

            btn.setVisibility(View.GONE);
            new ProgressTask().execute();
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // 몇초 후에 정지 없음
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            });
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mScanning) {
                        //backgroundTask.cancel(true);
                    } else {
                        if (device.getAddress().equals(mAddress) && !isDoing) {
                            isDoing = true;
                            alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
                            startActivityForResult(alarmIntent,222);
                        }
                    }
                }
            });
        }
    };

    private class ProgressTask extends AsyncTask<Void,Void,Integer> {
        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... arg0) {
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            bar.setVisibility(View.VISIBLE);
        }
    }

    public void registerBT(View v) {
        startActivity(new Intent(getApplication(), Register_BT.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch(requestCode) {
                case(222): {
                    //핸드폰을 찾아서 종료된 경우
                    if(resultCode == Activity.RESULT_OK) {
                        isDoing = false;
                        alarmIntent = null;
                        Log.d("FIND RESULT: ","isDoing-" + isDoing);
                    }
                    else {
                        Log.d("INTENT_COMMUNICATION", "failed");
                    }
                    break;
                }
            }
        }
        catch(Exception e) {
        }
    }
}