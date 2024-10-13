package com.moew.tk;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText et;
    Button bt;
    Button  btnScanCode;
    WebView wv;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "已取消扫码", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    et.setText(result.getContents());
                    OpenUrlOrCode(result.getContents());
                }
            });

    private void openUrl(String url){
        if(url==null||url.isEmpty()){
            Toast.makeText(MainActivity.this,"不可以为空",Toast.LENGTH_LONG).show();
            return;
        }
        wv.loadUrl(url);
    }

    private final String BaseUrl = "http://music.zixing.fun:7049/";
    public interface CallBack { void postCallBack(String string); }

    private void post(String url, String data,CallBack callback) {

        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    RequestBody body = RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded"));
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        String ret = response.body().string();
                        callback.postCallBack(ret);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    callback.postCallBack("");
                }
            }
            }).start();
    }
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build();
    private void openCode(String code){
        if(code==null||code.isEmpty()){
            Toast.makeText(MainActivity.this,"不可以为空",Toast.LENGTH_LONG).show();
            return;
        }
        try{
            showLoading();
            post(BaseUrl + "geturl", "code=" + code, new CallBack() {
                @Override
                public void postCallBack(String url) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if("error".equals(url))
                                {
                                    Toast.makeText(MainActivity.this, "服务端发生未知错误！", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                else if(url.startsWith("not found code"))
                                {
                                    Toast.makeText(MainActivity.this, "找不到对应的Code！", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(url==null || url.isEmpty())
                                {
                                    Toast.makeText(MainActivity.this, "无法获取到对应的url，请检查网络！", Toast.LENGTH_LONG).show();
                                    return ;
                                }
                                Toast.makeText(MainActivity.this, "获取到url："+url, Toast.LENGTH_LONG).show();
                                openUrl(url);
                            }catch (Exception ex)
                            {
                                Toast.makeText(MainActivity.this, "发生错误:"+ex.getMessage(), Toast.LENGTH_LONG).show();
                                ex.printStackTrace();
                                return ;
                            }
                            finally {
                                closeLoading();
                            }
                        }
                    });
                }
            });
        }
        catch (Exception ex){
            Toast.makeText(MainActivity.this, "发生错误:"+ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();

        }
    }

    final PopupWindow popupWindow = new PopupWindow();
    private void showLoading()
    {

        et.setEnabled(false);
        bt.setEnabled(false);
        btnScanCode.setEnabled(false);

        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        View view = LayoutInflater.from(this).inflate(R.layout.popup,null);
        popupWindow.setContentView(view);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER,0,0);
    }
    private void closeLoading()
    {
        if(popupWindow!=null){
            try{

                et.setEnabled(true);
                bt.setEnabled(true);
                btnScanCode.setEnabled(true);
                popupWindow.dismiss();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    /**
     * 打开url或者code
     */
    private void OpenUrlOrCode(String url){
        if(url==null||url.isEmpty()){
            Toast.makeText(MainActivity.this,"不可以为空",Toast.LENGTH_LONG).show();
            return;
        }
        if(url.startsWith("http://") || url.startsWith("https://"))
        {
            openUrl(url);
        }
        if(url.length()<=6)
        {
            openCode(url);
        }else{
            openUrl(url);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et=findViewById(R.id.activitymainEditText1);
        bt=findViewById(R.id.activitymainButton1);
        btnScanCode=findViewById(R.id.btnScanCode);
        wv=findViewById(R.id.activitymainWebView1);
        WebSettings settings = this.wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setTextSize(WebSettings.TextSize.NORMAL);
        settings.setSupportZoom(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setAllowFileAccess(true);
        settings.setNeedInitialFocus(true);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setLoadsImagesAutomatically(true);
        settings.setDisplayZoomControls(false);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        //settings.setAppCacheEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= 21)
        {
            try
            {
                settings.setMixedContentMode(0);
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            Class<WebSettings> cls = WebSettings.class;
            try
            {
                Method method = cls.getMethod("setMixedContentMode", new Class[]{Integer.TYPE});
                if (method != null)
                {
                    method.invoke(settings, new Object[]{0});
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
        final MyWebViewActivity webClient = new MyWebViewActivity();
        this.wv.setWebViewClient(webClient);

        bt.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View p1)
            {
                String url=et.getText().toString();
                OpenUrlOrCode(url);
            }
        });

        btnScanCode.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View p1)
            {
                ScanOptions options = new ScanOptions();
                //options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
                options.setPrompt("扫描二维码");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                options.setOrientationLocked(true);
                barcodeLauncher.launch(options);
            }
        });
    }




    class MyWebViewActivity extends WebViewClient
    {




        @SuppressLint("NewApi")
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest)
        {

            try
            {
                String str = URLDecoder.decode(webResourceRequest.getUrl().toString(), "UTF-8");
                if (str.startsWith("jsbridge://CAPTCHA/onVerifyCAPTCHA"))
                {
                    onVerifyCAPTCHA(str.replaceAll("^jsbridge://CAPTCHA/onVerifyCAPTCHA\\?p=", "").replaceAll("#2$", ""));
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (super.shouldOverrideUrlLoading(webView, webResourceRequest))
            {
                return true;
            }
            if (webResourceRequest.getUrl().toString().startsWith("http") || webResourceRequest.getUrl().toString().startsWith("https"))
            {
                return super.shouldOverrideUrlLoading(webView, webResourceRequest);
            }

            try
            {
                Intent intent = new Intent("android.intent.action.VIEW", webResourceRequest.getUrl());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Toast.makeText(MainActivity.this, "您未安装请求打开的App...", Toast.LENGTH_LONG).show();
                return true;
            }






        }

        public void onVerifyCAPTCHA(String json)
        {
            try
            {
                JSONObject g = new JSONObject(json);
                int result=g.getInt("result");
                if (result == 0)
                {
                    AlertDialog a=new AlertDialog.Builder(MainActivity.this).create();
                    TextView tv=new TextView(MainActivity.this);
                    tv.setText(g.getString("ticket"));
                    tv.setTextIsSelectable(true);
                    a.setView(tv);
                    a.show();
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }


    }

}
