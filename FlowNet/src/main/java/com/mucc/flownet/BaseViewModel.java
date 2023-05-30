package com.mucc.flownet;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class BaseViewModel extends ViewModel implements IViewModel {
    public MutableLiveData<Boolean> loadingEvent = new MutableLiveData<>();
    public MutableLiveData<ApiException> apiExceptionEvent = new MutableLiveData<>();

    @Override
    public void showLoading() {
        loadingEvent.setValue(true);
    }

    @Override
    public void closeLoading() {
        loadingEvent.setValue(false);
    }

}
