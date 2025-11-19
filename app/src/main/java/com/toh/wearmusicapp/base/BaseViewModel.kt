package com.toh.wearmusicapp.base

import androidx.lifecycle.ViewModel
import com.toh.wearmusicapp.BaseWearApplication
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers

abstract class BaseViewModel() : ViewModel() {
    val coroutineExHandler = BaseWearApplication.Companion.coroutineExceptionHandler()
    val IODispatchers = Dispatchers.IO + coroutineExHandler
    val MainDispatchers = Dispatchers.Main + coroutineExHandler

    val mCompositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

}