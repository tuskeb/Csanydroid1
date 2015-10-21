package hu.csany_zeg.one.csanydroid1;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import hu.csany_zeg.one.csanydroid1.core.Hero;

public class HeroView extends View {
    Hero mHero;
    boolean mIsAttacker;
    private final float FRICTION = .15f;
    private final float STICKING = FRICTION * 2f;
    private static Bitmap heartBitmap = null;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        flushCache();
    }

    Bitmap mask;
    private ArrayList<Particle> mParticles = new ArrayList<Particle>();
    private Paint mPaint = new Paint();

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
                    heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart1);
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
        setMeasuredDimension(mIsAttacker ? widthMeasureSpec / 2 : widthMeasureSpec, heightMeasureSpec);
    }

    public void onLifeLost(float lostLife) {
        addParticles(getWidth() / 2, getHeight() / 2, Math.round(lostLife * 5));
    }

    // private static final float LIFE_PER_HEART = 100;
/*
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

*/

    private void drawHero(Canvas canvas) {
        for (Integer imgId : mHero.getOffensiveImageArray(0)) {
            final Bitmap bitmap = getPicture(imgId);

            int width = bitmap.getWidth(), height = bitmap.getHeight();

            canvas.drawBitmap(bitmap, (getWidth() - width) / 2, (getHeight() - height) / 2, null);
        }

    }

    private void drawAsdf(Canvas canvas) {

        Bitmap bitmap;

        float y = getHeight() - 100;
        float x = getWidth() * .1f;

        mPaint.setTextSize(23f);
        mPaint.setTextAlign(Paint.Align.LEFT);


        bitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), mHero.getCharmImageID()));
        canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, mPaint);
        canvas.drawText(String.format("%.2f", mHero.getDrunkCharm()), x + bitmap.getWidth() / 2 + 10, y, mPaint);
        y += bitmap.getHeight() + 10;

        bitmap.recycle();


        bitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), mIsAttacker ? mHero.getOffensiveImageID() : mHero.getDefensiveImageID()));
        canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, mPaint);
        canvas.drawText(String.format("%.2f", mIsAttacker ? mHero.getBaseOffensivePoint() : mHero.getBaseDefensivePoint()), x + bitmap.getWidth() / 2 + 10, y, mPaint);
        bitmap.recycle();

        x = getWidth() * .6f;
        bitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), mHero.getHealthImageID()));
        canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, mPaint);
        canvas.drawText(String.format("%.2f", mHero.getHealthPoint()), x + bitmap.getWidth() / 2 + 10, y, mPaint);
        bitmap.recycle();

    }

    private Bitmap scaleBitmap(Bitmap bitmap) {

        Bitmap dst;
        Matrix m = new Matrix();
        m.setScale(4, 4);
        (dst = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false)).setDensity(DisplayMetrics.DENSITY_DEFAULT);
        bitmap.recycle();
        return dst;
    }


    private static final HashMap<Integer, Bitmap[]> mBitmapCache = new HashMap<>();

    private static void flushCache() {
        synchronized (mBitmapCache) {
            for (Iterator<Map.Entry<Integer, Bitmap[]>> it = mBitmapCache.entrySet().iterator(); it.hasNext(); ) {
                Bitmap bitmapArray[] = it.next().getValue();
                bitmapArray[0].recycle();
                bitmapArray[1].recycle();

                it.remove();
            }

        }
    }

    Bitmap getPicture(int id) {
        synchronized (mBitmapCache) {
            if (!mBitmapCache.containsKey(id)) {
                Bitmap src = BitmapFactory.decodeResource(getResources(), id);
                Bitmap dst[] = new Bitmap[2];
                Matrix m = new Matrix();

                final float scale = (float) Math.min(((float) getWidth() * .8f) / (float) src.getWidth(), ((float) getHeight() * .8f) / (float) src.getHeight());

                m.setScale(scale, scale);
                (dst[0] = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false)).setDensity(DisplayMetrics.DENSITY_DEFAULT);

                m.setScale(-scale, scale);
                (dst[1] = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false)).setDensity(DisplayMetrics.DENSITY_DEFAULT);

                mBitmapCache.put(id, dst);


            }
            return mBitmapCache.get(id)[mIsAttacker ? 0 : 1];

        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = this.getMeasuredWidth(), h = this.getMeasuredHeight();

        if (mHero != null) {
            //mPaint.setColor(Color.RED);
            //canvas.drawRect(0, 0, w, h, mPaint);

            mPaint.setTextSize(32.0f);
            mPaint.setColor(Color.DKGRAY);
mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mHero.getName(), getWidth() / 2, 50, mPaint);

            drawHero(canvas);
            drawAsdf(canvas);

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

            HeroView.this.mPaint.setColor(Color.rgb(0xd0, 0x00, 0x00));
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
