package feiyu.com.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


import feiyu.com.R;
import feiyu.com.musicplayer_service.MediaService;
import feiyu.com.musicplayer_util.Config;
import feiyu.com.musicplayer_util.MediaUtil;
import feiyu.com.musicplayer_util.Music;
import feiyu.com.musicplayer_util.PlayState;
import feiyu.com.musicplayer_view.SlidingMenu;


public class MainActivity extends Activity {
    public List<Music> mPlayList = new ArrayList(){};
    private ListView mMusiclist;
    private mBaseAdapter mAdapter;
    private Button playBtn;
    private boolean isFirstTime;
    private boolean isPlaying = false;
    private boolean isPause = true;
    private int listPosition;
    private TextView musicTitle;
    private TextView musicArtist;
    private Button nextBtn;


    private RelativeLayout mainBackground;
    private RelativeLayout musicAbout_layout;
    private TextView tv_changeBg;
    private TextView tv_backToMain;
    private TextView tv_lrcShow;
    private TextView tv_share;
    private TextView tv_quit;
    private SlidingMenu slidingMenu;
    private mReceiver serviceReceiver;

    int []bg = new int[]{R.drawable.main_bg01,R.drawable.main_bg02,R.drawable.main_bg03,R.drawable.main_bg04,
            R.drawable.main_bg05,R.drawable.main_bg06,R.drawable.background};
    int bgflag = 0;
    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ShareSDK.initSDK(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        serviceReceiver = new mReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(Config.UPDATE_ACTION);
        filter.addAction(Config.PLAYSTATE);
        // 注册BroadcastReceiver
        registerReceiver(serviceReceiver, filter);


        mMusiclist = (ListView)findViewById(R.id.music_list);


        mPlayList = MediaUtil.getMp3Infos(MainActivity.this);
        mAdapter = new mBaseAdapter();
        mMusiclist.setAdapter(mAdapter);
        mMusiclist.setOnItemClickListener(new mItemClickListener());
        findViewById();
        setViewOnclickListener();
        isFirstTime = true;
    }



    private void findViewById() {
        playBtn = (Button)findViewById(R.id.play_music);
        nextBtn = (Button)findViewById(R.id.next_music);
        musicTitle = (TextView)findViewById(R.id.music_title);
        musicArtist = (TextView)findViewById(R.id.music_artist);
        slidingMenu = (SlidingMenu)findViewById(R.id.id_slidingMenu);

        mainBackground = (RelativeLayout)findViewById(R.id.mainBg);
        musicAbout_layout = (RelativeLayout)findViewById(R.id.music_about_layout);

        tv_changeBg = (TextView)findViewById(R.id.id_tv_background);
        tv_backToMain = (TextView)findViewById(R.id.id_tv_backToMain);
        tv_lrcShow = (TextView)findViewById(R.id.id_tv_lrcShow);
        tv_share = (TextView)findViewById(R.id.id_tv_share);
        tv_quit = (TextView)findViewById(R.id.id_tv_quit);
    }

    /**
     * 给每一个按钮设置监听器 
     */
    private void setViewOnclickListener() {
        ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
        playBtn.setOnClickListener(viewOnClickListener);
        nextBtn.setOnClickListener(viewOnClickListener);
        musicAbout_layout.setOnClickListener(viewOnClickListener);
        tv_changeBg.setOnClickListener(viewOnClickListener);
        tv_backToMain.setOnClickListener(viewOnClickListener);
        tv_lrcShow.setOnClickListener(viewOnClickListener);
        tv_share.setOnClickListener(viewOnClickListener);
        tv_quit.setOnClickListener(viewOnClickListener);
    }

    //自定义的BroadcastReceiver，负责监听从Service传回来的广播
    public class mReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Config.MUSIC_CURRENT)){

            } else if (action.equals(Config.MUSIC_DURATION)) {
                //duration = intent.getIntExtra("duration", -1);
            }
            else if(action.equals(Config.UPDATE_ACTION)) {
                //获取Intent中的current消息，current代表当前正在播放的歌曲
                listPosition = intent.getIntExtra("current", -1);
                if(listPosition >= 0) {
                    musicTitle.setText(mPlayList.get(listPosition).getTitle());
                    musicArtist.setText(mPlayList.get(listPosition).getArtist());
                }
            }else if(action.equals(Config.PLAYSTATE)){
                Boolean isPause = intent.getBooleanExtra("playState",false);
                isFirstTime = false;
                if (isPause) {
                        //isPlaying = false;
                        playBtn.setBackgroundResource(R.drawable.play_selector);
                } else {
                        //isPlaying = true;
                        playBtn.setBackgroundResource(R.drawable.pause_selector);

                }

            }

        }
    }

    private class ViewOnClickListener implements View.OnClickListener {
        Intent intent = new Intent();
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_music: // 播放音乐  
                    if(isFirstTime) {
                        play();
                        isFirstTime = false;
                        isPlaying = true;
                        isPause = false;
                        Toast.makeText(MainActivity.this,"First",Toast.LENGTH_LONG).show();
                        if (!isPlaying) {
                            playBtn.setBackgroundResource(R.drawable.play_selector);
                        } else if(!isPause) {
                            playBtn.setBackgroundResource(R.drawable.pause_selector);
                        }
                    } else {
                        if (isPlaying) {
                            playBtn.setBackgroundResource(R.drawable.play_selector);
                            intent.setAction(Config.STARTSERVICE);
                            intent.putExtra("MSG", PlayState.PlayerMsg.PAUSE_MSG);
                            startService(intent);
                            isPlaying = false;
                            isPause = true;
                            Toast.makeText(MainActivity.this,"pause",Toast.LENGTH_LONG).show();
                        } else if (isPause) {
                            playBtn.setBackgroundResource(R.drawable.pause_selector);
                            intent.setAction(Config.STARTSERVICE);
                            intent.putExtra("MSG", PlayState.PlayerMsg.CONTINUE_MSG);
                            startService(intent);
                            isPause = false;
                            isPlaying = true;
                            Toast.makeText(MainActivity.this,"continue",Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case R.id.next_music:
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                    isFirstTime = false;
                    isPlaying = true;
                    isPause = false;
                    next();
                    break;


                case R.id.music_about_layout:
                    Toast.makeText(MainActivity.this,"lrc start !",Toast.LENGTH_LONG).show();
                    Music m = mPlayList.get(listPosition);
                    Intent intent2 = new Intent(getApplicationContext(), PlayActivity.class);
                    intent2.putExtra("title", m.getTitle());
                    intent2.putExtra("url", m.getPath());
                    intent2.putExtra("isFirstTime",isFirstTime);
                    intent2.putExtra("isPause",isPause);
                    intent2.putExtra("isPlaying",isPlaying);
                    intent2.putExtra("artist", m.getArtist());
                    intent2.putExtra("listPosition", listPosition);
                    intent2.putExtra("MSG",PlayState.PlayerMsg.PLAYING_MSG);
                    startActivity(intent2);
                    break;

                case R.id.id_tv_quit:
           new AlertDialog.Builder(MainActivity.this)  
                	                    .setIcon(R.drawable.ic_launcher)  
                	                    .setTitle("退出")  
                	                    .setMessage("您确定要退出？")  
                	                    .setNegativeButton("取消", null)  
                	                    .setPositiveButton("确定",  
                	                            new DialogInterface.OnClickListener() {  
                	  
                	                                @Override  
                	                                public void onClick(DialogInterface dialog,  
                	                                        int which) {
                	                                	MainActivity.this.onPause();
                	                                	MainActivity.this.onStop();
                	                                	MainActivity.this.onDestroy();
                	           
                	                                    finish();  
                	                                    Intent intent = new Intent(  
                	                                            MainActivity.this,  
                	                                            MediaService.class);  
                	                                    unregisterReceiver( serviceReceiver);  
                	                                    stopService(intent); // 停止后台服务  
                	                                }  
                	                            }).show();  
                
                
                    break;
                case R.id.id_tv_share:

                	OnekeyShare onekeyShare = new OnekeyShare();
                    onekeyShare.setTitle("这只是一个标题");
            		onekeyShare.setText("分享一首好听的音乐"+"<<"+mPlayList.get(listPosition).getTitle()+">>");
            		onekeyShare.show(getApplicationContext());
                    break;
                case R.id.id_tv_background:
                    mainBackground.setBackgroundResource(bg[bgflag++%bg.length]);
                    break;
                case R.id.id_tv_backToMain:
                    slidingMenu.closeMenu();
                    break;
                case R.id.id_tv_lrcShow:
                    Toast.makeText(MainActivity.this,"lrc start !",Toast.LENGTH_LONG).show();
                    Music m1 = mPlayList.get(listPosition);
                    Intent intent1 = new Intent(getApplicationContext(), PlayActivity.class);
                    intent1.putExtra("title", m1.getTitle());
                    intent1.putExtra("url", m1.getPath());
                    intent1.putExtra("isFirstTime",isFirstTime);
                    intent1.putExtra("isPause",isPause);
                    intent1.putExtra("isPlaying",isPlaying);
                    intent1.putExtra("artist", m1.getArtist());
                    intent1.putExtra("listPosition", listPosition);
                    intent1.putExtra("MSG",PlayState.PlayerMsg.PLAYING_MSG);
                    startActivity(intent1);
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    /**
     * 下一首歌曲
     */
    public void next() {
        listPosition = listPosition + 1;
        if(listPosition <= mPlayList.size() - 1) {
            Music  m = mPlayList.get(listPosition);
            //musicTitle.setText(m.getTitle());
            Intent intent = new Intent();
            intent.setAction(Config.STARTSERVICE);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", m.getPath());
            intent.putExtra("MSG", PlayState.PlayerMsg.NEXT_MSG);
            startService(intent);
        } else {
            Toast.makeText(MainActivity.this, "没有下一首了", Toast.LENGTH_SHORT).show();
        }
    }
    private void play() {
        playBtn.setBackgroundResource(R.drawable.pause_selector);
        Music m = mPlayList.get(listPosition);
        musicTitle.setText(m.getTitle());
        musicArtist.setText(m.getArtist());
        Intent intent = new Intent();
        intent.setAction(Config.STARTSERVICE);
        intent.putExtra("listPosition", 0);
        intent.putExtra("url", m.getPath());
        intent.putExtra("MSG", PlayState.PlayerMsg.PLAY_MSG);
        startService(intent);
    }


    class ViewHolder{


        public TextView tx1;
        public TextView tx2;
        public TextView tx3;
    }


    //音乐列表项的初始化
    private class mBaseAdapter extends BaseAdapter {

        private static final long Long = 0;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null){

                holder = new ViewHolder();
                //convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listitem, null);
                convertView = View.inflate(getApplicationContext(),R.layout.music_list_item_layout, null);
                holder.tx1 = (TextView) convertView.findViewById(R.id.music_title);
                holder.tx2 = (TextView) convertView.findViewById(R.id.music_Artist);
                holder.tx3=(TextView)convertView.findViewById(R.id.music_duration);
                convertView.setTag(holder);
            }else{

                holder = (ViewHolder) convertView.getTag();
            }
			

            holder.tx1.setTextColor(Color.WHITE);

            holder.tx1.setText(mPlayList.get(position).getTitle());
            holder.tx2.setText((mPlayList.get(position)).getArtist());
            holder.tx3.setText(MediaUtil.formatTime(mPlayList.get(position).getDuration()));
            return convertView;
        }
        @Override
        public int getCount() {
            return mPlayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPlayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceReceiver!=null)
        unregisterReceiver(serviceReceiver);
        Intent intent = new Intent(MainActivity.this, MediaService.class);
        stopService(intent); // 停止后台服务
        finish();
    }

    private class mItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(mPlayList != null) {
                listPosition = position;
                isFirstTime = false;
                isPlaying = true;
                isPause = false;
                if (!isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.play_selector);
                } else if(!isPause) {
                    playBtn.setBackgroundResource(R.drawable.pause_selector);
                }
                play();
            }

        }
    }
   public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK  
                && event.getAction() == KeyEvent.ACTION_DOWN) {  
  
            new AlertDialog.Builder(this)  
                    .setIcon(R.drawable.ic_launcher)  
                    .setTitle("退出")  
                    .setMessage("您确定要退出？")  
                    .setNegativeButton("取消", null)  
                    .setPositiveButton("确定",  
                            new DialogInterface.OnClickListener() {  
  
                                @Override  
                                public void onClick(DialogInterface dialog,  
                                        int which) {
                                	MainActivity.this.onPause();
                                	MainActivity.this.onStop();
                                	MainActivity.this.onDestroy();
           
                                    finish();  
                                    Intent intent = new Intent(  
                                            MainActivity.this,  
                                            MediaService.class);  
                                    unregisterReceiver( serviceReceiver);  
                                    stopService(intent); // 停止后台服务  
                                }  
                            }).show();  
  
        }  
        return super.onKeyDown(keyCode, event);  
    }  
    
    
}