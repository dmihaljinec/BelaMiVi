package bela.mi.vi.android.ui

import androidx.lifecycle.MutableLiveData

class EmptyListViewModel {
    val visibility: MutableLiveData<Boolean> = MutableLiveData(false)
    val text: MutableLiveData<Int> = MutableLiveData(0)
    val icon: MutableLiveData<Int> = MutableLiveData(0)
}
