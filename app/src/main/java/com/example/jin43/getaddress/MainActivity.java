package com.example.jin43.getaddress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* 작성자 : 김정진
 * Native 전화번호부에서 이름과 전화번호 가져오기
 * 참고한 블로그 : http://mainia.tistory.com/4924 [녹두장군 - 상상을 현실로]
 */
public class MainActivity extends AppCompatActivity {

    WebView web; // 웹뷰 선언
    TextView testtx;
    ArrayList<Map<String, String>> dataList;
    JSONObject jsonData; // 자바스크립트에서 값을 받을 json 변수 선언
    JSONArray jsonContacts; // 전화번호부에서 이름, 전화번호를 받아와 jsonarray형태로 변환 후 web단으로 넘기기 위한 변수

    private final Handler handler = new Handler();

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = (WebView) findViewById(R.id.webView); //웹뷰 선언
        testtx = (TextView) findViewById(R.id.testTx);

        // 웹뷰 세팅
        WebSettings webSet = web.getSettings();                   // 웹뷰 설정
        webSet.setJavaScriptEnabled                     (true) ; // 자바스크립트 허용
        webSet.setLoadWithOverviewMode(true);
        webSet.setUseWideViewPort                       (true) ; // 웹뷰에 맞게 출력하기
        webSet.setBuiltInZoomControls                   (false); // 안드로이드 내장 줌 컨트롤 사용 X
        webSet.setAllowUniversalAccessFromFileURLs      (true) ; // file://URL이면 어느 오리진에 대해서도 Ajax로 요청을 보낼 수 있다.
        // API 레벨 16부터 이용할 수 있다.
        webSet.setJavaScriptCanOpenWindowsAutomatically (true) ; // javascript 가  window.open()을 사용할 수 있도록 설정
        webSet.setSupportMultipleWindows                (true) ; // 여러개의 윈도우를 사용할 수 있도록 설정
        webSet.setSaveFormData                          (false); // 폼의 입력값를 저장하지 않는다
        webSet.setSavePassword                          (false); // 암호를 저장하지 않는다.
        webSet.setLayoutAlgorithm                       (WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            web.setWebContentsDebuggingEnabled(true); 				//API 레벨 19부터 이용 가능.
        }
        web.addJavascriptInterface(new WebAppInterface(this), "android");

        web.loadUrl("file:///android_asset/html/index.html"); // 처음 로드할 페이지

    }

    public void getNativeContacts() {
        Log.d("JJKIM", "Native영역 전화번호부 정보 가져오기");
        dataList = new ArrayList<Map<String, String>>();
        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");

        while (c.moveToNext()) {
            HashMap<String, String> map = new HashMap<String, String>();
            // 연락처 id 값
            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            // 연락처 대표 이름
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            map.put("name", name);

            // ID로 전화 정보 조회
            Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null, null);

            // 데이터가 있는 경우
            if (phoneCursor.moveToFirst()) {
                String number = phoneCursor.getString(phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                map.put("phone", number);
            }

            phoneCursor.close();
            dataList.add(map);
        }// end while
        c.close();

        // JSON 으로 변환
        try {
            jsonContacts = new JSONArray();//배열이 필요할때
            for (int i = 0; i < dataList.size(); i++) {
                JSONObject tempContacts = new JSONObject();//배열 내에 들어갈 json
                tempContacts.put("name", dataList.get(i).get("name"));
                tempContacts.put("phonenum", dataList.get(i).get("phone"));
                jsonContacts.put(tempContacts);
            }

            Log.d("JSON Test", jsonContacts.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        출처: http://mainia.tistory.com/5673 [녹두장군 - 상상을 현실로]

        handler.post(new Runnable() {
            @Override
            public void run() {
                String args = null;
                if(jsonContacts != null) args = jsonContacts.toString();
                Log.d("JJKIM", "jsonContacts = "+ args);
                web.loadUrl("javascript:printContacts('" + args + "')"); // 해당 url의 자바스크립트 함수 호출
            }
        });
    }

    /* 안드로이드와 자바스크립트간의 데이터 주고 받기 */
    public class WebAppInterface {
        Context mContext;


        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /* 전화번호부 가져오기*/
        @JavascriptInterface
        public void callContacts() {
            Log.d("JJKIM", "Web JS에서 MainActivity쪽 function 호출");
            getNativeContacts();
        }

        /* 푸시알림*/
        @JavascriptInterface
        public void callPush() {
            Log.d("JJKIM", "Web JS에서 MainActivity쪽 function 호출");
            getNativeContacts();
        }
    }
}
