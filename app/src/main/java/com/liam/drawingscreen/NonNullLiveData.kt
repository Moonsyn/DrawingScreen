package com.liam.drawingscreen

import androidx.lifecycle.MutableLiveData

class NonNullLiveData<T: Any>(defaultValue: T) : MutableLiveData<T>(defaultValue) {

	init {
		value = defaultValue
	}

	override fun getValue() = super.getValue()!!
}