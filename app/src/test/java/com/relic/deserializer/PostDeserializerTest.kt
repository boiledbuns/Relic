package com.relic.deserializer

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SubRepository
import com.relic.data.UserRepository
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.Deserializer
import com.relic.data.deserializer.PostDeserializerImpl
import com.relic.data.gateway.PostGateway
import com.relic.data.gateway.SubGateway
import com.relic.network.NetworkUtil
import com.relic.presentation.displaysub.DisplaySubVM
import com.squareup.moshi.Moshi
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.*

/**
 * just ensure tests don't throw exceptions
 */
@ExperimentalCoroutinesApi
class PostDeserializerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = TestCoroutineDispatcher()

    private val moshi : Moshi = Deserializer.getInstance()
    private lateinit var postDeserializer: Contract.PostDeserializer

    @Before
    fun setup() {
        postDeserializer = PostDeserializerImpl(appDB = mock(), moshi = moshi)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.cleanupTestCoroutines()
    }

    @Test
    fun `parse single post`() = runBlockingTest {
        val post = postDeserializer.parsePost(POST_RESPONSE)
    }

    @Test
    fun `parse posts`() = runBlockingTest {
        val post = postDeserializer.parsePosts(POSTS_RESPONSE)
    }
}