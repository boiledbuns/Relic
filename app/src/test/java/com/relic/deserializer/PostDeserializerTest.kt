package com.relic.deserializer

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.Deserializer
import com.relic.data.deserializer.PostDeserializerImpl
import com.relic.deserializer.response.POSTS_RESPONSE
import com.relic.deserializer.response.POST_RESPONSE
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
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
        postDeserializer = PostDeserializerImpl(moshi = moshi)
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