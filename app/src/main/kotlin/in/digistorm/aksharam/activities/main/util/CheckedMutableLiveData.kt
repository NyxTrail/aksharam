/*
 * Copyright (c) 2023-2025 Alan M Varghese <alan@digistorm.in>
 *
 * This files is part of Aksharam, a script teaching app for Indic
 * languages.
 *
 * Aksharam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aksharam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even teh implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package `in`.digistorm.aksharam.activities.main.util

import androidx.lifecycle.MutableLiveData

/**
 * This class sets or posts a value only if the current value and the incoming value
 * are different. This helps avoid recreating calculations when the incoming value is
 * the same.
 *
 * The trigger() method allows us to circumvent this and emit the same value if
 * desired.
 *
 * This is useful when initialising a view using data binding and the backing data
 * (in a view model) was already initialised.
 */
class CheckedMutableLiveData<T>: MutableLiveData<T> {
    constructor(): super()
    constructor(value: T): super(value)

    override fun postValue(value: T) {
        if(this.value != value)
            super.postValue(value)
    }

   fun postValueWithTrigger(value: T) {
        super.postValue(value)
    }

    fun setValueIfDifferent(value: T) {
        if(this.value != value)
            super.setValue(value)
    }

    fun trigger() {
        super.setValue(value)
    }
}
