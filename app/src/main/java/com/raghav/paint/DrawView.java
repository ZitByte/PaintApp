package com.raghav.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    //класс Paint инкапсулирует информацию о цвете и стиле
    // как рисовать геометрию, текст и растровые изображения
    private Paint mPaint;
    //ArrayList для хранения всех линий, нарисованных пользователем на холсте
    private ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    //Конструктор для инициализации всех аргументов
    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        //приведенные ниже методы сглаживают рисунки пользователя
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //0xff=255 в десятичном формате
        mPaint.setAlpha(0xff);

    }

    //этот метод создает экземпляр растрового изображения и объекта
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //устанавливаем начальный цвет кисти
        currentColor = Color.GREEN;
        //устанавливаем начальный размер кисти
        strokeWidth = 20;
    }

    //устанавливаем текущий цвет обводки
    public void setColor(int color) {
        currentColor = color;
    }

    //устанавливаем толщину кисти
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void undo() {
        //проверяем, пустой список или нет
        //если пусто, метод удаления вернет ошибку
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    //эти методы возвращают текущее растровое изображение
    public Bitmap save() {
        return mBitmap;
    }

    //это основной метод, в котором происходит рисование
    @Override
    protected void onDraw(Canvas canvas) {
        //сохраняем текущее состояние холста до,
        //рисуем фон холста
        canvas.save();
        //Цвет холста ПО УМОЛЧАНИЮ
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);

        //теперь мы перебираем список путей и рисуем каждый путь на холсте
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);

            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    //приведенные ниже методы управляют сенсорным откликом пользователя на экране

    //сначала создаем новый Stroke и добавляем его в список путей
    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        paths.add(fp);

        //окончательно удалить любую кривую или линию с пути
        mPath.reset();
        //этот метод устанавливает начальную точку рисуемой линии
        mPath.moveTo(x, y);
        //сохраняем текущие координаты пальца
        mX = x;
        mY = y;
    }

    //в этом методе мы проверяем, было ли движение пальца на
    // экран больше, чем допуск, который мы определили ранее,
    // затем мы вызываем метод quadTo(), который фактически сглаживает созданные нами повороты,
    //путем вычисления средней позиции между предыдущей позицией и текущей позицией
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // в конце мы вызываем метод lineTo, который просто рисует линию до тех пор, пока
    //конечная позиция
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    //метод onTouchEvent() предоставляет нам информацию о типе движения
    //что и произошло, и в соответствии с этим вызываем нужные нам методы
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}