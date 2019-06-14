package com.relic.viewmodel

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.relic.data.PostRepository
import com.relic.data.UserRepository
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.UserModel
import com.relic.presentation.displayuser.DisplayUserVM
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

class UserVMTest {
    private val mainThreadSurrogate = newSingleThreadContext("Test thread")

    lateinit var postRepo : PostRepository
    lateinit var userRepo : UserRepository
    lateinit var postGateway : PostGateway
    val username = "boiledbuns"

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Before
    fun initTests(){
        Dispatchers.setMain(mainThreadSurrogate)

        postRepo = mock()
        userRepo = mock()
        postGateway = mock()
    }

    @Test fun `user retrieved on init`() = runBlocking {
        val mockUser = mock(UserModel::class.java)
        whenever(userRepo.retrieveUser(username)).doReturn(mockUser)

        val vm = mockVM()

        val observer : Observer<UserModel> = mock()
        vm.userLiveData.observeForever(observer)

        verify(userRepo, times(1)).retrieveUser(username)
        verify(observer).onChanged(mockUser)
    }


    private fun mockVM() : DisplayUserVM {
        return DisplayUserVM(postRepo, userRepo, postGateway, username)
    }
}