package com.decagon.multimodularapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.decagon.multimodularapp.databinding.ActivityMainBinding
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

class MainActivity : AppCompatActivity() {

    private lateinit var splitInstallManager: SplitInstallManager // responsible for downloading the module
    lateinit var request: SplitInstallRequest // contains the request information that will be used to request the module from play store
    private val NEWS_FEATURE = "news_feature"
    private lateinit var binding: ActivityMainBinding
    var mySessionId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView (binding.root)

        initDynamicModules()
        setClickListeners()  { listener }

    }

    private fun initDynamicModules() {
        splitInstallManager = SplitInstallManagerFactory.create(this) // factory for installing modules
        request = SplitInstallRequest    // nodule request builder
            .newBuilder()
            .addModule(NEWS_FEATURE)
            .build()
    }

    private fun setClickListeners(action : () -> Unit){
        binding.buttonClick.setOnClickListener {
            if(!isDynamicFeatureDownloaded(NEWS_FEATURE)){
                downloadFeature()
            } else {
                binding.buttonDeleteNewsModule.visibility = View.VISIBLE
                binding.buttonOpenNewsModule.visibility = View.VISIBLE
            }
        }

        binding.buttonOpenNewsModule.setOnClickListener {
            val intent = Intent().setClassName(this, "com.decagon.dynamicfeature.NewsActivity" )
            startActivity(intent)
        }

        binding.buttonDeleteNewsModule.setOnClickListener {
            val list = ArrayList<String>()
            list.add(NEWS_FEATURE)
            uninstallDynamicFeature(list)
        }

        action.invoke()
    }

    private fun isDynamicFeatureDownloaded(feature: String): Boolean =
        when (feature){
            NEWS_FEATURE ->  splitInstallManager.installedModules.contains(feature)

            else -> splitInstallManager.installedModules.contains(feature)
        }

    private fun downloadFeature(){
        splitInstallManager.startInstall(request)
            .addOnFailureListener(){}
            .addOnSuccessListener {
                binding.buttonOpenNewsModule.visibility = View.VISIBLE
                binding.buttonDeleteNewsModule.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
            }
    }


    private fun uninstallDynamicFeature(list: List<String>) {
        splitInstallManager.deferredUninstall(list)
            .addOnSuccessListener {
                binding.buttonDeleteNewsModule.visibility = View.GONE
                binding.buttonOpenNewsModule.visibility = View.GONE
            }
    }

    val listener = SplitInstallStateUpdatedListener {
        mySessionId = it.sessionId()
        when (it.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                val totalBytes = it.totalBytesToDownload()
                val progress = it.bytesDownloaded()
                // Update progress bar.
            }
            SplitInstallSessionStatus.INSTALLING -> Log.d("Status", "INSTALLING")
            SplitInstallSessionStatus.INSTALLED -> Log.d("Status", "INSTALLED")
            SplitInstallSessionStatus.FAILED -> Log.d("Status", "FAILED")
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> Log.d("Status", "REQUIRES_USER_CONFIRMATION")
        }
    }

    /*
    * SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION only occur when
    * attempting to download a sufficiently large module and to test it we have to upload it on playstore.
    * To get the user confirmation we use,
    *
    * SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> startIntentSender(it.resolutionIntent().intentSender, null,0, 0, 0)
    * */

    // private fun getAllModules() = SplitInstallManager.getInstalledModules()


    /*
    *     If the module is very less used feature in the app but is an important flow of the app
    * we keep that in dynamic-module. For example, Paid Features, OnBoarding Flow etc.

    If the dynamic module is initially integrated in the first build of APK can also be deleted later after the use.
    * Like after the OnBoarding of the user, we can delete the module to save some storage.
    *
    * If the module is very light in size we can keep as a library module.
    * */
}