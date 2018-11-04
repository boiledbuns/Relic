package com.relic.presentation.subinfodialog

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.models.SubredditModel
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subinfo_sheetdialog.*

class SubInfoBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel : SubInfoDialogVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                // inject dependencies into factory and construct viewmodel
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .build()
                        .getDisplaySubInfoVM().create(subName) as T
            }
        }).get(SubInfoDialogVM::class.java)
    }

    private lateinit var subName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_SUB_NAME)?.apply {
            subName = this
            bindVm()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_subinfo_sheetdialog, container,  false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subNameView.text = subName

        // initialize onclicks
        subscribeButtonView.setOnClickListener { }
        pinButtonView.setOnClickListener { viewModel.pinSubreddit(true) }
    }

    private fun bindVm() {
        viewModel.subredditLiveData.nonNull().observe(this) { setSubredditData(it) }
    }

    private fun setSubredditData(subredditModel: SubredditModel) {
        if (subredditModel.isSubscribed) {
            subscribeButtonView.text = getString(R.string.subscribed)
            subscribeButtonView.background?.setTint(resources.getColor(R.color.positive))
        } else {
            subscribeButtonView.text = getString(R.string.subscribe)
            subscribeButtonView.background?.setTint(resources.getColor(R.color.negative))
        }

        subCountView.text = resources.getString(R.string.subscriber_count, subredditModel.subscriberCount)
        subDescriptionView.text = subredditModel.description
        // TODO create custom movement method class
        subDescriptionView.movementMethod
    }

}