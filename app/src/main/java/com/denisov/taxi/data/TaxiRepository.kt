package com.denisov.taxi.data

import com.denisov.taxi.dto.Car
import com.denisov.taxi.dto.Point
import com.squareup.picasso.Picasso
import io.reactivex.Single
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class TaxiRepository(private val picasso: Picasso) {

    fun getCar(): Single<Car> = Single.fromCallable {
        picasso
            .load(CAR_FILE_PATH)
            .get()
            .let { Car(it) }
    }

    fun getRoute(start: Point, end: Point): Single<List<Point>> = Single.fromCallable {
        val minX = min(start.x, end.x)
        val maxX = max(start.x, end.x)

        val minY = min(start.y, end.y)
        val maxY = max(start.y, end.y)

        val xPoints = (1..3).map { Random.nextInt(minX, maxX) }
        val yPoints = (1..3).map { Random.nextInt(minY, maxY) }

        mutableListOf<Point>().apply {
            for (i in 0 until 3) {
                add(Point(xPoints[i], yPoints[i]))
            }
            add(end)
        }
    }

    private companion object {
        private const val CAR_FILE_PATH = "file:///android_asset/car.png"
        private const val POINTS_COUNT_IN_ROUTE = 3
    }
}