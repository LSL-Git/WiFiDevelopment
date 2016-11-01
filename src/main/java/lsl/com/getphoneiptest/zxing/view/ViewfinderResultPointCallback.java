package lsl.com.getphoneiptest.zxing.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

/**
 * Created by M1308_000 on 2016/11/1.
 */

public class ViewfinderResultPointCallback implements ResultPointCallback {

    private final ViewfinderView viewfinderView;

    public ViewfinderResultPointCallback( ViewfinderView viewfinderView1) {

        this.viewfinderView = viewfinderView1;
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint resultPoint) {
        viewfinderView.addPossibleResultPoint(resultPoint);
    }
}
