package com.xiangyanger.student;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiangyanger.student.R;
import com.xiangyanger.student.bean.GlobalDetailBean;
import com.xiangyanger.student.constant.Constants;
import com.xiangyanger.student.joinroom.view.JoinRoomActivity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GlobalDetailActivity extends Activity implements OnClickListener  {
	private ProgressDialog mPrgDlg = null;
	private WebView webView;
	WebSettings settings;

	private Context mContext;
	public GlobalDetailBean gdb;
	boolean webOn = false; // 需要在退出时 关掉结束web

	private long exitTime = 0;

	private RequestQueue mSingleQueue;

	public static String getAPPVersionNameFromAPP(Context ctx) {
		int currentVersionCode = 0;
        String appVersionName = null;
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            appVersionName  = info.versionName; // 版本名
			currentVersionCode = info.versionCode; // 版本号
			//System.out.println(currentVersionCode + " " + appVersionName);
		} catch (Exception e) {
			// TODO Auto-generated catch blockd
			e.printStackTrace();
		}
		return appVersionName;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		gdb = (GlobalDetailBean) this.getIntent().getSerializableExtra("gdb");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.global_detail_activity);
		StatusBarCompat.compat(this, 0xffdcdcdc); //#fffff0
		mContext = getBaseContext();

		if (webView == null) {
			webView = (WebView) this.findViewById(R.id.detail_web_webview);
		}
		mSingleQueue = Volley.newRequestQueue(getApplicationContext());
		//判断版本号
		StringRequest stringRequest = new StringRequest(Constants.version,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

						//这里需要手改吧
						String versionName  = getAPPVersionNameFromAPP(mContext);

						try {

							byte[] responseBytes = response.getBytes("iso8859-1"); //得到iso8859-1样式的编码

							String response_temp = new String(responseBytes);
                            response_temp = response_temp.replace("\r\n","");

							JSONObject jsonObject =   new JSONObject(response_temp);  // new JSONObject(response);
							String serverVersionName =  jsonObject.getString("ver");
                            //serverVersionName = "1.1";
							String info = jsonObject.getString("info");
							final String ref = jsonObject.getString("ref");

							Log.d("education->Student", "versionName:"+serverVersionName);

							if(versionName.compareTo(serverVersionName)<0)

							{
								AlertDialogCustom dialog =
										new AlertDialogCustom(GlobalDetailActivity.this).builder().setTitle("版本提示").setMsg(info).
												setPositiveButton("取消", new OnClickListener() {
													@Override
													public void onClick(View v) {

														finish();

                                                        Intent intent = new Intent(GlobalDetailActivity.this, GuideActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);

													}
												}).setNegativeButton("更新", new OnClickListener() {
											@Override
											public void onClick(View v) {

												finish();

												Intent intent1 = new Intent();
												intent1.setAction("android.intent.action.VIEW");
												Uri content_url = Uri.parse(ref);
												intent1.setData(content_url);
												startActivity(intent1);
											}
										});
								dialog.show();
								dialog.setCancelable(false);
							}



						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//Log.e("TAG", error.getMessage(), error);
			}
		});

		mSingleQueue.add(stringRequest);
	}

	public void onPause()
	{
		super.onPause();
		//webView.reload();
	}


	public void onResume(){
		super.onResume();
		webViewTask();
	}

	@SuppressLint("NewApi")
	protected void onDestroy()
	{
		super.onDestroy();

		if (webView != null)
		{
			try{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					webView.onPause(); // 暂停网页中正在播放的视频

				//  API 11之后，不显示缩放按钮
				webView.getSettings().setDisplayZoomControls(false);
				//  API 11之前，在finish时，从view层级中删除webview
				ViewGroup viewgroup = (ViewGroup)(webView.getParent());
				viewgroup.removeView(webView);

				webView.setVisibility(View.GONE);

				webView.destroy();

			}catch(Exception e){

			}
		}

		if(mPrgDlg != null)
			mPrgDlg.dismiss();

	}

	@SuppressLint({ "JavascriptInterface", "NewApi" })
	private void webViewTask() {

		// HttpUtils.synCookiesForWebView(mContext);

		if (webOn == true)
			return;

		webOn = true;
		settings = webView.getSettings();
		// HttpUtils.synCookiesForWebView(mContext);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setAppCacheEnabled(false);
		settings.setDomStorageEnabled(false);

	/*
		if(true)
		{
			settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
			settings.setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
			settings.setSupportZoom(true);//是否可以缩放，默认true
			settings.setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
			settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
			settings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
			settings.setAppCacheEnabled(false);//是否使用缓存
			settings.setDomStorageEnabled(true);//DOM Storage
			// displayWebview.getSettings().setUserAgentString("User-Agent:Android");//设置用户代理，一般不用
		}else{		// 以下两行屏幕自适应
			settings.setUseWideViewPort(true);
			settings.setLoadWithOverviewMode(true);
			settings.setPluginState(android.webkit.WebSettings.PluginState.ON);
			settings.setSupportZoom(false);
			settings.setBuiltInZoomControls(false);
			settings.setDisplayZoomControls(false);
			settings.setJavaScriptEnabled(true);
			settings.setJavaScriptCanOpenWindowsAutomatically(true);
			settings.setAllowFileAccess(true);
			settings.setDefaultTextEncodingName("UTF-8");
		}
*/


		settings.setAllowFileAccess(true);
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setDisplayZoomControls(false);
		settings.setPluginState(android.webkit.WebSettings.PluginState.ON);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
		settings.setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
		settings.setSupportZoom(false);//是否可以缩放，默认true
		settings.setBuiltInZoomControls(false);//是否显示缩放按钮，默认false
		settings.setUseWideViewPort(false);//设置此属性，可任意比例缩放。大视图模式
		settings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
		settings.setAppCacheEnabled(false);//是否使用缓存
		settings.setDomStorageEnabled(true);


		webView.setDownloadListener(new MyWebViewDownLoadListener());

		mPrgDlg = new ProgressDialog(this) {
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					webView.stopLoading(); // 停止加载
					mPrgDlg.dismiss();
				}
				return super.onKeyDown(keyCode, event);
			}

		};
		mPrgDlg.setCanceledOnTouchOutside(false);

		webView.loadUrl(gdb.URL);

		//webView.setWebChromeClient(new WebChromeClient() {});

		webView.setWebChromeClient(new ReWebChomeClient(new myOpenFileChooserCallBack()));

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
				handler.proceed(); // 接受网站证书
			}


			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("tel:")){

					Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
					startActivity(intent);

				} else if(url.startsWith("http:") || url.startsWith("https:")) {

					if (url.startsWith("https://wx.tenpay.com")) {

						Map<String, String> extraHeaders = new HashMap<String, String>();
						extraHeaders.put("Referer", Constants.Referer);
						view.loadUrl(url, extraHeaders);

					} else {

						view.loadUrl(url);

					}

				}else if(url.startsWith("weixin://wap/pay?")){

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
					return true;

				}else if(url.startsWith("jxaction:")){

					//Toast.makeText(getApplicationContext(), url,Toast.LENGTH_SHORT).show();

					//jxaction://joinroom?sdkAppid=1400102667&accountType=29179&roomid=0621001&indentifer=testStudent&usg=eJxlj8FOg0AQhu88xYZrjRmWhYI3wCqVWm2pjfRCoLu0q4VuYKnUxnfXYhM3ceY235f58580hJC*mMTX2Xq9byuZyqNgOrpBOuhXf1AITtNMpmZN-0HWCV6zNCskq3toWJaFAVSHU1ZJXvCLIVkjY9mej4rU0Pe0T-r9QgAMwLY9VBW*6eHjKAnGs1vqB89Bcpd5TgLgRZ9WSN4cf758uheduXI5XuFiW*zygf0x3noPrXMgr8M8fyGDHR1F8fQ4bSOxCbs5gSpwl4uw5K5fTppIiZS8ZJda2CXOz9oKPbC64fuqFzAYloFNOI*ufWnfdRtecw__

					if(!url.contains("joinroom?")) return true;

					url = url.substring(url.indexOf("?")+1);
					String spl[] = url.split("&");

					for(String tmp : spl){

						String spli[] = tmp.split("=");

                         if(tmp.startsWith("sdkAppid=")){

							 JoinRoomActivity.sdkAppid = Integer.parseInt(spli[1]);

						 }else if(tmp.startsWith("accountType=")){

							 JoinRoomActivity.accountType = Integer.parseInt(spli[1]);

						 }else if(tmp.startsWith("roomid=")){

							 JoinRoomActivity.roomid = Integer.parseInt(spli[1]);

						 }else if(tmp.startsWith("indentifer=")){

							 JoinRoomActivity.indentifer = spli[1];

						 }else if(tmp.startsWith("userSig=")){

							 JoinRoomActivity.userSig = spli[1];

						 }

					}

					Intent intent = new Intent(mContext, JoinRoomActivity.class);
					//intent.putExtra("gdb", gdb);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);

				}

				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (mPrgDlg != null) {
					mPrgDlg.setMessage("正在加载.....");
					mPrgDlg.show();
				}
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				if (mPrgDlg != null)
					mPrgDlg.dismiss();
				super.onPageFinished(view, url);
			}

		});

		//webView.addJavascriptInterface(new DemoJavaScriptInterface(), "demo");
	}


	// 注入js函数监听
	private void addImageClickListner() {
		// 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，函数的功能是在图片点击的时候调用本地java接口并传递url过去
		webView.loadUrl("javascript:(function(){" +
				"var objs = document.getElementsByTagName(\"img\"); " +
				"for(var i=0;i<objs.length;i++)  " +
				"{"
				+ "    objs[i].onclick=function()  " +
				"    {  "
				+ "        window.demo.openImage(this.src);  " +
				"    }  " +
				"}" +
				"})()");
	}

	final class DemoJavaScriptInterface {
		DemoJavaScriptInterface() {
		}

		@JavascriptInterface // 在5.0上作用
		public void clickOnAndroid(int number) { // 注意这里的名称。它为clickOnAndroid(),注意，注意，严重注意
			//Toast.makeText(getBaseContext(), "clickOnAndroid" + number, Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface // 在5.0上作用
		public void OnTop(int number) { // 注意这里的名称。它为clickOnAndroid(),注意，注意，严重注意
			//Toast.makeText(getBaseContext(), "你已经上天啦，歇一会儿吧~", Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface // 在5.0上作用
		public void openImage(String img) {

			//这里可以打开图片
		}
	};

	private class MyWebViewDownLoadListener implements DownloadListener {
		@Override
		public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
									long contentLength) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}



	////////////////////////////////////////////////////////////////////////////

	Uri tmpUri;


	private Intent createDefaultOpenableIntent() {
		// Create and return a chooser with the default OPENABLE
		// actions including the camera, camcorder and sound
		// recorder where available.

		tmpUri  = null;

		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");

		Intent chooser = createChooserIntent(createCameraIntent());//,// createCamcorderIntent(),
		// createSoundRecorderIntent());
		chooser.putExtra(Intent.EXTRA_INTENT, i);
		return chooser;
	}

	private Intent createChooserIntent(Intent... intents) {
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
		chooser.putExtra(Intent.EXTRA_TITLE, "选择文件方式");
		return chooser;
	}

	private Intent createCameraIntent() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File externalDataDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM);
		File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
				File.separator + "Ephotos");
		cameraDataDir.mkdirs();
		String mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
				System.currentTimeMillis() + ".jpg";

		tmpUri = Uri.fromFile(new File(mCameraFilePath));

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);
		return cameraIntent;
	}

	private Intent createCamcorderIntent() {
		return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	}

	private Intent createSoundRecorderIntent() {
		return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
	}

	// tmpUri = Uri.fromFile(mCameraPicture);

	@TargetApi(Build.VERSION_CODES.M)
	public  boolean requesCallPhonePermissions(int requestCode)
	{

		if(requestCode == 200){

			if ( checkSelfPermission("android.permission.CAMERA")!= PackageManager.PERMISSION_GRANTED
					|| checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED )  {
				requestPermissions( new String[]{"android.permission.CAMERA","android.permission.READ_EXTERNAL_STORAGE"},200);
				return false;
			}

		}else{

			if ( checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED	)  {

				requestPermissions( new String[]{"android.permission.READ_EXTERNAL_STORAGE"},201);
				return false;
			}

		}

		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 200: {  //摄像头
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED
						&&grantResults[1] == PackageManager.PERMISSION_GRANTED) {

					startActivityForResult(createDefaultOpenableIntent(), 0);

				} else {
//	        	 if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
//	    			 if( !requesCallPhonePermissions(200)){ //没有权限
//	    				 return;
//	    			 }
//	    			}
					Toast.makeText(this, "您有权限没被允许", Toast.LENGTH_SHORT).show();
					// permission denied, boo! Disable the
					// f用户不同意 可以给一些友好的提示
					//Log.i("用户不同意权限", "用户不同意权限!");
				}
				return;
			}

		}
	}

	public void showOptions() {

		if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){

			if( !requesCallPhonePermissions(200)){ //没有权限
				return;
			}

			startActivityForResult(createDefaultOpenableIntent(), 0);

		}
	}


	ValueCallback<Uri> mUploadMsg;
	ValueCallback<Uri[]> mUploadMessageForAndroid5;


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK) {
			if (mUploadMsg != null) {
				mUploadMsg.onReceiveValue(null);
				mUploadMsg = null;
			}

			if (mUploadMessageForAndroid5 != null) {
				mUploadMessageForAndroid5.onReceiveValue(null);
				mUploadMessageForAndroid5 = null;
			}
			return;
		}
		if (null != mUploadMessageForAndroid5)
		{
			Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
			if (result != null) {
				mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
			} else {

				if(tmpUri != null)
					mUploadMessageForAndroid5.onReceiveValue(new Uri[]{tmpUri});
				else
					mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
			}
			mUploadMessageForAndroid5 = null;

			return;
		}


		if (null != mUploadMsg)
		{
			Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
			if (result != null) {
				mUploadMsg.onReceiveValue(result);
			} else {

				if(tmpUri != null)
					mUploadMsg.onReceiveValue(tmpUri);
				else
					mUploadMsg.onReceiveValue(null);
			}
			mUploadMessageForAndroid5 = null;

			return;
		}

	}

	/**
	 * 自定义接口  方便MainActivity调用
	 */

	public interface OpenFileChooserCallBack {
		void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);
		void openFileChooserCallBack(ValueCallback<Uri[]> uploadMsg,WebChromeClient.FileChooserParams fileChooserParams);
	}

	class myOpenFileChooserCallBack implements OpenFileChooserCallBack {

		public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg,String acceptType) {
			mUploadMsg = uploadMsg;
			showOptions();
		}

		public void openFileChooserCallBack(ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
			mUploadMessageForAndroid5 = uploadMsg;
			showOptions();
		}


	}

	public class ReWebChomeClient extends WebChromeClient  {

		private OpenFileChooserCallBack mOpenFileChooserCallBack;

		public ReWebChomeClient(OpenFileChooserCallBack openFileChooserCallBack) {
			mOpenFileChooserCallBack = openFileChooserCallBack;
		}

		// For Android < 3.0
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			openFileChooser(uploadMsg, "");
		}

		//For Android 3.0+
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
			mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
		}

		// For Android  > 4.1.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
			openFileChooser(uploadMsg, acceptType);
		}

		// For Android > 5.0
		public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
			mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg,fileChooserParams);
			return true;
		}
	}
}
