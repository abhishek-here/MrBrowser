package sb.mrbrowser;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karan.churi.PermissionManager.PermissionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity  {
    PermissionManager permissionManager;
    SharedPreferences sharedpreferences;

    EditText url;
    WebView webView;
   ImageButton capture;
   ImageButton pop;
    ImageButton cancel;
    //ImageButton hide;
    //Button show;
    //ImageButton forward,bookmark;
   // ImageButton exit;
    boolean gone=false;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    static String saveUrl;
    static int check;
    static String saveTitle;
    private DBHelper databaseHelper;
    private SwipeRefreshLayout swipeLayout;
    static String combine;

    public static final String mypreference = "mypref";
    public static final String Name = "nameKey";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if device is connected to internet

        if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {

            }
      //get runtime permissions
        permissionManager =new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        databaseHelper=new DBHelper(this); // DBHelper is class to handle database
        sharedpreferences = getSharedPreferences(mypreference,Context.MODE_PRIVATE);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        frameLayout=(FrameLayout)findViewById(R.id.frame);
        progressBar=(ProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);

        webView=(WebView)findViewById(R.id.wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        on();
       //swipe to refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //your method to refresh content
                if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
                else {
                    webView.reload();
                }

            }
        });

        //download manager
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setMimeType(mimeType);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });


        checkCondition(check);

        //handle buttons
        cancel=(ImageButton)findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
            }
        });
        pop=(ImageButton)findViewById(R.id.mnu);
        webView.setWebViewClient(new NewClient());
        capture=(ImageButton)findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });

         //handling webview
        webView.setWebChromeClient(new WebChromeClient(){
        public void onProgressChanged(WebView view,int progress){
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            pop.setVisibility(View.GONE);
            cancel.setVisibility(View.VISIBLE);
            if (webView.getUrl().contains("http://"))
            {
                url.setText("|Not Secure| "+ webView.getUrl());
            }else
            {
                url.setText(webView.getUrl());
            }

           //progress bar
            setTitle("Loading......");
            if (progress==100){
                frameLayout.setVisibility(View.GONE);
                pop.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                setTitle(getTitle());

                if (swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                saveTitle=webView.getTitle();
                saveUrl=webView.getUrl();

            }
            super.onProgressChanged(view, progress);
        }
        });

        //text input field
        url=(EditText)findViewById(R.id.et1);
        url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                          @Override
                                          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                              if (actionId== EditorInfo.IME_ACTION_SEARCH){
                                                  performSearch();
                                                  return true;
                                              }
                                              return false;
                                          }
                                      }

        );

        url.setTextColor(Color.parseColor("#FFFFFF"));
        url.setSelectAllOnFocus(true);
        url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    view.clearFocus();
                    view.requestFocus();
            }
        });

        if(getIntent().getData()!=null){//check if intent is not null
            Uri data = getIntent().getData();//set a variable for the Intent
            String scheme = data.getScheme();//get the scheme (http,https)
            String fullPath = data.getEncodedSchemeSpecificPart();//get the full path -scheme - fragments
            combine = scheme+"://"+fullPath; //combine to get a full URI
        }

        if(combine!=null){//if combine variable is not empty then navigate to that full path
            url.setText(combine);
            performSearch();

        }

    }
    //alert dialog box to display message if internet not available
    public AlertDialog.Builder buildDialog(MainActivity mainActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder;
    }
    //method which actually perform check if internet is available(mobile data or wifi)
    public boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }


   //this method launches webview
    private void on() {
        if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {
            if (sharedpreferences.contains(Name)) {
                webView.loadUrl(sharedpreferences.getString(Name,""));
            }
            else {
                webView.loadUrl("https://www.google.co.in/");
            }

        }

    }
    //this method handles text from edit text used as search bar
    private void performSearch() {
        if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {
            String website = url.getText().toString();
            if (url.getText().toString().contains("http://")
                    || url.getText().toString()
                    .contains("https://")) {
                webView.loadUrl(website);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(url.getWindowToken(),
                        0);
            } else if (url.getText().toString().contains(".com")
                    || url.getText().toString().contains(".net")
                    || url.getText().toString().contains(".org")
                    || url.getText().toString().contains(".gov")
                    || url.getText().toString().contains(".in")) {
                webView.loadUrl("http://"
                        + website);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(url.getWindowToken(),
                        0);
            } else {
                webView.loadUrl("https://www.google.com/search?q="
                        + website);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(url.getWindowToken(),
                        0);
            }
        }
    }

    //this method handles back button
    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        }
    //three dot popup menu
    public void popup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                                             public boolean onMenuItemClick(MenuItem item) {
                                                 switch (item.getItemId()) {
                                                     case R.id.back:
                                                         if (webView.canGoBack())
                                                             webView.goBack();
                                                         return true;
                                                     case R.id.forward:
                                                         if (webView.canGoForward())
                                                             webView.goForward();
                                                         return true;
                                                     case R.id.reload:
                                                         if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
                                                         else {
                                                             webView.reload();
                                                         }
                                                         return true;
                                                     case R.id.clear:
                                                         android.webkit.CookieManager cookieManager = CookieManager.getInstance();
                                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                             cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                                                                 // a callback which is executed when the cookies have been removed
                                                                 @Override
                                                                 public void onReceiveValue(Boolean aBoolean) {
                                                                 }
                                                             });
                                                         }
                                                         else cookieManager.removeAllCookie();
                                                         return true;
                                                     case R.id.addBook:
                                                         check=1;
                                                         boolean result= databaseHelper.insertData(saveTitle,saveUrl);
                                                         if (result)
                                                             Toast.makeText(MainActivity.this,"Bookmark Saved",Toast.LENGTH_SHORT).show();
                                                         else
                                                             Toast.makeText(MainActivity.this,"Already Saved",Toast.LENGTH_SHORT).show();
                                                         return true;
                                                     case R.id.bookmark:
                                                         Intent i=new Intent(MainActivity.this,display_bookmark.class);
                                                         startActivity(i);
                                                         return true;
                                                     case R.id.home_page:
                                                         try {
                                                             SharedPreferences.Editor editor = sharedpreferences.edit();
                                                             editor.putString(Name,webView.getUrl());
                                                             editor.commit();
                                                         }
                                                         catch (Exception e){}
                                                         Toast.makeText(MainActivity.this, "Homepage Set", Toast.LENGTH_SHORT).show();
                                                         return true;
                                                     case R.id.quit:
                                                         finish();
                                                         return true;
                                                     default:
                                                         return false;
                                                 }
                                             }
        }
        );
        popup.show();
    }




    private class NewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String url){
           view.loadUrl(url);
            frameLayout.setVisibility(View.VISIBLE);
            return true;
        }
    }
    //load bookmark
    public void checkCondition(int check){
        if (check==1){
            Bundle bundle=getIntent().getExtras();
            String urlBookmark=bundle.getString("urlBookmark");
            if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
            else {
                webView.loadUrl(urlBookmark);
            }
        }
    }
    //method to capture screenshot programmatically
public void takeScreenshot(){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        try {
        // image naming and path  to include sd card  appending name you choose for file
        String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

        // create bitmap screen capture
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        File imageFile = new File(mPath);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
        openScreenshot(imageFile);

    } catch (Throwable e) {
        // Several error may come out with file handling or DOM
        e.printStackTrace();
    }
}

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);

    }



}