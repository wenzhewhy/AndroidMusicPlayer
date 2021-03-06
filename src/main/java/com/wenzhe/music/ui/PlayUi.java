package com.wenzhe.music.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wenzhe.music.R;
import com.wenzhe.music.action.MusicChangeAction;
import com.wenzhe.music.action.PlayAction;
import com.wenzhe.music.constants.AppConstant;
import com.wenzhe.music.constants.MusicChangeType;
import com.wenzhe.music.constants.PlayState;
import com.wenzhe.music.constants.PlayType;
import com.wenzhe.music.data.MusicInfo;
import com.wenzhe.music.helper.PlayHelper;
import com.wenzhe.music.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by wenzhe on 2016/4/30.
 */
public class PlayUi implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private View rootView;
    private Activity activity;

    private ImageButton btnPre,btnNext,btnPlay,btnShuffle,btnLoop;
    private TextView musicTitle,musicArtist, musicAlbum,currentDuration,totalDuration;
    private SeekBar seekBar;
    //private ImageView albumImg;
    private AlbumView albumView;

    //记录当前背景颜色
    private int currentColor;

    private RelativeLayout playContainer;

    public PlayUi(Activity activity, LayoutInflater inflater, ViewGroup container) {
        this.activity = activity;
        this.rootView = inflater.inflate(R.layout.play_fragment, container, false);
        initView(rootView, activity);
    }

    private void initView(View rootView, Activity activity) {

        playContainer = (RelativeLayout) rootView.findViewById(R.id.play_container);
        currentColor = activity.getWindow().getNavigationBarColor();
        playContainer.setBackgroundColor(currentColor);

        currentDuration = (TextView) rootView.findViewById(R.id.music_current_time);
        totalDuration = (TextView) rootView.findViewById(R.id.music_end_time);

        btnPre = (ImageButton) rootView.findViewById(R.id.music_pre);
        btnPre.setOnClickListener(this);

        btnNext = (ImageButton) rootView.findViewById(R.id.music_next);
        btnNext.setOnClickListener(this);

        btnPlay = (ImageButton) rootView.findViewById(R.id.music_play);
        btnPlay.setOnClickListener(this);

        btnShuffle = (ImageButton) rootView.findViewById(R.id.music_shuffle);
        btnShuffle.setOnClickListener(this);

        btnLoop = (ImageButton) rootView.findViewById(R.id.music_loop);
        btnLoop.setOnClickListener(this);

        musicTitle = (TextView) rootView.findViewById(R.id.music_title);
        musicArtist = (TextView) rootView.findViewById(R.id.music_artist);
        musicAlbum = (TextView) rootView.findViewById(R.id.music_album);

        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);

        albumView = (AlbumView) rootView.findViewById(R.id.album_view);
        albumView.init(BitmapFactory.decodeResource(activity.getResources(),R.mipmap.default_bg));

    }



    public View getView() {
        return rootView;
    }

    public void changeUi(MusicChangeAction action) {
        switch (action.getType()) {
            case MusicChangeAction.NEXT_MUSIC:
            case MusicChangeAction.PRE_MUSIC:
            case MusicChangeAction.CURRENT_MUSIC:
                setMusicInfo((MusicInfo) action.getInfo());
                break;
            case MusicChangeAction.STATE_CHANGED:
                if (action.getInfo() == PlayHelper.STATE_PLAYING) {
                    btnPlay.setImageResource(R.mipmap.widget_pause_normal);
                } else {
                    btnPlay.setImageResource(R.mipmap.widget_play_normal);
                }
                break;
        }
    }

    private void setMusicInfo(MusicInfo info) {
        musicTitle.setText(info.getTitle());
        musicArtist.setText(info.getArtist());
        musicAlbum.setText(info.getAlbum());

        totalDuration.setText(MediaUtils.formatTime(info.getDuration()));

        seekBar.setMax((int) info.getDuration());
    }

    public void updateSeekBar(int progress) {
        seekBar.setProgress(progress);
        currentDuration.setText(MediaUtils.formatTime(progress));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_pre:
                EventBus.getDefault().post(new PlayAction<>(PlayAction.PREVIOUS,null));
                break;
            case R.id.music_next:
                EventBus.getDefault().post(new PlayAction<>(PlayAction.NEXT, null));
                break;
            case R.id.music_play:
                EventBus.getDefault().post(new PlayAction<>(PlayAction.BUTTON_PLAY, null));
                break;
        }
    }

    public void setBitmapAndColor(int color, Bitmap bitmap,String type) {
        albumView.setImageWithAnimation(bitmap,type);
        getValueAnimator(currentColor, color).start();
        currentColor = color;
    }

    public ValueAnimator getValueAnimator(int from, int to) {
        ValueAnimator animator = ValueAnimator.ofArgb(from, to);
        animator.setDuration(AppConstant.ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int)animation.getAnimatedValue();
                playContainer.setBackgroundColor(color);
                activity.getWindow().setNavigationBarColor(color);
            }
        });
        return animator;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        EventBus.getDefault().post(new PlayAction<>(PlayAction.STOP_TIMER,null));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        EventBus.getDefault().post(new PlayAction<>(PlayAction.SEEKBAR_CHANGE,seekBar.getProgress
                ()));
        EventBus.getDefault().post(new PlayAction<>(PlayAction.START_TIMER,null));
    }
}
