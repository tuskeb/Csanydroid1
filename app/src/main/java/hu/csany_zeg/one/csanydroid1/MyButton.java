package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MyButton extends View {


	public MyButton(Context context) {
		super(context);
	}

	public MyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);
		int width = getWidth();
		int height = getHeight();
		canvas.drawCircle(width / 2, height / 2, height / 3, paint);

		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(width / 2, height / 2, height / 3, paint);



	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	/*@Override
	public void setOnClickListener(final OnClickListener onClickListener) {
		MyButton.this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}*/

}