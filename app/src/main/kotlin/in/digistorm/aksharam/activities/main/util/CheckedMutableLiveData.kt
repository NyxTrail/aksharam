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

    override fun setValue(value: T) {
        if(this.value != value)
            super.setValue(value)
    }

    fun trigger() {
        super.setValue(value)
    }
}