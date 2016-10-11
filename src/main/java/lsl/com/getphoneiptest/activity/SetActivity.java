package lsl.com.getphoneiptest.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lsl.com.getphoneiptest.R;
import lsl.com.getphoneiptest.tool.MySharepreferences;

/** 设置页面
 * Created by M1308_000 on 2016/10/11.
 */
public class SetActivity extends Activity implements View.OnClickListener{

    private Button but_set_cancel;
    private Button but_save;
    private EditText et_name;
    private MySharepreferences mspf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        but_save = (Button) findViewById(R.id.but_save_setting);
        but_set_cancel = (Button) findViewById(R.id.but_set_cancel);
        et_name = (EditText) findViewById(R.id.et_set_hp_name);

        but_save.setOnClickListener(this);
        but_set_cancel.setOnClickListener(this);

        mspf = new MySharepreferences(this);
        String str = mspf.ReadHpName();
        et_name.setText(str.substring(3,str.length()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_save_setting:
                String hostpot_name =  et_name.getText().toString();
                if (!TextUtils.isEmpty(hostpot_name)) {
                    mspf.Save("LSL" + hostpot_name);
                    Toast.makeText(this, "保存成功", 1).show();
                    finish();
                } else {
                    Toast.makeText(this, "热点名不能为空！", 1).show();
                }
                break;

            case R.id.but_set_cancel:
                finish();
                break;
        }
    }
}
