package com.example.Zan.nrfuart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by nodgd on 2017/09/19.
 */

public class BlurBitmap {

    public static Bitmap printScreen(Context context) {
        View view = ((Activity) context).getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bmp1 = view.getDrawingCache();
        //获取屏幕有效部分的高度范围
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int contentTop = ((Activity) context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentTop - statusBarHeight;
        int topHeight = statusBarHeight + titleBarHeight;
        WindowManager wm = ((Activity) context).getWindowManager();
        int bottomHeight = wm.getDefaultDisplay().getHeight();
        //生成子图
        return Bitmap.createBitmap(bmp1, 0, topHeight, bmp1.getWidth(), bottomHeight - topHeight);
    }

    //对一张图片进行高斯模糊
    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float blurRadius) {

        // 用需要创建高斯模糊bitmap创建一个空的bitmap
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 初始化Renderscript，该类提供了RenderScript context，创建其他RS类之前必须先创建这个类，其控制RenderScript的初始化，资源管理及释放
        RenderScript rs = RenderScript.create(context);

        // 创建高斯模糊对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 创建Allocations，此类是将数据传递给RenderScript内核的主要方法，并制定一个后备类型存储给定类型
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //设定模糊度(注：Radius最大只能设置25.f)
        blurScript.setRadius(blurRadius);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        // recycle the original bitmap
        bitmap.recycle();

        // After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }
}
