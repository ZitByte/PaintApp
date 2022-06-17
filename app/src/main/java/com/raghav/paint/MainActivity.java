package com.raghav.paint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.slider.RangeSlider;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import petrov.kristiyan.colorpicker.ColorPicker;

public class MainActivity extends AppCompatActivity {
    //создаем объект класса DrawView
    private DrawView paint;

    //создаем обэекты Button
    private ImageButton save,color,stroke,undo;

    //создаем обэект класса RangeSlider, который будет
    // помогать в выборе толщены кисти
    private RangeSlider rangeSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //получаем ссылку на кнопки из их id
        paint=(DrawView)findViewById(R.id.draw_view);
        rangeSlider=(RangeSlider)findViewById(R.id.rangebar);
        undo=(ImageButton)findViewById(R.id.btn_undo);
        save=(ImageButton)findViewById(R.id.btn_save);
        color=(ImageButton)findViewById(R.id.btn_color);
        stroke=(ImageButton)findViewById(R.id.btn_stroke);
        
        //создаем  OnClickListener для каждой кнопки, выполнять определенные действия

        //Кнопка возврата действия, для отката действия на холсте
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paint.undo();
            }
        });

        // кнопка сохранения сохранит текущий холст, который является растровым изображением
        //в виде PNG, в хранилище
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //получение растрового изображения из класса DrawView
                Bitmap bmp=paint.save();
                //открываем OutputStream для записи в файл
                OutputStream imageOutStream = null;

                ContentValues cv=new ContentValues();
                //Название файла
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing.png");
                //тип файла
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                //директория куда будет сохраняться файл
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                }

                //получить Uri файла, который должен v=создаваться в хранилище
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                try {
                    //открываем выходной поток с указанным выше uri
                    imageOutStream = getContentResolver().openOutputStream(uri);
                    //этот метод зарписывает файл в директоию
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
                    //закрываем выходной поток после использования
                    imageOutStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //Кнопка цвета позволяет выбрать цвет для кисти
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ColorPicker colorPicker=new ColorPicker(MainActivity.this);
                colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                    @Override
                    public void setOnFastChooseColorListener(int position, int color) {
                        // получаем целочисленное значение цвета, выбранного в диалоговом окне и
                        // установить его в качестве цвета обводки
                       paint.setColor(color);

                    }

                    @Override
                    public void onCancel() {

                        colorPicker.dismissDialog();
                    }
                })
                        //установите количество цветных столбцов, которые вы хотите отобразить в диалоге.
                        .setColumns(5)
                        // устанавливаем цвет по умолчанию, выбранный в диалоге
                        .setDefaultColorButton(Color.parseColor("#000000"))
                        .show();
            }
        });
        //кнопка переключает видимость RangeBar/RangeSlider
        stroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rangeSlider.getVisibility()==View.VISIBLE)
                    rangeSlider.setVisibility(View.GONE);
                else
                    rangeSlider.setVisibility(View.VISIBLE);
            }
        });

        //устанавливаем диапазон RangeSlider
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);
        //добавляем OnChangeListener, который изменит ширину обводки
        //как только пользователь двигает ползунок
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                paint.setStrokeWidth((int) value);
            }
        });

        //передаем высоту и ширину пользовательского представления в метод инициализации объекта DrawView
        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });
    }
}