package com.example.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.jetbrains.annotations.Nullable;

import java.lang.Math;
import java.util.Collections;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CostomView extends View {
    protected Random random = new Random();
    protected Path mPath;
    protected Paint mPaint;
    protected Bitmap mBitmap;
    protected Canvas mCanvas;
    boolean touch = false, wait_new_game = false;
    protected int winner = 0, tick = 0, travs_len=0, level=1;
    float x_mouse=0, y_mouse=0;
    Set<Sity> sitys = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected HashSet<Sity> sel_sitys = new HashSet<>();
    Traveler[] travs = new Traveler[10000];
    int cRed = getResources().getColor(R.color.red);
    int cBlue = getResources().getColor(R.color.blue);
    Bitmap fon = BitmapFactory.decodeResource(getResources(), R.drawable.fon);
    Bitmap lend = BitmapFactory.decodeResource(getResources(), R.drawable.lend);
    Matrix matrix = new Matrix();

    public CostomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x_mouse = event.getX();
        y_mouse = event.getY();
        Sity new_sity = null;
        for (Sity sity: sitys)
            if (length(sity.x, sity.y, x_mouse, y_mouse) < sity.r) new_sity = sity;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touch = true;
                if (new_sity != null && new_sity.user == 1 && new_sity.value >= 1) sel_sitys.add(new_sity);
                break;
            case MotionEvent.ACTION_UP:
                touch = false;
                if (new_sity != null) {
                    sel_sitys.remove(new_sity);
                    travel(sel_sitys, new_sity);
                    for (Sity sity : sel_sitys) sity.value = 0;
                }
                sel_sitys.clear();
                break;
        }
        return true;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(fon, matrix, null);
    }

    private void travel(HashSet<Sity> from_sitys, Sity to_sity) {
        float x, y, vx1, vy1;
        float ang;
        int i1;
        for (Sity sity : from_sitys) {
            for (int i=0; i<(int)sity.value; i++) {
                x = sity.x + random.nextInt(sity.r * 2) - sity.r;
                y = sity.y + random.nextInt(sity.r * 2) - sity.r;
                vx1 = to_sity.x - x;
                vy1 = to_sity.y - y;
                ang = (float) Math.acos(vx1 / Math.sqrt(vx1 * vx1 + vy1 * vy1));
                ang = (y < to_sity.y)? (float)(-ang + 2 * Math.PI) : ang;
                for (i1=0; i1<travs_len; i1++) if (travs[i1].finished) break;
                travs[i1] = new Traveler(x, y, ang, sity.user, to_sity);
                if (i1 == travs_len) travs_len++;
            }
        }
    }

    private float length(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    void init() {
        new Thread(() -> {
            while (true) {
                while (!wait_new_game) sleep(1);
                sitys.clear();
                travs_len = winner = 0;
                wait_new_game = false;
                int width = mBitmap.getWidth(), height = mBitmap.getHeight() - 400, k = 0, first_value=0;
                while (k < 30) {
                    boolean flag = true;
                    int x=0, y=0, r=0, user=0, value=0;
                    k = 0;
                    while (flag) {
                        k = (sitys.size() > 2)? k + 1 : 0;
                        flag = false;
                        value = random.nextInt(12) + 5;
                        user = (sitys.size() == 0) ? 1 : (sitys.size() == 1) ? 2 : 0;
                        if (user == 1) first_value = value;
                        else if (user == 2) value = first_value;
                        r = value * 10;
                        x = random.nextInt(width - 2 * r) + r;
                        y = random.nextInt(height - 2 * r) + r;
                        for (Sity sity : sitys)
                            if (length(x, y, sity.x, sity.y) <= r + sity.value * 10 + 100) flag = true;
                        if (k >= 30) flag = false;
                    }
                    if (k < 30) sitys.add(new Sity(x, y, r, user, value, random.nextInt(360)));
                }

                mCanvas.drawBitmap(fon, matrix, null);
                for (int i = 0; i < 100; i++) {
                    for (Sity sity : sitys) {
                        sity.r = (int) (i * sity.value / 10);
                        float size = (float) (sity.r * 3);
                        matrix.postScale(size / lend.getWidth(), size / lend.getHeight());
                        matrix.postTranslate(sity.x - size / 2, sity.y - size / 2);
                        matrix.postRotate(sity.ang, sity.x, sity.y);
                        mCanvas.drawBitmap(lend, matrix, mPaint);
                        matrix.reset();
                    }
                    invalidate();
                }

                while (!wait_new_game && update()) sleep(10);

                if (winner != 0) {
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor((winner == 1)? Color.BLUE : Color.RED);
                    mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                    int x = mBitmap.getWidth() / 2, y = mBitmap.getHeight() / 2;
                    for (int i=0; i<100; i++) {
                        mCanvas.drawBitmap(fon, matrix, null);
                        mPaint.setTextSize((int)(mBitmap.getWidth() / 600.0 * i));
                        mCanvas.save();
                        mCanvas.rotate((float) (i * 3.6), x, y);
                        if (winner == 1) mCanvas.drawText("Победа", x, y, mPaint);
                        else mCanvas.drawText("Поражение", x, y, mPaint);
                        mCanvas.restore();
                        invalidate();
                        sleep(2);
                    }
                }

                level = (winner == 1)? level + 1 : Math.max(1, level - 1);
            }
        }).start();
    }

    public boolean update() {
        invalidate();
        int num_tr = 0;
        for (int i=0; i<travs_len; i++) {
            Traveler trav = travs[i];
            if (trav.finished) continue;
            num_tr++;
            trav.x += Math.cos(trav.ang) * 3;
            trav.y -= Math.sin(trav.ang) * 3;
            float len = length(trav.x, trav.y, trav.to_sity.x, trav.to_sity.y);
            if (len < trav.to_sity.r) {
                if (trav.to_sity.value < 1) trav.to_sity.user = trav.user;
                if (trav.to_sity.user != trav.user) trav.to_sity.value--;
                else trav.to_sity.value++;
                trav.finished = true;
            }
        }

        tick = (tick + 1) % 80;
        if (tick != 0) return winner == 0;

        for (Sity sity: sitys) {
            if (sity.user == 1) sity.value += sity.r / 100.0;
            if (sity.user == 2) sity.value += sity.r / 100.0 + level / 10.0;
            sity.value = Math.min(sity.value, 300);
        }

        if (random.nextInt(3) != 0) {
            int num = 0;
            HashSet<Sity> group = new HashSet<>();
            for (Sity sity: sitys)
                if (sity.user == 2 && sity.value >= 1 && random.nextInt(2) == 0) {
                    group.add(sity);
                    num += sity.value;
                }

            for (Sity sity: sitys)
                if (sity.user != 2 && sity.value < num) {
                    if (sity.user == 1 && random.nextInt(3) != 0) continue;
                    travel(group, sity);
                    for (Sity g_sity : group) g_sity.value = 0;
                    break;
                }
        }

        if (num_tr == 0) {
            int num_1 = 0, num_2 = 0;
            for (Sity sity: sitys)
                if (sity.user == 1) num_1++;
                else if (sity.user == 2) num_2++;
            if (num_2 == 0) winner = 1;
            else if (num_1 == 0) winner = 2;
        }
        return winner == 0;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        if (touch) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(10);
            mPaint.setColor(Color.BLACK);
            mPaint.setPathEffect(new DashPathEffect(new float[]{30, 30}, 0));
            for (Sity sity: sel_sitys) {
                mPath.moveTo(sity.x, sity.y);
                mPath.lineTo(x_mouse, y_mouse);
            }
            canvas.drawPath(mPath, mPaint);
            mPaint.setPathEffect(new DashPathEffect(new float[] {10, 0}, 0));
            mPath.reset();
        }

        if (winner == 0) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTypeface(Typeface.DEFAULT);
            for (Sity sity : sitys) {
                mPaint.setTextSize(sity.r);
                mPaint.setColor((sity.user == 2) ? cRed : (sity.user == 1) ? cBlue : Color.BLACK);
                canvas.drawText(String.valueOf((int) sity.value), sity.x, (float) (sity.y + sity.r / 4.0), mPaint);
            }
        }

        for (int i=0; i<travs_len; i++) {
            if (travs[i].finished) continue;
            mPaint.setColor((travs[i].user == 1) ? cBlue : cRed);
            canvas.drawCircle(travs[i].x, travs[i].y, 10, mPaint);
        }
    }
}
