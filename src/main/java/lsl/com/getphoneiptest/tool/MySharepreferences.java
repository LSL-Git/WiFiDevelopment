package lsl.com.getphoneiptest.tool;

import android.content.Context;
import android.content.SharedPreferences;

/**  SharedPreferences 类有关操作
 * Created by M1308_000 on 2016/10/11.
 */
public class MySharepreferences {

    private Context mcontext;

    public MySharepreferences(Context mcontext) {
        this.mcontext = mcontext;
    }

    public void Save(String hp_name) {
        SharedPreferences.Editor editor = mcontext.getSharedPreferences("hp_name.txt", 0).edit();
        editor.putString("hp_name", hp_name);
        editor.commit();
    }

    public String ReadHpName() {
        SharedPreferences query = mcontext.getSharedPreferences("hp_name.txt", 0);
        String name = query.getString("hp_name", "");
        return name;
    }
}
