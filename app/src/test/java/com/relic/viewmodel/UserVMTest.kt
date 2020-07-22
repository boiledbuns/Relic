package com.relic.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
import com.relic.api.response.Data
import com.relic.api.response.Listing
import com.relic.data.PostRepository
import com.relic.data.UserRepository
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.ListingItem
import com.relic.domain.models.UserModel
import com.relic.presentation.displayuser.DisplayUserVM
import com.relic.presentation.displayuser.ErrorData
import com.relic.presentation.displayuser.UserTab
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserVMTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = TestCoroutineDispatcher()

    private lateinit var postRepo : PostRepository
    private lateinit var userRepo : UserRepository
    private lateinit var postGateway : PostGateway

    private val username = "testUsername"

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)

        postRepo = mock()
        userRepo = mock()
        postGateway = mock()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.cleanupTestCoroutines()
    }

    @Test
    fun `user retrieved on init`() = runBlockingTest {
        val mockUser = mockUser()
        whenever(userRepo.retrieveUser(username)).doReturn(mockUser)

        val vm = DisplayUserVM(postRepo, userRepo, postGateway, username)

        val observer : Observer<UserModel> = mock()
        vm.userLiveData.observeForever(observer)

        verify(userRepo, times(1)).retrieveUser(username)
        verify(observer).onChanged(mockUser)
    }

    @Test
    fun `livedata updated when posts retrieved` () = runBlockingTest {
        val mockUser = mockUser()
        whenever(userRepo.retrieveUser(username)).doReturn(mockUser)

        val mockListingItems = listOf<ListingItem>(mock())
        val listing = mockListing(mockListingItems)
        whenever(postRepo.retrieveUserListing(any(), any(), any())).doReturn(listing)

        val tab = UserTab.Submitted
        val vm = DisplayUserVM(postRepo, userRepo, postGateway, username)

        val observer : Observer<List<ListingItem>> = mock()
        vm.getTabPostsLiveData(tab).observeForever(observer)

        verify(postRepo, times(1)).retrieveUserListing(any(), any(), any())
        verify(observer, times(1)).onChanged(mockListingItems)
    }

    @Test
    fun `error livedata updated when no posts retrieved` () = runBlockingTest {
        val mockUser = mockUser()
        whenever(userRepo.retrieveUser(username)).doReturn(mockUser)

        val listing = mockListing()
        val localPostRepo = postRepo
        whenever(localPostRepo.retrieveUserListing(any(), any(), any())).doReturn(listing)

        val tab = UserTab.Submitted
        val vm = DisplayUserVM(postRepo, userRepo, postGateway, username)

        val listingObserver : Observer<List<ListingItem>> = mock()
        vm.getTabPostsLiveData(tab).observeForever(listingObserver)

        val errorObserver : Observer<ErrorData> = mock()
        vm.errorLiveData.observeForever(errorObserver)

        verify(postRepo, times(1)).retrieveUserListing(any(), any(), any())
        // listing livedata should be updated with an "error" should be posted
        verify(listingObserver, times(1)).onChanged(any())
        verify(errorObserver, times(1)).onChanged(ErrorData.NoMorePosts(tab))
    }

    private fun mockListing(
        listingItems : List<ListingItem> = emptyList()
    ) : Listing<ListingItem> {

        val data = Data<ListingItem>().apply {
            children = listingItems
        }

        return Listing(kind = "", data = data)
    }

    private fun mockUser() : UserModel {
        return UserModel().apply {
            username = username
        }
    }
}