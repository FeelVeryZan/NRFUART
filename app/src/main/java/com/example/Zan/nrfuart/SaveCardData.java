package com.example.Zan.nrfuart;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by nodgd on 2017-09-17.
 */

public class SaveCardData extends BaseCardData {

    public static final String TAG = "SaveCardData";

    private int channelNumber;
    private int[] channelList;
    private String content;

    public SaveCardData() {
        super();
        content = "";
    }

    public SaveCardData(SaveCardData SCD) {
        super();
        channelList = SCD.getChannellist().clone();
    }

    @Override
    public String getTitle() {
        if (title.equals("")) {
            return "No Tag Saver";
        } else {
            return title + " (Saver)";
        }
    }

    //通道数目
    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    //通道集合
    public void setChannelList(int channelList[]) {
        this.channelList = channelList.clone();
    }

    public int[] getChannellist() {
        return channelList;
    }

    public String getChannelList(){
        String SS="";
        for (int i=0;i<channelList.length;i++)
            if (channelList[i]==1)
            SS=SS+String.valueOf(i)+',';
        return SS;
    }


    //设置初始内容、增加内容、清空内容
    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    public void addContent(String moreContent) {
        content = content + moreContent;
    }

    public void clearContent() {
        content = "";
    }

    public String getContent() {
        return content;
    }

    //开启和结束存储线程
    public void startSaveThread() {
        new Thread(new SaveRunner(getIdentifier(), channelNumber, channelList)).start();
    }

    public void stopSaveThread() {
        Intent intent = new Intent(SaveRunner.SaveRunner_Off);
        intent.putExtra("name", String.valueOf(getIdentifier()));
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }
}
