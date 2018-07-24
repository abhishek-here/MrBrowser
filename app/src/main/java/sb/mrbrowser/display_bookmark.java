package sb.mrbrowser;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.opengl.Visibility;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class display_bookmark extends AppCompatActivity {
    Button open,delete;
    private DBHelper databaseHelper;
    ListView listView;
    ArrayAdapter <String> adapter;
    ArrayList <String> arrayList,displayList;
    static String i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_bookmark);
        databaseHelper=new DBHelper(this);
        listView= (ListView) findViewById(R.id.lv);
        open=(Button)findViewById(R.id.open);
        delete=(Button)findViewById(R.id.delete);
        open.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        arrayList=new ArrayList<String>();
        displayList=new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(adapter);
        display();
  //handles long click on list of saved bookmarks
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                i=displayList.get(position).toString();
                parent.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
                view.setBackgroundColor(Color.LTGRAY);
                open.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                return false;
            }
        });
        //button to delete saved bookmark from database
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteRow(i);
                adapter.clear();
                displayList.clear();
                open.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                display();
            }
        });
        //open saved bookmark
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(display_bookmark.this,MainActivity.class);
                in.putExtra("urlBookmark",i);
                startActivity(in);
                finish();
            }
        });
    }
    //this method fetch saved bookmark from database and display in listview
    public void display(){
        Cursor data=databaseHelper.getAllData();
        data.moveToFirst();
        if (data.getCount()==0){
            return;
        }
        else {
        StringBuffer stringBuffer=new StringBuffer();
        while (data.moveToNext()){
            stringBuffer.append("NAME: "+data.getString(0)+"\n");
            arrayList.add(stringBuffer.toString());
            displayList.add(data.getString(1).toString());
            adapter.notifyDataSetChanged();
            stringBuffer.setLength(0);
             }
        }
    }
}
