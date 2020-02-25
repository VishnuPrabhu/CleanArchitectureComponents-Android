package com.sample.gitrepos.repositories


import androidx.lifecycle.MutableLiveData
import com.sample.gitrepos.models.GitReposModel
import com.sample.gitrepos.models.Timestamp
import com.sample.gitrepos.network.FetchRepoAPIService
import com.sample.gitrepos.network.Resource
import com.sample.gitrepos.persistence.DaoHandlerImpl
import com.sample.gitrepos.utility.TWO_HOURS_MILLIS

class ReposListRepositoryImpl(private val fetchRepoWeatherWebservice: FetchRepoAPIService, private val daoHandlerImpl: DaoHandlerImpl) : BaseRepository(),
    ReposListRepository {

    private val fetchCompleteLiveData by lazy { MutableLiveData<Resource<MutableList<GitReposModel>>>() }

    override fun getReposResponseLiveData(): MutableLiveData<Resource<MutableList<GitReposModel>>> {
        return fetchCompleteLiveData
    }

    override suspend fun getGitRepositories() {
          if (System.currentTimeMillis() - getLastSavedTimeStamp() >= TWO_HOURS_MILLIS || daoHandlerImpl.getReposDataFromDB().isNullOrEmpty()) {
            val resource = safeApiCall(call = { fetchRepoWeatherWebservice.fetchRepositoriesFromURL().await() })
              updateDatabase(resource)
              fetchCompleteLiveData.value = resource
        } else {
              fetchCompleteLiveData.value = Resource.success(daoHandlerImpl.getReposDataFromDB())
        }
    }

    private suspend fun updateDatabase(resource: Resource<MutableList<GitReposModel>>) {
        resource.data?.let {
            daoHandlerImpl.addReposDataIntoDB(it)
            daoHandlerImpl.addTimeStampInDB(Timestamp(System.currentTimeMillis()))
        }
    }

    private suspend fun getLastSavedTimeStamp(): Long {
        return daoHandlerImpl.getTimeStampFromDB().timesTamp
    }

}