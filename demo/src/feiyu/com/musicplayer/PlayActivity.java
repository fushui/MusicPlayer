package feiyu.com.musicplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import feiyu.com.R;
import feiyu.com.musicplayer_service.MediaService;
import feiyu.com.musicplayer_util.Config;
import feiyu.com.musicplayer_util.LrcContent;
import feiyu.com.musicplayer_util.LrcProcess;
import feiyu.com.musicplayer_util.LrcView;
import feiyu.com.musicplayer_util.MediaUtil;
import feiyu.com.musicplayer_util.Music;
import feiyu.com.musicplayer_util.PlayState;

/**
 * Created by feiyu on 2015/10/25.
 */
public class PlayActivity extends Activity{



    public static LrcView lrcView;


    private TextView musicTitle;
    private TextView musicArtist;
    private TextView currentProgress;   //当前进度消耗的时间
    private TextView finalProgress;     //歌曲时间
    private SeekBar music_progressBar;  //歌曲进度

    private Button playBtn;     // 播放（播放、暂停）
    private Button nextBtn;     // 下一首
    private Button previousBtn; // 上一首

    private Button shuffleBtn; // 随机播放
    private Button repeatBtn;   // 重复（单曲循环、全部循环）

    private String title;       //歌曲标题
    private String artist;      //歌曲艺术家
    private String url;         //歌曲路径
    private int listPosition;   //播放歌曲在mp3Infos的位置
    private int currentTime;    //当前歌曲播放时间
    private int duration;       //歌曲长度
    private int flag;           //播放标识
    private Boolean isFirstTime = true;


    private int repeatState;
    private final int isCurrentRepeat = 1; // 单曲循环
    private final int isAllRepeat = 2;      // 全部循环
    private final int isNoneRepeat = 3;     // 无重复播放
    private boolean isNoneShuffle;           // 顺序播放
    private boolean isShuffle;          // 随机播放
    private boolean isPull = false;


    private boolean isPlaying;              // 正在播放
    private boolean isPause;                // 暂停

    private List<Music> mp3Infos;


    private PlayerReceiver playerReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ircshow);
        findViewById();
        setViewOnclickListener();
        mp3Infos = MediaUtil.getMp3Infos(PlayActivity.this);
        playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.UPDATE_ACTION);
        filter.addAction(Config.PLAYSTATE);
        filter.addAction(Config.MUSIC_DURATION);
        filter.addAction(Config.MUSIC_CURRENT);
        registerReceiver(playerReceiver, filter);

    }


    public class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Config.MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", -1);
                currentProgress.setText(MediaUtil.formatTime(currentTime));
                if(!isPull)music_progressBar.setProgress(currentTime);
                if(currentTime == 0){      initLrc();handler.sendEmptyMessage(1);}
                //handler.sendEmptyMessageDelayed(1, 1000);
            } else if(action.equals(Config.MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1);
                music_progressBar.setMax(duration);
                finalProgress.setText(MediaUtil.formatTime(duration));
            } else if(action.equals(Config.UPDATE_ACTION)) {
                //获取Intent中的current消息，current代表当前正在播放的歌曲
                listPosition = intent.getIntExtra("current", -1);
                url = mp3Infos.get(listPosition).getPath();
                if(listPosition >= 0) {
                    musicTitle.setText(mp3Infos.get(listPosition).getTitle());
                    musicArtist.setText(mp3Infos.get(listPosition).getArtist());
                }
                if(listPosition == 0) {
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                    isPause = true;
                }
            }else if(action.equals(Config.PLAYSTATE)){
                Boolean isPause = intent.getBooleanExtra("playState",false);
                isFirstTime = false;
                if (isPause) {
                    isPlaying = false;
                    playBtn.setBackgroundResource(R.drawable.play_selector);
                } else {
                    isPlaying = true;
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                }

            }
        }

    }

    /**
     * 从界面上根据id获取按钮
     */
    private void findViewById() {

        musicTitle = (TextView) findViewById(R.id.musicTitle);
        musicArtist = (TextView) findViewById(R.id.musicArtist);

        playBtn = (Button) findViewById(R.id.play_music);
        previousBtn = (Button) findViewById(R.id.previous_music);
        nextBtn = (Button) findViewById(R.id.next_music);
        shuffleBtn = (Button) findViewById(R.id.shuffle_music);
        repeatBtn = (Button) findViewById(R.id.repeat_music);

        lrcView = (LrcView) findViewById(R.id.lrcShowView);
        music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
        currentProgress = (TextView) findViewById(R.id.current_progress);
        finalProgress = (TextView) findViewById(R.id.final_progress);


    }

    /**
     * 给每一个按钮设置监听器
     */
    private void setViewOnclickListener() {
        ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();
        playBtn.setOnClickListener(ViewOnClickListener);
        previousBtn.setOnClickListener(ViewOnClickListener);
        nextBtn.setOnClickListener(ViewOnClickListener);
        repeatBtn.setOnClickListener(ViewOnClickListener);
        shuffleBtn.setOnClickListener(ViewOnClickListener);


        music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

    }

    private Runnable mRunnable;
    private LrcProcess mLrcProcess; //歌词处理
    private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象

    private int index = 0;
    public void initLrc(){
        mLrcProcess = new LrcProcess(getApplicationContext());
        mLrcProcess.readLRC(mp3Infos.get(listPosition).getPath());
        lrcList = mLrcProcess.getLrcList();
        PlayActivity.lrcView.setmLrcList(lrcList);

        mRunnable = new Runnable() {
            public void run() {
                PlayActivity.lrcView.setIndex(lrcIndex(lrcList));
                PlayActivity.lrcView.invalidate();
                handler.postDelayed(mRunnable, 100);
            }
        };
        handler.post(mRunnable);
    }

    public Handler handler= new Handler() {
        public void handleMessage(android.os.Message msg) {
            if(isPlaying){
                if (msg.what == 1) {
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };



    public int lrcIndex(List  lrcList) {
        List  lyricContents = lrcList;
        int size = lyricContents.size();
       /* if(isPlaying) {
            currentTime = mp3Infos.get(listPosition).get
            duration = (int)mp3Infos.get(listPosition).getDuration();
        }*/
        if(currentTime < duration) {
            for (int i = 0; i < size; i++) {
                if (i < size - 1) {
                    if (currentTime < ((LrcContent) lyricContents.get(i)).getLrcTime() && i==0) {
                        index = i;
                        break;
                    }
                    if (currentTime > ((LrcContent) lyricContents.get(i)).getLrcTime()
                            && currentTime < ((LrcContent) lyricContents.get(i + 1)).getLrcTime()) {
                        index = i;
                        break;
                    }
                }
                if (i == size - 1
                        && currentTime > ((LrcContent) lyricContents.get(i)).getLrcTime()) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    public void next_music() {
        playBtn.setBackgroundResource(R.drawable.pause_selector);
        listPosition = listPosition + 1;
        if(listPosition <= mp3Infos.size() - 1) {
            Music mp3Info = mp3Infos.get(listPosition);
            url = mp3Info.getPath();
            musicTitle.setText(mp3Info.getTitle());
            musicArtist.setText(mp3Info.getArtist());
            Intent intent = new Intent();
            intent.setAction(Config.STARTSERVICE);
            intent.putExtra("url", mp3Info.getPath());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", PlayState.PlayerMsg.NEXT_MSG);
            startService(intent);
        } else {
            Toast.makeText(PlayActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 上一首
     */
    public void previous_music() {
        playBtn.setBackgroundResource(R.drawable.pause_selector);
        listPosition = listPosition - 1;
        if(listPosition >= 0) {
            Music mp3Info = mp3Infos.get(listPosition);   //上一首MP3
            musicTitle.setText(mp3Info.getTitle());
            musicArtist.setText(mp3Info.getArtist());
            url = mp3Info.getPath();
            Intent intent = new Intent();
            intent.setAction(Config.STARTSERVICE);
            intent.putExtra("url", mp3Info.getPath());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", PlayState.PlayerMsg.PRIVIOUS_MSG);
            startService(intent);
        }
        else {
            Toast.makeText(PlayActivity.this, "没有上一首了", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 播放音乐
     */
    public void play() {
        //开始播放的时候为顺序播放
        repeat_none();
       // handler.sendEmptyMessage(1);
        Intent intent = new Intent();
        intent.setAction(Config.STARTSERVICE);
        intent.putExtra("url", url);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("MSG", PlayState.PlayerMsg.PLAY_MSG);
        startService(intent);
    }



    public void repeat_all() {
        Intent intent = new Intent(Config.CTL_ACTION);
        intent.putExtra("control", 2);
        sendBroadcast(intent);
    }


    public void repeat_none() {
        Intent intent = new Intent(Config.CTL_ACTION);
        intent.putExtra("control", 3);
        sendBroadcast(intent);
    }

    public void repeat_one() {
        Intent intent = new Intent(Config.CTL_ACTION);
        intent.putExtra("control", 1);
        sendBroadcast(intent);
    }

    /**
     * 随机播放
     */
    public void shuffleMusic() {
        Intent intent = new Intent(Config.CTL_ACTION);
        intent.putExtra("control", 4);
        sendBroadcast(intent);
    }

    private class ViewOnclickListener implements View.OnClickListener {
        Intent intent = new Intent();
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.play_music:
                    if (isFirstTime) {
                        play();
                        isFirstTime = false;
                        isPlaying = true;
                        isPause = false;
                        if (!isPlaying) {
                            playBtn.setBackgroundResource(R.drawable.play_selector);
                        } else if(!isPause) {
                            playBtn.setBackgroundResource(R.drawable.pause_selector);
                        }
                        Toast.makeText(PlayActivity.this,"First",Toast.LENGTH_LONG).show();
                    } else {
                        if (isPlaying) {
                            playBtn.setBackgroundResource(R.drawable.play_selector);
                            intent.setAction(Config.STARTSERVICE);
                            intent.putExtra("MSG", PlayState.PlayerMsg.PAUSE_MSG);
                            startService(intent);
                            isPlaying = false;
                            isPause = true;

                        } else if (isPause) {
                           playBtn.setBackgroundResource(R.drawable.pause_selector);
                            intent.setAction(Config.STARTSERVICE);
                            intent.putExtra("MSG",
                                    PlayState.PlayerMsg.CONTINUE_MSG);
                            startService(intent);
                            isPause = false;
                            isPlaying = true;
                        }
                    }
                    break;
                case R.id.previous_music:       //上一首歌曲
                    previous_music();
                      break;
               case R.id.next_music:           //下一首歌曲
                    next_music();
                    break;
                case R.id.repeat_music:         //重复播放音乐
                    if (repeatState == isNoneRepeat) {
                        repeat_one();
                        shuffleBtn.setClickable(false); //是随机播放变为不可点击状态
                        repeatState = isCurrentRepeat;
                    } else if (repeatState == isCurrentRepeat) {
                        repeat_all();
                        shuffleBtn.setClickable(false);
                        repeatState = isAllRepeat;
                    } else if (repeatState == isAllRepeat) {
                        repeat_none();
                        shuffleBtn.setClickable(true);
                        repeatState = isNoneRepeat;
                    }
                    Intent intent = new Intent(Config.REPEAT_ACTION);
                    switch (repeatState) {
                        case isCurrentRepeat: // 单曲循环
                            repeatBtn
                                    .setBackgroundResource(R.drawable.repeat_current_selector);
                            Toast.makeText(PlayActivity.this, R.string.repeat_current,
                                    Toast.LENGTH_SHORT).show();


                            intent.putExtra("repeatState", isCurrentRepeat);
                            sendBroadcast(intent);
                            break;
                        case isAllRepeat: // 全部循环
                            repeatBtn
                                    .setBackgroundResource(R.drawable.repeat_all_selector);
                            Toast.makeText(PlayActivity.this, R.string.repeat_all,
                                    Toast.LENGTH_SHORT).show();
                            intent.putExtra("repeatState", isAllRepeat);
                            sendBroadcast(intent);
                            break;
                        case isNoneRepeat: // 无重复
                            repeatBtn
                                    .setBackgroundResource(R.drawable.repeat_none_selector);
                            Toast.makeText(PlayActivity.this, R.string.repeat_none,
                                    Toast.LENGTH_SHORT).show();
                            intent.putExtra("repeatState", isNoneRepeat);
                            break;
                    }
                    break;
                case R.id.shuffle_music:            //随机播放状态
                    Intent shuffleIntent = new Intent(Config.SHUFFLE_ACTION);
                    if (isNoneShuffle) {            //如果当前状态为非随机播放，点击按钮之后改变状态为随机播放
                        shuffleBtn
                                .setBackgroundResource(R.drawable.shuffle_selector);
                        Toast.makeText(PlayActivity.this, R.string.shuffle,
                                Toast.LENGTH_SHORT).show();
                        isNoneShuffle = false;
                        isShuffle = true;
                        shuffleMusic();
                        repeatBtn.setClickable(false);
                        shuffleIntent.putExtra("shuffleState", true);
                        sendBroadcast(shuffleIntent);
                    } else if (isShuffle) {
                        shuffleBtn
                                .setBackgroundResource(R.drawable.shuffle_none_selector);
                        Toast.makeText(PlayActivity.this, R.string.shuffle_none,
                                Toast.LENGTH_SHORT).show();
                        isShuffle = false;
                        isNoneShuffle = true;
                        repeatBtn.setClickable(true);
                        shuffleIntent.putExtra("shuffleState", false);
                        sendBroadcast(shuffleIntent);
                    }
                    break;
            }

        }
    }
    /**
     * 在OnResume中初始化和接收Activity数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        title = bundle.getString("title");
        artist = bundle.getString("artist");
        url = bundle.getString("url");
        listPosition = bundle.getInt("listPosition");
        flag = bundle.getInt("MSG");
        currentTime = bundle.getInt("currentTime");
        isFirstTime = bundle.getBoolean("isFirstTime");
        isPause = bundle.getBoolean("isPause");
        isPlaying = bundle.getBoolean("isPlaying");
        if (!isPlaying) {
                playBtn.setBackgroundResource(R.drawable.play_selector);
        } else if(!isPause) {
                playBtn.setBackgroundResource(R.drawable.pause_selector);
        }
        initView();
    }


    /**
     * 初始化界面
     */
    public void initView() {
        musicTitle.setText(title);
        musicArtist.setText(artist);

        music_progressBar.setMax((int) mp3Infos.get(listPosition).getDuration());
        finalProgress.setText(MediaUtil.formatTime(mp3Infos.get(listPosition).getDuration()));

        repeatState = isNoneRepeat;
        isNoneShuffle = true;
        switch (repeatState) {
          /*  case isCurrentRepeat: // 单曲循环
                shuffleBtn.setClickable(false);
                repeatBtn.setBackgroundResource(R.drawable.repeat_current_selector);
                break;
            case isAllRepeat: // 全部循环
                shuffleBtn.setClickable(false);
                repeatBtn.setBackgroundResource(R.drawable.repeat_all_selector);
                break;*/
            case isNoneRepeat: // 无重复
                shuffleBtn.setClickable(true);
                repeatBtn.setBackgroundResource(R.drawable.repeat_none_selector);
                break;
        }
       /* if(isShuffle) {
            isNoneShuffle = false;
            shuffleBtn.setBackgroundResource(R.drawable.shuffle_selector);
            repeatBtn.setClickable(false);
        } else {
            isNoneShuffle = true;
            shuffleBtn.setBackgroundResource(R.drawable.shuffle_none_selector);
            repeatBtn.setClickable(true);
        }*/
        if(flag == PlayState.PlayerMsg.PLAYING_MSG) { //如果播放信息是正在播放

           // Intent intent1 = new Intent();
           // intent1.setAction(Config.SHOW_LRC);
           // intent1.putExtra("listPosition", listPosition);
            //sendBroadcast(intent1);
            Toast.makeText(PlayActivity.this, "正在播放--" + title, Toast.LENGTH_LONG).show();
        }
        //playBtn.setBackgroundResource(R.drawable.pause_selector);
        //isPlaying = true;
        //isPause = false;
    }
    protected void onStop() {
        super.onStop();
       /* if(playerReceiver!=null)
        unregisterReceiver(playerReceiver);
      */  //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        if(playerReceiver!=null)
            unregisterReceiver(playerReceiver);
    }


    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(fromUser&&isPlaying) {
               // Toast.makeText(PlayActivity.this,"flag",Toast.LENGTH_LONG).show();
                isPull = true;
                audioTrackChange(progress); //用户控制进度的改变
            }else{
                isPull = false;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }


    public void audioTrackChange(int progress) {
        Intent intent = new Intent();
        intent.setAction(Config.STARTSERVICE);
        intent.putExtra("url", url);
        intent.putExtra("listPosition", listPosition);
        if(isPause) {
            intent.putExtra("MSG", PlayState.PlayerMsg.PAUSE_MSG);
        }
        else {
            intent.putExtra("MSG", PlayState.PlayerMsg.PROGRESS_CHANGE);
        }
        intent.putExtra("progress", progress);
        startService(intent);
    }

}
