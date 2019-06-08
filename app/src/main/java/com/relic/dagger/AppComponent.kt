package com.relic.dagger

import android.app.Application
import android.content.Context
import com.relic.RelicApp
import com.relic.dagger.modules.ActivityModule
import com.relic.presentation.main.MainActivity
import com.relic.dagger.modules.AppModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.VMModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityModule::class,
        RepoModule::class,
        VMModule::class
    ]
)
interface AppComponent {
    /**
     * this is how we allow the component to build itself
     */
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app : Application) : Builder

        @BindsInstance
        fun context(appContext : Context) : Builder

//        @BindsInstance
//        fun repoModule(app : RepoModule) : Builder
//
//        @BindsInstance
//        fun appModule(app : AppModule) : Builder


        fun build() : AppComponent
    }

    fun inject(app : RelicApp)

//    fun inject(mainActivity: MainActivity)
}
