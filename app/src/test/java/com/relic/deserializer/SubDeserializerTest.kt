package com.relic.deserializer

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.relic.data.deserializer.Contract
import com.relic.data.deserializer.Deserializer
import com.relic.data.deserializer.SubDeserializerImpl
import com.relic.deserializer.response.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*

@ExperimentalCoroutinesApi
class SubDeserializerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = TestCoroutineDispatcher()

    private val moshi: Moshi = Deserializer.getInstance()
    private lateinit var subDeserializer: Contract.SubDeserializer

    @Before
    fun setup() {
        subDeserializer = SubDeserializerImpl(moshi = moshi)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.cleanupTestCoroutines()
    }

    @Test
    fun `parse single sub`() = runBlockingTest {
        val sub = subDeserializer.parseSubredditResponse(SUB_RESPONSE)
        // basic fields -> won't test all of them because that would be too exhaustive
        sub.apply {
            Assert.assertEquals("BoiledBuns", subName)
            Assert.assertEquals("t5_12jodh", fullname)
            Assert.assertEquals("test description", description)

            Assert.assertEquals(false, nsfw)
            Assert.assertEquals("", subIcon)
        }
    }

    @Test
    fun `parse multiple subreddits`() = runBlockingTest {
        val subListing = subDeserializer.parseSubredditsResponse(SUBS_RESPONSE)
        Assert.assertEquals("Listing", subListing.kind)
        Assert.assertEquals("t5_2qhae", subListing.data.after)
        Assert.assertEquals(1, subListing.data.children!!.size)
    }

    @Test
    fun `parse subreddit search`() = runBlockingTest {
        val subPreviews = subDeserializer.parseSearchSubsResponse(SUB_SEARCH_RESPONSE)
        Assert.assertEquals(1, subPreviews.size)

        subPreviews[0].let { preview ->
            Assert.assertEquals("hearthstone", preview.name)
            Assert.assertEquals(1044162, preview.subs)
            Assert.assertEquals(4519, preview.activeUsers)
            Assert.assertEquals("#7e53c1", preview.keyColor)
            Assert.assertEquals("https://b.thumbs.redditmedia.com/kOJ2mLk2e2e2kOto6K188zsLQzJE6Yv3AMi3Pv6kwkM.png", preview.icon)
            Assert.assertEquals(true, preview.imagesAllowed)
        }
    }
}