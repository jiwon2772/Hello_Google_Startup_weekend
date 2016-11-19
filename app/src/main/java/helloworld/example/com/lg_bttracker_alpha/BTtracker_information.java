package helloworld.example.com.lg_bttracker_alpha;

    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.bluetooth.BluetoothAdapter;
    import android.bluetooth.BluetoothDevice;
    import android.bluetooth.BluetoothManager;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.media.AudioManager;
    import android.media.MediaPlayer;
    import android.os.AsyncTask;
    import android.os.Handler;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.Toast;

    @SuppressLint("NewApi")
    public class BTtracker_information extends Activity {

        private BluetoothAdapter mBluetoothAdapter;
        private MediaPlayer mp = null;
        AudioManager am = null;
        private boolean mScanning;
        private Handler mHandler;
        //private boolean isDoing = false;
        // 등록된 비콘 Mac Address

        Intent alarmIntent = null;
        private boolean isDoing = false;

        private static final int REQUEST_ENABLE_BT = 1;

        // Stops scanning after 10 seconds.
        private static final long SCAN_PERIOD = 10000;

        //사운드 관련 변수
        int currVol;
        int maxVol;

        private String mDeviceName;
        private String mDeviceAddress;

        @Override
        protected void onResume() {
            super.onResume();

            if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
            final Intent intent = getIntent();
            mDeviceName = intent.getStringExtra("deviceName");
            mDeviceAddress = intent.getStringExtra("deviceAddress");
            scanLeDevice(true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //getActionBar().setTitle(R.string.title_devices);

            mHandler = new Handler();

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }

            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

        }

        @Override
        protected void onPause() {
            super.onPause();
            scanLeDevice(true);
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

        class BackgroundTask extends AsyncTask<String, Integer, Integer> {
            protected void onPreExecute() {
                if (!mScanning) {
                    //backgroundTask.cancel(true);
                }
            }

            //--- Thread의 주요 작업을 처리 하는 함수
            //--- Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받는다.
            protected Integer doInBackground(String... values) {
                int count = 0;
                long startTime = 0;

                //초기 셋팅 ( 디바이스 최대 음량의 절반으로 하고 시작 )
                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                currVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.d("VOL", "CURRENT VOL = " + currVol + ", MAX VOL = " + maxVol);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 8, 0);
                currVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                mp = MediaPlayer.create(BTtracker_information.this, R.raw.tt);
                mp.start();

                startTime = System.currentTimeMillis();

                // 유저가 반응할 때 까지 알람소리 반복
                while (true) {
                    //소리키우기
                    long now = System.currentTimeMillis();
                    if (now - startTime >= 15000) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
                        startTime = System.currentTimeMillis();
                    } else if (now - startTime >= 60000) {
                        break;
                    }
                    //카운트 증가
                    count++;
                }

                //--- onProgressUpdate(~) 실행하기 위해서는 아래 함수를 호출 한다.
                // publishProgress("Thread processing.");
                return 1;
            }

            //--- doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
            protected void onProgressUpdate(Integer values) {

            }

            //--- Thread를 처리 한 후에 호출되는 함수
            //--- doInBackground(~)의 리턴값을 인자로 받는다.
            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    Log.d("playing music : ", "success");
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, AudioManager.FLAG_PLAY_SOUND);
                }
            }

            //--- AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
            protected void onCancelled() {
            }
        } // end of AsyncTask

        // Device scan callback.
        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(!mScanning) {
//                            //backgroundTask.cancel(true);
//                        } else {
//                            if(device.getAddress().equals(mDeviceAddress) && !isDoing) {
////                                backgroundTask = new BackgroundTask();
////                                backgroundTask.execute(device.getAddress());
//                                startActivity(new Intent(this, AlarmActivity.class));
//                            }
//                        }
//                    }
//                });
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!mScanning) {
//                            //backgroundTask.cancel(true);
                        } else {
                            if (device.getAddress().equals(mDeviceAddress) && !isDoing) {
                                isDoing = true;
                                alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
                                startActivityForResult(alarmIntent,222);
                            }
                        }
                    }
                });
            }
        };

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            try {
                switch(requestCode) {
                    case(222): {
                        //핸드폰을 찾아서 종료된 경우
                        if(resultCode == Activity.RESULT_OK) {
                            isDoing = false;
                            finish();
                        }
                        else {
                            Log.d("INTENT_COMMUNICATION","failed");
                        }
                        break;
                    }
                }
            }
            catch(Exception e) {
            }
        }
    }

