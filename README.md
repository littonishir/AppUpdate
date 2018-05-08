---
layout:     post
title:      "Android app版本更新"
subtitle:   "app update"
date:       2018-05-08 12:00:00
author:     "littonishir"
header-img: "img/2017-10-15-ps-one.jpg"
header-mask: 0.5
catalog:    true
tags:
	- Android
---
**<font size="5">  </font>**

# Android app版本更新
公司app有一个需求：当用户在Wi-Fi环境中打开app发现有更新，后台默默下载好安装包进行安装。

## 1. UpdataAppManger(工具类)

```java
package com.littonishir.appupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * auth : littonishir
 * date : 2018/5/8
 * gith : https://github.com/littonishir
 * APP更新管理器
 * 建议放在服务里面启动
 */

public class UpdataAppManger {
	private DownloadManager downloadManager;
	private Context mContext;
	private long mTaskId;
	private String downloadPath;
	private String versionName;
	private String tag = "littonishir";

	public UpdataAppManger(Context context) {
		this.mContext = context;

	}

	//广播接收者，接收下载状态
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkDownloadStatus();//检查下载状态
		}
	};

	//使用系统下载器下载
	public void downloadAPK(String versionUrl, String versionName) {
		this.versionName = versionName;
		//将下载请求加入下载队列
		downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		//创建下载任务
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
		request.setAllowedOverRoaming(false);//漫游网络是否可以下载
		//设置文件类型，可以在下载结束后自动打开该文件
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
		request.setMimeType(mimeString);//加入任务队列
		//在通知栏显示，默认就是显示的  VISIBILITY_HIDDEN 表示隐藏
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setAllowedOverRoaming(false);
		request.setVisibleInDownloadsUi(false);
		//sdcard的目录下的download文件夹，必须设置
		//request.setDestinationInExternalPublicDir("/sdcard/download", versionName);
		//自定义路径的方法
		request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, versionName);
		//加入下载列后会给该任务返回一个long型的id,通过该id可以取消任务，重启任务等等
		mTaskId = downloadManager.enqueue(request);
		//注册广播接收，监听下载状态
		mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

	}


	//检查下载状态
	private void checkDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(mTaskId);//赛选下载任务，传入任务ID,可变参数
		Cursor cursor = downloadManager.query(query);
		if (cursor.moveToFirst()) {
			int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			switch (status) {
				case DownloadManager.STATUS_PAUSED:
					//下载暂停
					Log.d(tag, "下载暂停");
					break;
				case DownloadManager.STATUS_PENDING:
					//下载延迟
					Log.d(tag, "下载延迟");
					break;
				case DownloadManager.STATUS_RUNNING:
					//正在下载
					Log.d(tag, "正在下载");
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					Toast.makeText(mContext, "下载完成", Toast.LENGTH_LONG).show();
					//打开文件进行安装
					installAPK(mTaskId);
					break;
				case DownloadManager.STATUS_FAILED:
					//下载失败
					Log.d(tag, "下载失败");
					Toast.makeText(mContext, "更新失败", Toast.LENGTH_LONG).show();
					break;

			}
		}
		cursor.close();
	}

	//下载到本地后执行安装根据获得的id进行安装
	protected void installAPK(long appId) {
		Intent install = new Intent(Intent.ACTION_VIEW);
		Uri downloadFileUri = downloadManager
				.getUriForDownloadedFile(appId);
		install.setDataAndType(downloadFileUri,
				"application/vnd.android.package-archive");
		install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		/**
		 * 目标应用临时授权[适配8.0]
		 * 注意：build.gradle中的targetSdkVersion
		 * 27是调不起来安装的这里我们用的25
		 * 暂未弄清楚原因欢迎指教及时更新
		 */
		install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		mContext.startActivity(install);
	}
}
```

## 2. AndroidManifest(申请权限)

```xml
	<!-- SD卡读写权限 -->
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
	<!-- 隐藏状态栏权限 -->
	<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"/>
	<!-- 访问网络的权限 -->
	<uses-permission android:name="android.permission.INTERNET"/>
```
## 3. 用法
详情请见源码

```java
	//创建 UpdataAppManger
	UpdataAppManger  updatamanger = new UpdataAppManger(this);
	//mUrl:下载地址 "花瓣"：app名字
	updatamanger.downloadAPK(mUrl, "花瓣");
```
>[源码下载](https://github.com/littonishir/AppUpdate)

	  
