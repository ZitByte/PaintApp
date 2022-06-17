package com.raghav.paint;

import android.graphics.Path;

public class Stroke {

    //Цвет кисти
    public int color;
    //Толщина кисти
    public int strokeWidth;
    //объект Path для представления нарисованного пути
    public Path path;

    //конструктор для инициализации атрибутов
    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}