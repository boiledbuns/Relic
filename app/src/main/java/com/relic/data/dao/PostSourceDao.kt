package com.relic.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.data.entities.PostSourceEntity
import java.util.ArrayList

@Dao
abstract class PostSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPostSources(postSourceEntities: ArrayList<PostSourceEntity>)

    @Query("SELECT * FROM PostSourceEntity WHERE sourceId = :postId")
    abstract fun getPostSource(postId: String): PostSourceEntity?

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE subredditPosition >= 0 AND subreddit = :subredditName")
    abstract fun getItemsCountForSubreddit(subredditName: String): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE frontpagePosition >= 0")
    abstract fun getItemsCountForFrontpage(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE allPosition >= 0")
    abstract fun getItemsCountForAll(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userSubmissionPosition >= 0")
    abstract fun getItemsCountForUserSubmission(): Int

    // region remove source from all posts based on criteria

    @Query("UPDATE PostSourceEntity SET subredditPosition = -1 WHERE subreddit = :subName")
    abstract fun removeAllSubredditAsSource(subName: String)

    @Query("UPDATE PostSourceEntity SET frontpagePosition = -1 WHERE frontpagePosition >= 0")
    abstract fun removeAllFrontpageAsSource()

    @Query("UPDATE PostSourceEntity SET allPosition = -1 WHERE allPosition >= 0")
    abstract fun removeAllAllAsSource()

    @Query("UPDATE PostSourceEntity SET userSubmissionPosition = -1 WHERE userSubmissionPosition >= 0")
    abstract fun removeAllCurrentUserAsSource()

    // endregion

    // region remove source from single post based on criteria
    @Query("UPDATE PostSourceEntity SET subredditPosition = -1 WHERE sourceId = :postId")
    abstract fun removeSubredditAsSource(postId: String)

    @Query("UPDATE PostSourceEntity SET frontpagePosition = -1 WHERE sourceId = :postId")
    abstract fun removeFrontpageAsSource(postId: String)

    @Query("UPDATE PostSourceEntity SET allPosition = -1 WHERE sourceId = :postId")
    abstract fun removeAllAsSource(postId: String)
    // endregion

    @Query("DELETE FROM PostSourceEntity WHERE subredditPosition < 0 AND frontpagePosition < 0 AND allPosition < 0")
    abstract fun removeAllUnusedSources()
}