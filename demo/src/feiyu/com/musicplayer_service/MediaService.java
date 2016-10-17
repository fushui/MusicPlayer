package feiyu.com.musicplayer_service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import feiyu.com.musicplayer_util.Config;
import feiyu.com.musicplayer_util.LrcContent;
import feiyu.com.musicplayer_util.LrcProcess;
import feiyu.com.musicplayer_util.MediaUtil;
import feiyu.com.musicplayer_util.Music;
import feiyu.com.musicplayer_util.PlayState;

/**
 * Created by feiyu on 2015/10/24.
 */
public class MediaService extends Service{



    private LrcProcess mLrcProcess; //歌词处理
    private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象
    private int index=0;
    private MediaPlayer mediaPlayer; // 媒体播放器对象
    private String path;            // 音乐文件路径
    private int msg;
    private boolean isPause;        // 暂停状态
    private int current = 0;        // 记录当前正在播放的音乐
    private List<Music> mp3Infos;   //存放Mp3Info对象的集合
    private int status = 1;

    private int duration;
    private int currentTime;
    private MyReceiver myReceiver;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "service created");
        mediaPlayer = new  MediaPlayer();
        mp3Infos = MediaUtil.getMp3Infos(MediaService.this);




        /**
         * 设置音乐播放完成时的监听器
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (status == 1) { // 单曲循环
                    mediaPlayer.start();
                } else if (status == 2) { // 全部循环
                    current++;
                    if (current > mp3Infos.size() - 1) {  //变为第一首的位置继续播放
                        current = 0;
                    }
                    Intent sendIntent = new Intent(Config.UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getPath();
                    play(0);
                } else if (status == 3) { // 顺序播放
                    current++;  //下一首位置
                    if (current <= mp3Infos.size() - 1) {
                        Intent sendIntent = new Intent(Config.UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                        sendBroadcast(sendIntent);
                        path = mp3Infos.get(current).getPath();
                        play(0);
                    } else {
                        mediaPlayer.seekTo(0);
                        current = 0;
                        Intent sendIntent = new Intent(Config.UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                        sendBroadcast(sendIntent);
                    }
                } else if (status == 4) {    //随机播放
                    current = getRandomIndex(mp3Infos.size() - 1);
                    System.out.println("currentIndex ->" + current);
                    Intent sendIntent = new Intent(Config.UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getPath();
                    play(0);
                }
            }
        });
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.CTL_ACTION);
        //filter.addAction(Config.SHOW_LRC);
        registerReceiver(myReceiver, filter);
    }

    /*public void initLrc(){
        mLrcProcess = new LrcProcess();
        mLrcProcess.readLRC(mp3Infos.get(current).getPath());
        lrcList = mLrcProcess.getLrcList();
        PlayActivity.lrcView.setmLrcList(lrcList);
        handler.post(mRunnable);
    }
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {

            PlayActivity.lrcView.setIndex(lrcIndex(lrcList));
            PlayActivity.lrcView.invalidate();
            handler.postDelayed(mRunnable, 100);
        }
    };


    public int lrcIndex(List  lrcList) {
        List  lyricContents = lrcList;
        int size = lyricContents.size();
        if(mediaPlayer.isPlaying()) {
            currentTime = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
        }
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
    }*/

    public  Handler handler= new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                if(mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
                    Intent intent = new Intent();
                    intent.setAction(Config.MUSIC_CURRENT);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent); // 给PlayerActivity发送广播
                    handler.sendEmptyMessageDelayed(1, 1000);
                }

            }
        }
    };

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);
            switch (control) {
                case 1:
                    status = 1; // 将播放状态置为1表示：单曲循环
                    break;
                case 2:
                    status = 2; //将播放状态置为2表示：全部循环
                    break;
                case 3:
                    status = 3; //将播放状态置为3表示：顺序播放
                    break;
                case 4:
                    status = 4; //将播放状态置为4表示：随机播放
                    break;

            }
           /* String action = intent.getAction();
            if(action.equals(Config.SHOW_LRC)){
               // current = intent.getIntExtra("listPosition", -1);
            }*/
        }
    }
    protected int getRandomIndex(int end) {
        int index = (int) (Math.random() * end);
        return index;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        path = intent.getStringExtra("url");        //歌曲路径
        current = intent.getIntExtra("listPosition", -1);   //当前播放歌曲的在mp3Infos的位置
        msg = intent.getIntExtra("MSG", 0);         //播放信息
        if (msg == PlayState.PlayerMsg.PLAY_MSG) {    //直接播放音乐
            play(0);
        } else if (msg == PlayState.PlayerMsg.PAUSE_MSG) {    //暂停
            pause();
        } else if (msg == PlayState.PlayerMsg.CONTINUE_MSG) { //继续播放
            resume();
        } else if (msg == PlayState.PlayerMsg.NEXT_MSG) {     //下一首
            next();
        } else if (msg == PlayState.PlayerMsg.PRIVIOUS_MSG) { //上一首
            previous();
        } else if (msg ==PlayState.PlayerMsg.PLAYING_MSG) {
            handler.sendEmptyMessage(1);
        } else if (msg == PlayState.PlayerMsg.PROGRESS_CHANGE) {  //进度更新
            currentTime = intent.getIntExtra("progress", -1);
            play(currentTime);
        }

            super.onStart(intent, startId);
    }

    private void previous() {
        isPause = false;
        Intent sendIntent = new Intent(Config.UPDATE_ACTION);
        sendIntent.putExtra("current", current);
        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
        sendBroadcast(sendIntent);
        play(0);
    }


    /**
     * 下一首
     */
    private void next() {
        isPause = false;
        Intent sendIntent = new Intent(Config.UPDATE_ACTION);
        sendIntent.putExtra("current", current);
         //发送广播，将被Activity组件中的BroadcastReceiver接收到
        sendBroadcast(sendIntent);
        play(0);

    }
    private void play(int currentTime) {
        try {
            mediaPlayer.reset();// 把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器

            handler.sendEmptyMessage(1);
            Intent sendIntent = new Intent(Config.PLAYSTATE);
            sendIntent.putExtra("playState", isPause);
            //发送广播，将被Activity组件中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;


            Intent sendIntent = new Intent(Config.PLAYSTATE);
            sendIntent.putExtra("playState", isPause);
            //发送广播，将被Activity组件中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    private void resume() {
        if (isPause) {
            mediaPlayer.start();
            isPause = false;


            Intent sendIntent = new Intent(Config.PLAYSTATE);
            sendIntent.putExtra("playState", isPause);
            //发送广播，将被Activity组件中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int currentTime;

        public PreparedListener(int currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start(); // 开始播放
            if (currentTime > 0) { // 如果音乐不是从头播放
                mediaPlayer.seekTo(currentTime);
            }
            Intent intent = new Intent();
            intent.setAction(Config.MUSIC_DURATION);
            duration = mediaPlayer.getDuration();
            intent.putExtra("duration", duration);  //通过Intent来传递歌曲的总长度
            sendBroadcast(intent);
        }
    }


    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(myReceiver!=null){
        	unregisterReceiver(myReceiver);
        }

    }
}
