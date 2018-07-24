package sb.mrbrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sb.mrbrowser.DBHelper;
import sb.mrbrowser.R;

public class bookmark extends AppCompatActivity {

    EditText name,url;
    Button button;
    private DBHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        Bundle bundle=getIntent().getExtras();
        String saveUrl=bundle.getString("saveUrl");
        String saveTitle=bundle.getString("saveTitle");

        url=(EditText)findViewById(R.id.et1);
        name=(EditText)findViewById(R.id.et2);
        url.setText(saveUrl);
        name.setText(saveTitle);
        button=(Button)findViewById(R.id.btn);
        databaseHelper=new DBHelper(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result= databaseHelper.insertData(name.getText().toString(),url.getText().toString());
                if (result)
                    Toast.makeText(bookmark.this,"Bookmark Saved",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(bookmark.this,"Bookmark Not Saved",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
