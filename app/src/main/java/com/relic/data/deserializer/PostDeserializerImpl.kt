package com.relic.data.deserializer

import com.relic.api.response.Listing
import com.relic.persistence.ApplicationDB
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.simple.parser.ParseException
import javax.inject.Inject

class PostDeserializerImpl @Inject constructor(
    private val appDB : ApplicationDB,
    private val moshi : Moshi
) : Contract.PostDeserializer {
    private val listingListingItemType = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val listingListingItemAdapter = moshi.adapter<Listing<ListingItem>>(listingListingItemType)

    private val postType = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val postListingAdapter = moshi.adapter<Listing<PostModel>>(postType)

    private val listingType = Types.newParameterizedType(List::class.java, postType)
    private val postListListingAdapter = moshi.adapter<List<Listing<PostModel>>>(listingType)

    override suspend fun parseListingItems(response: String): Listing<ListingItem> {
        try {
            // api returns a listing with a single post item as its child
            return listingListingItemAdapter.fromJson(response)!!
        } catch (e : ParseException){
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parsePost(response: String) : PostModel {
        try {
            // api returns a listing with a single post item as its child
            return postListListingAdapter.fromJson(response)!![0].data.children!!.first()
        } catch (e : ParseException){
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parsePosts(response: String) : Listing<PostModel> {
        try {
            return postListingAdapter.fromJson(response)!!
        } catch (e : Exception) {
            throw RelicParseException(response, e)
        }
    }
}