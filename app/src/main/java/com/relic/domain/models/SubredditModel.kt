package com.relic.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
class SubredditModel : Parcelable {

    @PrimaryKey
    var id: String = ""

    @Json(name = "display_name")
    var subName: String = ""

    @Json(name = "name")
    var fullname : String = ""

    @Json(name = "banner_background_image")
    var bannerUrl: String? = null

    @Json(name = "banner_img")
    var bannerImgUrl: String? = null

    @Json(name = "over18")
    var nsfw: Boolean = false

    @Json(name = "user_is_subscriber")
    var isSubscribed: Boolean = false

    @Json(name = "user_is_banned")
    var isBanned: Boolean = false

    @Json(name = "user_is_moderator")
    var isMod: Boolean = false

    @Json(name = "show_media")
    var showMedia: Boolean = false

    @Json(name = "subscribers")
    var subscriberCount: Int = 0

    @Json(name = "community_icon")
    var subIcon: String? = null

    @Json(name = "icon_img")
    var iconUrl: String? = null

    @Json(name = "public_description_html")
    var description: String? = null
    @Json(name = "submit_text")
    var submitText: String? = null
    @Json(name = "header_title")
    var headerTitle: String? = null

    // local properties
    var pinned : Boolean = false;

    override fun toString(): String {
        return "$subName $id $bannerUrl $nsfw"
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Writes relevant object properties to the parcel
     * @param dest
     * @param flags
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(subName)
        dest.writeString(bannerUrl)
        dest.writeInt(if (nsfw) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SubredditModel> = object : Parcelable.Creator<SubredditModel> {
            override fun createFromParcel(source: Parcel): SubredditModel {
                return SubredditModel().apply {
                    this.id = source.readString() ?: ""
                    this.bannerUrl = source.readString()
                    this.subName = source.readString() ?: ""

                    this.nsfw = source.readInt() == 1
                }
            }

            override fun newArray(size: Int): Array<SubredditModel> {
                return emptyArray()
            }
        }
    }
}
