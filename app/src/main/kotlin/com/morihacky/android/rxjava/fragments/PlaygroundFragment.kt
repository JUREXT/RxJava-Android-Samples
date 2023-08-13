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
import com.morihacky.android.rxjava.databinding.FragmentConcurrencySchedulersBinding

class PlaygroundFragment : Fragment() {

    private lateinit var binding: FragmentConcurrencySchedulersBinding

    private var _logsList: ListView? = null
    private var _adapter: LogAdapter? = null

    private var _logs: MutableList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConcurrencySchedulersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.btnStartOperation.setOnClickListener {
            setupLogger()
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private fun log(logMsg: String) {

        if (isCurrentlyOnMainThread()) {
            _logs.add(0, "$logMsg (main thread) ")
            _adapter?.clear()
            _adapter?.addAll(_logs)
        } else {
            _logs.add(0, "$logMsg (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper()).post {
                _adapter?.clear()
                _adapter?.addAll(_logs)
            }
        }
    }

    private fun setupLogger() {
        _logs = ArrayList()
        _adapter = LogAdapter(requireContext(), ArrayList())
        _logsList?.adapter = _adapter
    }

    private fun isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}