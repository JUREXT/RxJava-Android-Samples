package com.morihacky.android.rxjava.fragments


import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.morihacky.android.rxjava.databinding.FragmentRotationPersistBinding
import com.morihacky.android.rxjava.ext.plus
import com.morihacky.android.rxjava.wiring.LogAdapter
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class RotationPersist3Fragment : Fragment() { // TODO: Fx problems with view-model.

    private lateinit var binding: FragmentRotationPersistBinding

    private lateinit var logList: ListView
    private lateinit var adapter: LogAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private var logs: MutableList<String> = ArrayList()
    private var disposables = CompositeDisposable()

    // -----------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRotationPersistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // logList = binding.logList
        startOperationFromWorkerFrag()
    }

    private fun startOperationFromWorkerFrag() {
        logs = ArrayList<String>()
        adapter.clear()

        disposables +=
            sharedViewModel
                .sourceStream()
                .subscribe { l ->
                    log("Received element $l")
                }
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupLogger()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    private fun setupLogger() {
        logs = ArrayList<String>()
        adapter = LogAdapter(activity, ArrayList<String>())
        logList.adapter = adapter
    }

    private fun log(logMsg: String) {
        logs.add(0, logMsg)

        // You can only do below stuff on the main thread.
        Handler(getMainLooper())
            .post {
                adapter.clear()
                adapter.addAll(logs)
            }
    }
}

class SharedViewModel : ViewModel() {
    private var disposable: Disposable? = null

    private val sharedObservable: Flowable<Long> =
        Flowable.interval(1, TimeUnit.SECONDS)
            .take(20)
            .doOnNext {
                   // l -> Timber.tag("KG").d("onNext $l")
            }
            // .replayingShare()
            .replay(1)
            .autoConnect(1) { t -> disposable = t }

    fun sourceStream(): Flowable<Long> {
        return sharedObservable
    }

    override fun onCleared() {
        super.onCleared()
        //Timber.tag("KG").d("Clearing ViewModel")
        disposable?.dispose()
        // MyApp.getRefWatcher().watch(this)
    }
}
