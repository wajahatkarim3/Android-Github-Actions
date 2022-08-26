package com.wajahatkarim3.imagine.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wajahatkarim3.imagine.data.DataState
import com.wajahatkarim3.imagine.data.usecases.FetchPopularPhotosUsecase
import com.wajahatkarim3.imagine.data.usecases.SearchPhotosUsecase
import com.wajahatkarim3.imagine.model.PhotoModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val fetchPopularPhotosUsecase: FetchPopularPhotosUsecase,
    private val searchPhotosUsecase: SearchPhotosUsecase
) : ViewModel() {

    private var _uiState = MutableLiveData<HomeUiState>()
    var uiStateLiveData: LiveData<HomeUiState> = _uiState

    private var _photosList = MutableLiveData<List<PhotoModel>>()
    var photosListLiveData: LiveData<List<PhotoModel>> = _photosList

    private var pageNumber = 1
    private var searchQuery: String = ""

    init {
        fetchPhotos(pageNumber)
    }

    fun loadMorePhotos() {
        pageNumber++
        if (searchQuery == "")
            fetchPhotos(pageNumber)
        else
            searchPhotos(searchQuery, pageNumber)
    }

    fun retry() {
        if (searchQuery == "")
            fetchPhotos(pageNumber)
        else
            searchPhotos(searchQuery, pageNumber)
    }

    fun searchPhotos(query: String) {
        searchQuery = query
        pageNumber = 1
        searchPhotos(query, pageNumber)
    }

    fun fetchPhotos(page: Int) {
        _uiState.postValue(if (page == 1) LoadingState else LoadingNextPageState)
        viewModelScope.launch {
            fetchPopularPhotosUsecase(page).collect {
                when (it) {
                    is DataState.Success -> {
                        if (page == 1) {
                            // First page
                            _uiState.postValue(ContentState)
                            _photosList.postValue(it.data!!)
                        } else {
                            // Any other page
                            _uiState.postValue(ContentNextPageState)
                            val currentList = arrayListOf<PhotoModel>()
                            _photosList.value?.let { pl -> currentList.addAll(pl) }
                            currentList.addAll(it.data!!)
                            _photosList.postValue(currentList)
                        }
                    }

                    is DataState.Error -> {
                        if (page == 1) {
                            _uiState.postValue(ErrorState(it.message))
                        } else {
                            _uiState.postValue(ErrorNextPageState(it.message))
                        }
                    }
                }
            }
        }
    }

    private fun searchPhotos(query: String, page: Int) {
        _uiState.postValue(if (page == 1) LoadingState else LoadingNextPageState)
        viewModelScope.launch {
            searchPhotosUsecase(query, page).collect { dataState ->
                when (dataState) {
                    is DataState.Success -> {
                        if (page == 1) {
                            // First page
                            _uiState.postValue(ContentState)
                            _photosList.postValue(dataState.data!!)
                        } else {
                            // Any other page
                            _uiState.postValue(ContentNextPageState)
                            val currentList = arrayListOf<PhotoModel>()
                            _photosList.value?.let { currentList.addAll(it) }
                            currentList.addAll(dataState.data!!)
                            _photosList.postValue(currentList)
                        }
                    }

                    is DataState.Error -> {
                        if (page == 1) {
                            _uiState.postValue(ErrorState(dataState.message))
                        } else {
                            _uiState.postValue(ErrorNextPageState(dataState.message))
                        }
                    }
                }
            }
        }
    }
}