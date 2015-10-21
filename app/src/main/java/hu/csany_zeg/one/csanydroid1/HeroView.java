package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import hu.csany_zeg.one.csanydroid1.core.Hero;

public class HeroView extends View {
    Hero mHero;
    boolean mIsAttacker;
    private final float FRICTION = .15f;
    private final float STICKING = FRICTION * 2f;
    private static Bitmap heartBitmap = null;
    /*
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            for(Field field : cls.getDeclaredFields()){
                Class type = field.getType();
                String name = field.getName();
                Annotation[] annotations = field.getDeclaredAnnotations();
            }

            Log.v("mama", w + "-" + oldw + "|" + h + "-" + oldh);
        }
    */
    Bitmap mask;
    private ArrayList<Particle> mParticles = new ArrayList<Particle>();
    private Paint mPaint = new Paint();
    private float initialY;
    private float diffY;
    private long beginTime;
    private float speedY;
    private float y = 40;
    private boolean mIsTouched = false;

    final DataSetObserver dso = new DataSetObserver() {
        @Override
        public void onChanged() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    };

    public void setHero(final Hero hero) {

        if (mHero != null) {
            HeroView.this.mHero.unregisterObserver(dso);
        }
        mHero = hero;

        if (mHero != null) {
            mIsAttacker = hero.getBattle().getAttacker() == mHero;

            HeroView.this.mHero.registerObserver(dso);
        }
        invalidate();
    }

    public HeroView(final Context context, boolean leftSide) {
        super(context);
        mIsAttacker = leftSide;

        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (heartBitmap == null)
                    heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heartasd);
                mask = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //heartBitmap.recycle();
                mask.recycle();
                setHero(null);

            }
        });


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
/*
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int desiredWidth = widthSize / 2;
		int desiredHeight = heightSize;

		int width;
		int height;

		switch (widthMode) {
			case MeasureSpec.EXACTLY:
				width = widthSize;
				break;
			case MeasureSpec.AT_MOST:
				width = Math.min(desiredWidth, widthSize);
				break;
			default:
				width = desiredWidth;
		}

		switch (heightMode) {
			case MeasureSpec.EXACTLY:
				height = heightSize;
				break;
			case MeasureSpec.AT_MOST:
				height = Math.min(desiredHeight, heightSize);
				break;
			default:
				height = desiredHeight;

		}*/

        setMeasuredDimension(mIsAttacker ? widthMeasureSpec / 2 : widthMeasureSpec, heightMeasureSpec);

    }

    public void onLifeLost(float lostLife) {
        addParticles(getHealthBarWidth(), 26, 10);
    }

/*
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		this.w = w - bitmap.getWidth();
		this.h = h - bitmap.getHeight();

	}*/

    private static final float LIFE_PER_HEART = 100;

    private float getHealthBarWidth() {

        float life = mHero.getHealthPoint();
        return (32f) * (life / LIFE_PER_HEART);
    }

    private void drawHealth(Canvas canvas) {

        float life = mHero.getHealthPoint();
        byte i = 0;

        for (; life > LIFE_PER_HEART; life -= LIFE_PER_HEART, ++i) {
            canvas.drawBitmap(heartBitmap, 10 + i * (32 + 5), 10, mPaint);
        }

        {
            Canvas maskCanvas = new Canvas(mask);
            maskCanvas.drawRect(0, 0, (float) Math.floor(heartBitmap.getWidth() * life / LIFE_PER_HEART), 32, new Paint());

            // you can change original image here and draw anything you want to be masked on it.

            Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            tempCanvas.drawBitmap(heartBitmap, 0, 0, null);
            tempCanvas.drawBitmap(mask, 0, 0, paint);
            paint.setXfermode(null);

            // draw result after performing masking
            canvas.drawBitmap(result, 10 + i * (32 + 5), 10, mPaint);

        }

    }


    private void drawTest(Canvas canvas) {

        ArrayList<Integer> a = mHero.getOffensiveImageArray(0);
        ArrayList<Integer> b = mHero.getOffensiveImageArray(1);
        ArrayList<Integer> c = mHero.getOffensiveImageArray(2);
        for (Integer i: a) {
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),i),0,100,null);
        }
        for (Integer i: b) {
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),i),0,200,null);
        }
        for (Integer i: c) {
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),i),0,300,null);
        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = this.getMeasuredWidth(), h = this.getMeasuredHeight();

        if (mHero != null) {
            //mPaint.setColor(Color.RED);
            //canvas.drawRect(0, 0, w, h, mPaint);

            mPaint.setTextSize(30.0f);
            mPaint.setColor(Color.DKGRAY);
            canvas.drawText(mHero.getName(), 50, y, mPaint);

            drawHealth(canvas);

            //canvas.drawArc(200, 200, 250, 300,0, (float)Math.PI, true, mPaint);

            mPaint.setTextSize(20.0f);
            canvas.drawText(mHero.getHealthPoint() + "", 50, y + 20, mPaint);
            canvas.drawText((Math.round(mHero.getCharm() * 10f) / 10f) + "", 50, y + 50, mPaint);
            canvas.drawText((Math.round(mHero.getBaseOffensivePoint() * 10f) / 10f) + "", 50, y + 80, mPaint);
/*
		if (speedY > 0) {
			y += speedY;
			if ((speedY -= (FRICTION + (mIsTouched ? STICKING : 0))) < 0) speedY = 0;
			invalidate();
		} else if (speedY < 0) {
			y += speedY;
			if ((speedY += (FRICTION + (mIsTouched ? STICKING : 0))) > 0) speedY = 0;
			invalidate();
		} else
		*/


        }

        if (mParticles.size() > 0) {
            invalidate();
        }

        for (int i = mParticles.size(); i > 0; ) {
            Particle p = mParticles.get(--i);
            p.draw(canvas);
        }

        y %= h;


        drawTest(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                invalidate();
				/*
				mIsTouched = true;
				if (speedY == 0) {
					addParticles(event.getX(), event.getY(), 16 + (int)(Math.random() * 8));

					initialY = event.getY();
					diffY = initialY - y;
					beginTime = System.nanoTime();
				}
				*/
                break;
            case MotionEvent.ACTION_UP:
				/*
				mIsTouched = false;
				if (speedY == 0) {
					float elapsedSeconds = (System.nanoTime() - beginTime) / 1e9f;
					if (elapsedSeconds < .3f) {
						speedY = (event.getY() - initialY) / elapsedSeconds / 60f;
					}
				}
				*/

                break;
            case MotionEvent.ACTION_MOVE:
				/*
				// TODO csillapítás után újra mozog
				if (speedY == 0) {
					y = event.getY() - diffY;
					invalidate();

				}
				*/

        }

        //Animation animation1 = AnimationUtils.loadAnimation(this.getContext(), R.anim.barack);
        //this.startAnimation(animation1);

        //this.animate().translationY(1000).setDuration(400).setInterpolator(new FastOutLinearInInterpolator());

        return true;//super.onTouchEvent(event);
    }

    public void addParticles(float x, float y, int count) {
        while (--count >= 0) new Particle(x, y);
        invalidate();
    }

    private class Particle {
        private final float GRAVITY_Y = .55f;
        private final float AIR_RESISTANCE_X = .08f;
        private final float MIN_FORCE = 3.5f, MAX_FORCE = 8f;
        private final double ANGLE = 75 * (Math.PI / 180); // [rad] TODO find a name

        public float mX, mY;
        private double mVelocityX, mVelocityY;

        public Particle(float x, float y) {
            mX = x;
            mY = y;

            final double angle = (-Math.PI / 2) + (Math.random() * ANGLE - ANGLE / 2);
            mVelocityX = Math.cos(angle) * (MIN_FORCE + Math.random() * (MAX_FORCE - MIN_FORCE));
            mVelocityY = Math.sin(angle) * (MIN_FORCE + Math.random() * (MAX_FORCE - MIN_FORCE));

            HeroView.this.mParticles.add(this);

        }

        public void draw(Canvas canvas) {

            mY += mVelocityY;

            if (mY > canvas.getHeight()) {
                HeroView.this.mParticles.remove(this);
                return;
            }

            mX += mVelocityX;

            canvas.drawCircle(mX + 5f, mY + 5f, 10f, HeroView.this.mPaint);

            mVelocityY += GRAVITY_Y;
            if (mVelocityX > 0) {
                if ((mVelocityX -= AIR_RESISTANCE_X) < 0) mVelocityX = 0;
            } else if (mVelocityX < 0) {
                if ((mVelocityX += AIR_RESISTANCE_X) > 0) mVelocityX = 0;
            }


        }

    }

}
