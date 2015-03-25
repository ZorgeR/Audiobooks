package com.zlab.audiobooks;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudiobooksDetail extends Activity{
	
	String BookName = "";
	String Author = "";
	String BookLogoURL = "";
	String ReleaseDATA = "";
	String MediaURL = "";
	String SizeKB = "";
	String LanguageCODE = "";
	String mURL;
	boolean BuildInPlayer;
	private DownloadManager mgr=null;
	private File offlinebookfile;
	private Button downloadButton;

    MediaPlayer mediaPlayer;
    Button play;
    Button playview;
    SeekBar seek;
    SeekBar volume;
    TextView time;
    AudioManager am;
    int total;
    ProgressDialog audio_cache = null;

    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.detail_page);

        	BookName = getIntent().getExtras().getString("BookName");
        	Author = getIntent().getExtras().getString("Author");
        	BookLogoURL = getIntent().getExtras().getString("BookLogoURL");
        	ReleaseDATA = getIntent().getExtras().getString("ReleaseDATA");
        	MediaURL = getIntent().getExtras().getString("MediaURL");
        	SizeKB = getIntent().getExtras().getString("SizeKB");
        	LanguageCODE = getIntent().getExtras().getString("LanguageCODE");
        	BuildInPlayer = getIntent().getExtras().getBoolean("BuildInPlayer");

        	TextView txtBookName 		= (TextView) findViewById(R.id.txtBookName_detail);
			TextView txtAuthor 			= (TextView) findViewById(R.id.txtBookAuthor_detail);
//			TextView txtBookLogoURL 	= (TextView) findViewById(R.id.txtBookTotal);
			TextView txtReleaseDATA 	= (TextView) findViewById(R.id.txtBookData_detail);
//			TextView txtMediaURL 		= (TextView) findViewById(R.id.txtStorageSize);
//			TextView txtSizeKB 			= (TextView) findViewById(R.id.txtType);
//			TextView txtLanguageCODE	= (TextView) findViewById(R.id.txtLastUpdate);

			txtBookName.setText(BookName);
			txtAuthor.setText(Author);
//			txtBookLogoURL.setText(BookLogoURL);
			txtReleaseDATA.setText(ReleaseDATA);
//			txtMediaURL.setText(MediaURL);
//			txtSizeKB.setText(SizeKB);
//			txtLanguageCODE.setText(LanguageCODE);
			
			// Меняем шрифт
			/*
		    Typeface ptcaption=Typeface.createFromAsset(getAssets(),"fonts/ptcaption.ttf");
		    Typeface ptcaptionnormal=Typeface.createFromAsset(getAssets(),"fonts/ptcaptionnormal.ttf");
		    txtBookName.setTypeface(ptcaptionnormal);
		    txtAuthor.setTypeface(ptcaption);
		    txtReleaseDATA.setTypeface(ptcaptionnormal);
			 */
			
		    mgr=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		    registerReceiver(onNotificationClick,new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

		    offlinebookfile = new File(android.os.Environment.getExternalStorageDirectory()+"/Download/audiobooks/", BookName+".mp3");
		    downloadButton = (Button) findViewById(R.id.button_download);
		    if (offlinebookfile.exists()==true){downloadButton.setEnabled(false);} else {downloadButton.setEnabled(true);}


            // VIEW
		    setVolumeControlStream(AudioManager.STREAM_MUSIC);
            play = (Button)findViewById(R.id.button_play);
            seek = (SeekBar)findViewById(R.id.seekBar1);
            volume = (SeekBar) findViewById(R.id.seekBar_volume);
            time = (TextView) findViewById(R.id.time);
            
            // Перемотка
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar cur) {
                	mediaPlayer.seekTo(cur.getProgress()*total/100);
                }
                public void onStartTrackingTouch(SeekBar arg0) {
                }
                public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                }
            });
            
            // ГРОМКОСТЬ
            am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            volume.setMax(maxVolume);
            volume.setProgress(curVolume);
            volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar arg0) {
                }
                public void onStartTrackingTouch(SeekBar arg0) {
                }
                public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
                }
            });
            // ПЛЕЕР
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                    	audio_cache.dismiss();
                    	total = mediaPlayer.getDuration();
                        mp.start();
                    	playbuttonview.sendEmptyMessage(0);
                        whatchdog();
                    }
                });
                mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
                	public void onCompletion(MediaPlayer mp) {
				            mediaPlayer.stop();
				            mediaPlayer.reset();
				            playbuttonview.sendEmptyMessage(0);
					}
                });
            }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete_book:{
        				if(offlinebookfile.exists()==true) {
        					offlinebookfile.delete();
        					if (offlinebookfile.exists()==false){downloadButton.setEnabled(true);}
        				} else {Toast.makeText(this, "Книга еще не скачана!", Toast.LENGTH_LONG).show();}
       	 				return true;}
        case R.id.stop_play:{
			        		Message msg = new Message();
			        		msg.what=0;
				            mediaPlayer.stop();
				            mediaPlayer.reset();
	                    	playbuttonview.sendEmptyMessage(0);
				            setTimeHandler.sendMessage(msg);
			        	return true;}
        case R.id.pause_play:{
			        	if(mediaPlayer.isPlaying()) {
				            mediaPlayer.pause();
	                    	playbuttonview.sendEmptyMessage(0);
				        }
			        	return true;}
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
    public void play_book_button_click (View view){
    	switch (view.getId()) {
        case R.id.button_play:{
        	if (offlinebookfile.exists()==true){
        		Uri uri = Uri.parse("file:///sdcard/Download/audiobooks/"+BookName+".mp3");
        		mURL="file:///sdcard/Download/audiobooks/"+BookName+".mp3";
        		
        		if (BuildInPlayer){
            		preparePlayer();
        		}else{
        			Intent intent = new Intent(Intent.ACTION_VIEW);
                	intent.setDataAndType(uri, "audio/*");
                	startActivity(intent);
        		}
    			} else {
    				Uri uri = Uri.parse(MediaURL);
    				mURL=MediaURL;
    				if (BuildInPlayer){
                		preparePlayer();
            		}else{
            			Intent intent = new Intent(Intent.ACTION_VIEW);
        	        	intent.setDataAndType(uri, "audio/*");
        	        	startActivity(intent);
            		}
    	        	};}
        	/*
        	 * Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        	 * Uri uri = Uri.parse("file:///sdcard/song.mp3");
        	*/
        }
    }

    public void download_book_button_click (View view){
    	switch (view.getId()) {
        case R.id.button_download:{
            Request request = new Request(Uri.parse(MediaURL));
            request.setTitle(BookName).setDescription(Author)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS+"/audiobooks/", BookName + ".mp3")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            mgr.enqueue(request);
        };
        }
    }

    public void showDownload(View view) {
        Intent i = new Intent();
        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        startActivity(i);
    }
    
    protected void onStop(){
        super.onStop();
    }
    
    protected void onResume() {
    	super.onResume();
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      releaseMediaPlayer();
      unregisterReceiver(onComplete);
      unregisterReceiver(onNotificationClick);
    }

      BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
        		downloadButton.setEnabled(false);
				Toast.makeText(ctxt, "Файл загружен!", Toast.LENGTH_LONG).show();
        }
      };
      

      BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
        	Intent i = new Intent();
            i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(i);
        }
      };

      private Handler playbuttonview = new Handler(){
    	  public void handleMessage(Message msg){
    		  super.handleMessage(msg);
    		  Button playview = (Button) findViewById(R.id.button_play);
    		if(mediaPlayer.isPlaying()) {
    			playview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_pause));
  	        } else {
  	        	playview.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_play));
  	        }
    	  }
      };

      private Handler setTimeHandler = new Handler(){
          public void handleMessage(Message msg){
        	  //Toast.makeText(com.zlab.audiobooks.AudiobooksDetail.this, String.valueOf(msg), Toast.LENGTH_LONG).show();
        	  int CurentSec = msg.what/1000;
        	  int TotalSec = total/1000;
        	  int hours = CurentSec / 3600;
        	  int minutes = (CurentSec % 3600) / 60;
        	  int seconds = CurentSec % 60;

        	  int hours_total = TotalSec / 3600;
        	  int minutes_total = (TotalSec % 3600) / 60;
        	  int seconds_total = TotalSec % 60;

        	  String CurentTimeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        	  String TotalTimeString = String.format("%02d:%02d:%02d", hours_total, minutes_total, seconds_total);
        	  
        	  //String CurentTimeString = hours + ":" + minutes + ":" + seconds;
        	  //String TotalTimeString = hours_total + ":" + minutes_total + ":" + seconds_total;
        	  
        	  time.setText(CurentTimeString + " / " + TotalTimeString);
        	  
        	  //
        	  int progress = msg.what * 100 / total;
        	  seek.setProgress(progress);
          }
      };
      
      public void whatchdog(){
      	new Thread() {
              public void run() {
            	  try{
                      while(mediaPlayer != null && mediaPlayer.isPlaying()){
                          int currentPosition = mediaPlayer.getCurrentPosition();
                          Message msg = new Message();
                          msg.what = currentPosition;
                          setTimeHandler.sendMessage(msg);
                          Thread.sleep(1000);
                      }
                  }catch (InterruptedException e){
                      e.printStackTrace();
                  }
              }
          }.start();
      }
      
      private void releaseMediaPlayer() {
    	    if (mediaPlayer != null) {
    	        if(mediaPlayer.isPlaying()) {
    	            mediaPlayer.stop();
    	        }
    	        mediaPlayer.release();
    	        mediaPlayer = null;
    	    }
    	}
      private void preparePlayer() {
    	  audio_cache = ProgressDialog.show(this, "Кеширование ...", "Подождите ...", true);

    	    if (mediaPlayer == null) {
    	        mediaPlayer = new MediaPlayer();
    	    }

    	    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	    try {
    	        mediaPlayer.setDataSource(mURL);
    	        mediaPlayer.prepareAsync();
    	    } catch (IllegalArgumentException e) {
    	        Toast.makeText(this,"URI Error",Toast.LENGTH_LONG).show();audio_cache.dismiss();
    	        e.printStackTrace();
    	    } catch (IllegalStateException e) {
    	        // Toast.makeText(this,"Уже играю! Спокойной!",Toast.LENGTH_LONG).show();audio_cache.dismiss();
    	    	audio_cache.dismiss();
    	    	if(mediaPlayer.isPlaying()) {
    	            mediaPlayer.pause();
    	            playbuttonview.sendEmptyMessage(0);
    	        } else {
    	        	mediaPlayer.start();
    	        	playbuttonview.sendEmptyMessage(0);
    	        	whatchdog();}
    	        e.printStackTrace();
    	    } catch (IOException e) {
    	        Toast.makeText(this,"IO Error",Toast.LENGTH_LONG).show();audio_cache.dismiss();
    	        e.printStackTrace();
    	    }
    	}
      
}
