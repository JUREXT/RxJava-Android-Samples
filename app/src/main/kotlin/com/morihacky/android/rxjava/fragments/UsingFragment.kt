package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.databinding.FragmentBufferBinding
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import org.reactivestreams.Publisher
import java.util.Random
import java.util.concurrent.Callable

class UsingFragment : Fragment() {

    private lateinit var binding: FragmentBufferBinding

    private lateinit var _logs: MutableList<String>
    private lateinit var _logsList: ListView
    private lateinit var _adapter: UsingFragment.LogAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBufferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _setupLogger()
        initListeners()
    }

    private fun initListeners() {
        binding.textDescription.setText(R.string.msg_demo_using)
        binding.btnStartOperation.setOnClickListener {
            executeUsingOperation()
        }
    }

    private fun executeUsingOperation() {
        val resourceSupplier = Callable<Realm> { Realm() }
        val sourceSupplier = Function<Realm, Publisher<Int>> { realm ->
            Flowable.just(true)
                .map {
                    realm.doSomething()
                    // i would use the copyFromRealm and change it to a POJO
                    Random().nextInt(50)
                }
        }
        val disposer = Consumer<Realm> { realm ->
            realm.clear()
        }

        Flowable.using(resourceSupplier, sourceSupplier, disposer)
            .subscribe({ i ->
                //log("got a value $i - (look at the logs)")
            })
    }

    inner class Realm {
        init {
            log("initializing Realm instance")
        }

        fun doSomething() {
            log("do something with Realm instance")
        }

        fun clear() {
            // notice how this is called even before you manually "dispose"
            log("cleaning up the resources (happens before a manual 'dispose'")
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private fun log(logMsg: String) {
        _logs.add(0, logMsg)

        // You can only do below stuff on main thread.
        Handler(Looper.getMainLooper()).post {
            _adapter.clear()
            _adapter.addAll(_logs)
        }
    }

    private fun _setupLogger() {
        _logs = ArrayList()
        _adapter = LogAdapter(requireContext(), ArrayList())
        _logsList.adapter = _adapter
    }

    private class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}