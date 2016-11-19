package helloworld.example.com.lg_bttracker_alpha;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class AlarmActivity extends AppCompatActivity {

    private MediaPlayer mp = null;
    AudioManager am = null;

    //사운드 관련 변수
    int currVol;
    int maxVol;
    Context context;
    Button find;
    PlayMusic backgroundTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        find = (Button)findViewById(R.id.find);
       //음악 재생
        backgroundTask = new PlayMusic();
        backgroundTask.execute();
    }

    public void findPhone(View view) {
       backgroundTask.flag=false;
       // backgroundTask.cancel(true);this.onDestroy();
    }

    class PlayMusic extends AsyncTask<String , Integer  , Integer > {
        boolean flag=true;
        protected void onPreExecute() {
//            if(!mScanning) {
//                //backgroundTask.cancel(true);
//            }
        }
        //--- Thread의 주요 작업을 처리 하는 함수
        //--- Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받는다.
        protected Integer doInBackground(String ... values) {
            int count = 0;
            long startTime = 0;

            //초기 셋팅 ( 디바이스 최대 음량의 절반으로 하고 시작 )
            am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            currVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            Log.d("VOL", "CURRENT VOL = " + currVol + ", MAX VOL = " + maxVol);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 8, 0);
            currVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            mp = MediaPlayer.create(AlarmActivity.this, R.raw.tt);
            mp.start();

            startTime = System.currentTimeMillis();

            // 유저가 반응할 때 까지 알람소리 반복
            while(flag) {
                //소리키우기
                long now = System.currentTimeMillis();
                if(!flag) {
                    mp.stop();
                    break;
                }
                if(now - startTime >= 15000) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
                    startTime = System.currentTimeMillis();
                } else if(now - startTime >= 60000) {
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
        protected void onPostExecute(Integer  result) {
            if(result == 1) {
                Log.d("playing music : ", "no reply");
                am.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, AudioManager.FLAG_PLAY_SOUND);
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
        //--- AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
        }
    } // end of AsyncTask

    public void onStop(){
        mp.stop();
        super.onStop();
    }

}
