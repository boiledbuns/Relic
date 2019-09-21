package com.relic.presentation.search.user

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.UserModel
import com.relic.presentation.search.user.view.UserSearchItemView

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserVH>() {
    private var userResults : List<UserModel> = emptyList()

    override fun getItemCount(): Int {
        return userResults.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val userSearchItemView = UserSearchItemView(parent.context)
        return UserVH(userSearchItemView)
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        holder.bind(userResults[position])
    }

    fun updateResults(users : List<UserModel>) {
        userResults = users
        notifyDataSetChanged()
    }

    inner class UserVH(
        private val view : UserSearchItemView
    ) : RecyclerView.ViewHolder(view) {

        fun bind(user: UserModel) {
            view.bind(user)
        }
    }
}