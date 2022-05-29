package com.example.coroutinestestapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.coroutinestest.CoroutinesTestMainClass
import com.example.coroutinestestapp.databinding.HomeFragmentBinding
import com.example.coroutinestestapp.viewmodels.HomeViewModel
import com.example.testapp.RxTestClass
import io.reactivex.disposables.CompositeDisposable


class HomeFragment : Fragment() {
    private lateinit var cancellButton: Button
    private lateinit var startButton: Button
    private lateinit var createCoroutinesButton: Button
    private lateinit var subscribeSingleBtn: Button
    private lateinit var cancellableStartBtn: Button
    private lateinit var cancellableEndBtn: Button

    val coroutinesMainTest = CoroutinesTestMainClass()
    val rxTestClass: RxTestClass = RxTestClass()

    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var dataBinding: HomeFragmentBinding
    lateinit var  viewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        //return inflater.inflate(R.layout.home_fragment, container, false)
        dataBinding = HomeFragmentBinding.inflate(inflater)
            .also{
                it.lifecycleOwner = this
                it.viewModel = viewModel
            }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testLayoutInit()
        if(compositeDisposable==null){
            compositeDisposable = CompositeDisposable()
        }
        Log.d("onCreate main", "**onCreate: ")

        coroutinesMainTest.apply {
//            testJoin()
//            onRun()
//            onRunJoin()
//            onRunTwoJoins()
//            onRunWithCancell()
//            onRunDeffered()
//            onRunDefferedAsync()
//            scopeExceptionHandlerTest()
//            testChannels()
//            testFlow()
        }
//        val algosClass = AlgosTest()
//        val result = algosClass.binarySearch(algosClass.array,algosClass.toSearch)
//        print("**Result is $result")

    }

    private fun testLayoutInit() {
        cancellButton = dataBinding.cancellBtn
        startButton = dataBinding.startBtn
        createCoroutinesButton = dataBinding.createCoroutineBtn
        subscribeSingleBtn = dataBinding.subscribeSingleBtn
        cancellableStartBtn = dataBinding.startCancellableCoroutineBtn
        cancellableEndBtn = dataBinding.cancellableStopBtn

        cancellButton.setOnClickListener(View.OnClickListener {
            coroutinesMainTest.scopeFinish()
        })
        startButton.setOnClickListener(View.OnClickListener {
            coroutinesMainTest.onScopeCoroutinesStart()
        })
        createCoroutinesButton.setOnClickListener(View.OnClickListener {
            coroutinesMainTest.coroutineJobCreate()
        })
        subscribeSingleBtn.setOnClickListener(View.OnClickListener {
            compositeDisposable?.add(rxTestClass.testSingle().subscribe {
                subscribeSingleBtn.setText(it.toString())
            })
        })
        cancellableStartBtn.setOnClickListener(View.OnClickListener {
            coroutinesMainTest.suspendCancellableCoroutinetest()
        })
        cancellableEndBtn.setOnClickListener(View.OnClickListener {
            coroutinesMainTest.scopeFinish()
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutinesMainTest.scopeFinish()
        compositeDisposable?.dispose()
    }

}