package com.relic.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.relic.api.response.Data
import com.relic.api.response.Listing
import com.relic.data.ListingRepository
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SubRepository
import com.relic.data.UserRepository
import com.relic.data.gateway.SubGateway
import com.relic.domain.models.PostModel
import com.relic.interactor.Contract
import com.relic.network.NetworkUtil
import com.relic.presentation.displaysub.DisplaySubVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DisplaySubVMTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = TestCoroutineDispatcher()

    private lateinit var subRepo: SubRepository
    private lateinit var postRepo: PostRepository
    private lateinit var userRepo: UserRepository
    private lateinit var subGateway: SubGateway
    private lateinit var postInteractor: Contract.PostAdapterDelegate
    private lateinit var listingRepo: ListingRepository

    private val username = "testUsername"

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)

        subRepo = mock()
        postRepo = mock()
        userRepo = mock()
        subGateway = mock()
        postInteractor = mock()
        listingRepo = mock()

        whenever(subRepo.getSubGateway()).doReturn(subGateway)
        whenever(subRepo.getSingleSub(any())).doReturn(mock())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.cleanupTestCoroutines()
    }

    @Test
    fun `posts retrieved from network when connection available`() = runBlockingTest {
        val mockNetworkUtil = mock<NetworkUtil>()
        whenever(mockNetworkUtil.checkConnection()).doReturn(true)

        whenever(postRepo.retrieveSortedPosts(any(), any(), any())).doReturn(mockListing())

        val source = PostSource.Subreddit("test_sub")
        val vm = DisplaySubVM(source, subRepo, postRepo, postInteractor, listingRepo, mockNetworkUtil)

        verify(postRepo, times(0)).getPosts(any())
        verify(postRepo, times(1)).retrieveSortedPosts(any(), any(), any())
    }

    @Test
    fun `local posts loaded when no connection`() = runBlockingTest {
        val mockNetworkUtil = mock<NetworkUtil>()
        whenever(mockNetworkUtil.checkConnection()).doReturn(false)

        whenever(postRepo.retrieveSortedPosts(any(), any(), any())).doReturn(mockListing())

        val source = PostSource.Subreddit("test_sub")
        val vm = DisplaySubVM(source, subRepo, postRepo, postInteractor, listingRepo, mockNetworkUtil)

        verify(postRepo, times(1)).getPosts(any())
        verify(postRepo, times(0)).retrieveSortedPosts(any(), any(), any())
    }

    private fun mockListing() : Listing<PostModel>{
        val data : Data<PostModel> = mock()
        return Listing("test", data)
    }
}