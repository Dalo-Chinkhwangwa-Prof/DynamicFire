package com.dynamicdevz.dynamicfirebase

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dynamicdevz.dynamicfirebase.model.AddressResponse
import com.dynamicdevz.dynamicfirebase.network.RetrofitInstance
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel: ViewModel() {

    private val retrofit = RetrofitInstance()

    private val mutableLocData = MutableLiveData<AddressResponse>()

    val locData: LiveData<AddressResponse>
    get() = mutableLocData

    private val compositeDisposable = CompositeDisposable()

    fun getLocationAddress(location: Location){
        val latLng = "${location.latitude},${location.longitude}"
        compositeDisposable.add(
            retrofit.getLocationAddress(latLng).observeOn(
                Schedulers.io()
            ).subscribeOn(Schedulers.io())
                .subscribe({
                    mutableLocData.postValue(it)
                    clearDisposable()

                }, {
                    Log.d("TAG_X", it.localizedMessage)
                }
                )
        )
    }

    private fun clearDisposable() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        clearDisposable()
        super.onCleared()
    }
}








