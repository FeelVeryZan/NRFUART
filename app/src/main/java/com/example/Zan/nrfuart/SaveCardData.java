package com.example.Zan.nrfuart;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by nodgd on 2017-09-17.
 */

public class SaveCardData {
    private int identifier = 0;
    private String title = null;
    private int id = 0;
    private String content = "";
    private SaveRunner Save;
    private Thread Savethread;
    private int Channelnum;
    private int Channellist[];

    public void setChannelnum(int channelnun) {
        Channelnum = channelnun;
    }

    public int getChannelnum() {
        return Channelnum;
    }

    public void setChannellist(int channellst[]) {
        Channellist = channellst.clone();
    }

    public int[] getChannellist() {
        return Channellist;
    }

    public SaveCardData(SaveCardData SCD) {
        super();
        Channellist = SCD.getChannellist().clone();
    }

    public SaveCardData() {
        super();
    }

    public void Start() {
        Save = new SaveRunner(identifier, Channelnum, Channellist);
        Savethread = new Thread(Save);
        Savethread.start();
    }

    public void shutdown() {
        broadcastUpdate(SaveRunner.SaveRunner_Off, String.valueOf(identifier));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        if (title == null || title.equals("")) {
            return "Send Card Title";
        }
        return title;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdInString() {
        return "" + id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void addContent(String moreContent) {
        content = content + moreContent;
    }

    public String getContent() {
        return content;
    }

    private void broadcastUpdate(final String action, String name) {
        final Intent intent = new Intent(action);
        intent.putExtra("name", name);
        LocalBroadcastManager.getInstance(MyApplication.getContext()).sendBroadcast(intent);
    }
}
