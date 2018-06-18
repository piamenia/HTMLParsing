package kr.co.hoon.a180524htmlparsing;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // ListView를 사용하기 위한 인스턴스 변수
    ListView listView;
    ArrayList<String> data;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 인스턴스변수에 객체를 대입하고 ListView 출력
        data = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // 옵션
        listView.setDivider(new ColorDrawable(Color.parseColor("#cceecc")));
        listView.setDividerHeight(3);

        findViewById(R.id.btn).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                ParseThread th = new ParseThread();
                th.start();
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // adapter에 연결된 ListView에게 데이터가 변경됐으니 다시 출력하라는 알림
            adapter.notifyDataSetChanged();
        }
    };

    class ParseThread extends Thread {
        @Override
        public void run() {
            try{
                String addr = "http://finance.naver.com";
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "EUC-KR"));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if(line==null) break;
                    sb.append(line);
                }
                br.close();
                conn.disconnect();

                // 다 받으면 String으로 변환
                String html = sb.toString();

                // HTML파싱을 위해 html 문자열을 메모리에 트리 형태로 펼치기
                Document doc = Jsoup.parse(html);
                // 원하는 데이터 가져오기 h2 안에 있는 a 태그의 텍스트
                Elements tags = doc.select("a");
                // 데이터 가져오기
                for(Element temp : tags){
                    // 내용은 text, 속성은 attr("속성이름")
                    data.add(temp.text());
                }
                // 핸들러에게 메시지전송
                handler.sendEmptyMessage(0);

            }catch (Exception e){
                Log.e("예외", e.getMessage());
            }
        }
    }
}
