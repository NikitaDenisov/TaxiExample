package com.denisov.taxi.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.denisov.taxi.presentation.ui.MapView
import com.denisov.taxi.R
import com.denisov.taxi.data.TaxiRepository
import com.denisov.taxi.dto.Point
import com.squareup.picasso.Picasso
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), MapView.MapListener {

    private lateinit var viewModel: TaxiViewModel
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map)
        mapView.mapListener = this

        createViewModel()

        viewModel.carLiveData.observe(this, Observer { car ->
            car?.let {
                viewModel.carLiveData.value = null
                mapView.setCar(it)
            }
        })
        viewModel.startDriving.observe(this, Observer { route ->
            route?.let {
                viewModel.startDriving.value = null
                mapView.startDriving(it)
            }
        })
        viewModel.enabledTouches.observe(this, Observer {
            mapView.enableTouches = it == true
        })
    }

    override fun onMapClick(carPoint: Point, finalPoint: Point) {
        viewModel.onMapClick(carPoint, finalPoint)
    }

    override fun onDrivingEnd() {
        viewModel.onDrivingEnd()
    }

    private fun createViewModel() {
        val repository = TaxiRepository(Picasso.get())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProviders.of(this, factory).get(TaxiViewModel::class.java)
    }
}

class ViewModelFactory(
    private val repository: TaxiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            TaxiRepository::class.java,
            Scheduler::class.java
        ).newInstance(repository, Schedulers.newThread())
}