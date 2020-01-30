package com.denisov.taxi.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denisov.taxi.data.TaxiRepository
import com.denisov.taxi.dto.Car
import com.denisov.taxi.dto.Point
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class TaxiViewModel(
    private val taxiRepository: TaxiRepository,
    private val backgroundScheduler: Scheduler
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val carLiveData = MutableLiveData<Car>()
    val enabledTouches = MutableLiveData<Boolean>().apply { value = false }
    val startDriving = MutableLiveData<List<Point>>()

    init {
        subscribe {
            taxiRepository
                .getCar()
                .subscribeOn(backgroundScheduler)
                .doAfterTerminate { enabledTouches.postValue(true) }
                .subscribe({
                    carLiveData.postValue(it)
                }, {})
        }
    }

    fun onMapClick(carPoint: Point, finalPoint: Point) {
        subscribe {
            taxiRepository
                .getRoute(carPoint, finalPoint)
                .subscribeOn(backgroundScheduler)
                .doOnSubscribe { enabledTouches.postValue(false) }
                .subscribe({
                    startDriving.postValue(it)
                }, {})
        }
    }

    fun onDrivingEnd() {
        enabledTouches.value = true
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    private inline fun subscribe(crossinline function: () -> Disposable) {
        disposables.add(function())
    }
}