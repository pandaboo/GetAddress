package Component;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Contacts extends AppCompatActivity {

    public JSONArray getContacts() {
        ArrayList<Map<String, String>> dataList;
        JSONArray jsonContacts = null; // 전화번호부에서 이름, 전화번호를 받아와 jsonarray형태로 변환 후 web단으로 넘기기 위한 변수

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

        return jsonContacts;
    }

//    public void addContacts() {
//        new Thread(){
//            @Override
//            public void run() {
//
//                ArrayList<ContentProviderOperation> list = new ArrayList<>();
//                try{
//                    list.add(
//                            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                                    .build()
//                    );
//
//                    list.add(
//                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//
//                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "홍길동")   //이름
//
//                                    .build()
//                    );
//
//                    list.add(
//                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//
//                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "010-1234-5678")           //전화번호
//                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE  , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)   //번호타입(Type_Mobile : 모바일)
//
//                                    .build()
//                    );
//
//                    list.add(
//                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//
//                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//                                    .withValue(ContactsContract.CommonDataKinds.Email.DATA  , "hong_gildong@naver.com")  //이메일
//                                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE  , ContactsContract.CommonDataKinds.Email.TYPE_WORK)     //이메일타입(Type_Work : 직장)
//
//                                    .build()
//                    );
//
//                    getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list);  //주소록추가
//                    list.clear();   //리스트 초기화
//                }catch(RemoteException e){
//                    e.printStackTrace();
//                }catch(OperationApplicationException e){
//                    e.printStackTrace();
//                }
//            }
//        }.start();
////    출처: http://ghj1001020.tistory.com/4 [혁준 블로그]
//    }
}
