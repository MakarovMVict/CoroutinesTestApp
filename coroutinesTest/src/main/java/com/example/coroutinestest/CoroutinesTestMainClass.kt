package com.example.coroutinestest

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CoroutinesTestMainClass {

    private var formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val handler = CoroutineExceptionHandler { context, exception ->
        Log.d("CoroutinesTestMainClass", "*first coroutine exception $exception in ${context[CoroutineName]?.name}")
    }

    private val scope = CoroutineScope(Job())
    private val userData = UserData(1, "Maxim", 28)
    private val customScope = CoroutineScope(Job() + Dispatchers.Default + userData)
    private val cancellableScope = CoroutineScope(Job())
    private lateinit var job: Job
    private val supervisorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)


    fun testJoin() {
        Log.d("CoroutinesTestMainClass", "** Test Join!!")

    }

    fun onRun() {
        Log.d("CoroutinesTestMainClass", "** onRun, start")

        scope.launch {
            Log.d("CoroutinesTestMainClass", "** coroutine, start")
            TimeUnit.MILLISECONDS.sleep(1000)
            Log.d("CoroutinesTestMainClass", "** coroutine, end")
        }

        Log.d("CoroutinesTestMainClass", "** onRun, middle")

        scope.launch {
            Log.d("CoroutinesTestMainClass", "** coroutine2, start")
            TimeUnit.MILLISECONDS.sleep(1500)
            Log.d("CoroutinesTestMainClass", "** coroutine2, end")
        }

        Log.d("CoroutinesTestMainClass", "** onRun, end")
    }

    fun onRunJoin() {
        scope.launch {
            Log.d("CoroutinesTestMainClass", "*parent coroutine, start")

            val job = launch {
                Log.d("CoroutinesTestMainClass", "*child coroutine, start")
                TimeUnit.MILLISECONDS.sleep(1000)
                Log.d("CoroutinesTestMainClass", "*child coroutine, end")
            }

            Log.d("CoroutinesTestMainClass", "*parent coroutine, wait until child completes")
            job.join()//не завершается родительская пока не отработает эта

            Log.d("CoroutinesTestMainClass", "*parent coroutine, end")
        }
    }

    fun onRunTwoJoins() {
        scope.launch {
            Log.d("CoroutinesTestMainClass", "*parent coroutine, start")

            val job = launch {
                Log.d("CoroutinesTestMainClass", "*child coroutine 1, start")
                TimeUnit.MILLISECONDS.sleep(1000)
                Log.d("CoroutinesTestMainClass", "*child coroutine 1, end")
            }

            val job2 = launch {
                Log.d("CoroutinesTestMainClass", "*child coroutine 2 , start")
                TimeUnit.MILLISECONDS.sleep(1500)
                Log.d("CoroutinesTestMainClass", "*child coroutine 2, end")
            }

            Log.d("CoroutinesTestMainClass", "*parent coroutine, wait until children complete")
            job.join()
            job2.join()

            Log.d("CoroutinesTestMainClass", "*parent coroutine, end")
        }
    }

    fun onRunWithCancell() {
        Log.d("CoroutinesTestMainClass", "** onRunWithCancell, start")

        if (job.isActive) return

        job = scope.launch {
            Log.d("CoroutinesTestMainClass", "*coroutine, start")
            var x = 0
            while (x < 20 && isActive) {
                delay(1000)//if cancell then no logs after(instead of timeunit.MILLISECONDS.sleep(1000))
                Log.d("CoroutinesTestMainClass", "*coroutine, ${x++}, isActive = ${isActive}")
            }
            Log.d("CoroutinesTestMainClass", "*coroutine, end")
        }

        Log.d("CoroutinesTestMainClass", "*onRun, end")
    }

    fun coroutineJobCreate() {
        //needs start
        job = scope.launch(start = CoroutineStart.LAZY) {
            Log.d("CoroutinesTestMainClass", "*coroutine, start")
            TimeUnit.MILLISECONDS.sleep(1000)
            Log.d("CoroutinesTestMainClass", "*coroutine, end")
        }
    }

    fun onScopeCoroutinesStart() {//for coroutineJobCreate() starts coroutine
        Log.d("CoroutinesTestMainClass", "*onScopeCoroutinesStart start")
        job.start()
        Log.d("CoroutinesTestMainClass", "*onScopeCoroutinesStart end")
    }

    fun onRunDeffered() {
        scope.launch {
            Log.d("CoroutinesTestMainClass", "*parent coroutine, start")

            val deferred = async() {
                Log.d("CoroutinesTestMainClass", "*child coroutine, start")
                TimeUnit.MILLISECONDS.sleep(1000)
                Log.d("CoroutinesTestMainClass", "*child coroutine, end")
                "*async result"
            }

            Log.d("CoroutinesTestMainClass", "*parent coroutine, wait until child returns result")
            val result = deferred.await()
            Log.d("CoroutinesTestMainClass", "*parent coroutine, child returns: $result")

            Log.d("CoroutinesTestMainClass", "*parent coroutine, end")
        }
    }

    fun onRunDefferedAsync() {
        scope.launch {
            Log.d("CoroutinesTestMainClass", "*parent coroutine, start")
            //get data asyncronous(can be launched from mainhread) , onRunDefferedAsync() works 2500ms
            val data = async { getData() }
            val data2 = async { getData2() }

            Log.d("CoroutinesTestMainClass", "*parent coroutine, wait until children return result")
            val result = "${data.await()}, ${data2.await()}"
            Log.d("CoroutinesTestMainClass", "*parent coroutine, children returned: $result")

            Log.d("CoroutinesTestMainClass", "*parent coroutine, end")
        }
    }

    private suspend fun getData(): String {
        delay(1000)
        return "*data"
    }

    private suspend fun getData2(): String {

        delay(1500)
        return "*data2"
    }

    private fun getInfoFromCustoContextObjectSimple() {
        scope.launch {
            val scopeCustom = coroutineContext[UserData]
            Log.d("CoroutinesTestMainClass",
                "** scope custom tostring - ${scopeCustom.toString()}")
        }
    }
    /** it's when one coroutine failed with exeption, but other coroutines in scope works after it
     * (only possible when SupervisorJob() used ) !!!
     * lesson 13
     * in case of inner coroutines with SupervisorJob() all parent and child coroutines(not all in scope) will be stopped
     **/
    fun scopeExceptionHandlerTest() {

        supervisorScope.launch(CoroutineName("first_coroutine")) {//in case of SupervisorJob scope doesn't cancel in case of one schild coroutine finished with an exception
            TimeUnit.MILLISECONDS.sleep(1000)
            Integer.parseInt("a")
        }

        supervisorScope.launch(CoroutineName("second_coroutine")) {
            repeat(5) {
                TimeUnit.MILLISECONDS.sleep(300)
                Log.d("CoroutinesTestMainClass","*second coroutine isActive ${isActive}")
            }
        }
    }

    fun suspendCancellableCoroutinetest(){
        cancellableScope.launch {
            suspendCancellableCoroutine<Void> { continuation ->

                repeat(10) {
                    TimeUnit.MILLISECONDS.sleep(1000)
                    Log.d("CoroutinesTestMainClass","**suspendCancellable coroutine isActive ${isActive}")
                }
                continuation.invokeOnCancellation {//calls after work is done
                    Log.d("CoroutinesTestMainClass","**suspendCancellable coroutine onCancell ${isActive}")

                }

            }
        }
    }

    /**
     * lesson 17 : do smth/handle in different threads
     */
    fun handleWithContext() {

        scope.launch {
            launch {
                // get data from network or database
                // ...

                withContext(Dispatchers.Main) {
                    // show data on the screen
                    // ...
                }

                // ...
            }
        }
    }

    /***
     *  урок 18 Channels
     */

    fun testChannels() {
        val channel = Channel<Int>(Channel.CONFLATED)//only one value

        scope.launch {
            repeat(7) {
                delay(300)
                Log.d("CoroutinesTestMainClass","**send $it")
                channel.send(it)
            }
            Log.d("CoroutinesTestMainClass","close")
            channel.close()
        }

        scope.launch {
            channel.cancel()
            for (element in channel) {
                Log.d("CoroutinesTestMainClass","**received $element")
                delay(1000)
            }
        }
    }

    fun scopeFinish() {
        Log.d("CoroutinesTestMainClass", "** scope cancel")
        scope.cancel()
        supervisorScope.cancel()
        customScope.cancel()
        cancellableScope.cancel()
    }
    /**
     * Lesson 19 flow
     */
    fun testFlow() {
        fun flowFun(): Flow<Int> {//no necessary for flowFun to be suspend
            return flow {
                for (value in 1..10){
                    emit(value)
                }
            }
        }
        val flowFunRealization = flowFun()
        scope.launch {//code inside flow starts to work when we call "collect"
            flowFunRealization.collect { value -> //collect and code inside flow are suspend functions
                Log.d("CoroutinesTestMainClass", "** flow emits $value")
            }
        }
    }
    /**
     * lessom 21 flow channels
     */
    fun testChannelFlow() {
        scope.launch {
            flow {
                coroutineScope {
                    val channel = produce<Int> {//new coroutine + channel
                        launch {
                            delay(1000)
                            send(1)
                        }
                        launch {
                            delay(1000)
                            send(2)
                        }
                        launch {
                            delay(1000)
                            send(3)
                        }
                    }
                    channel.consumeEach {//just suspend fun
                        emit(it)
                    }
                }
            }
        }
    }

//    val commentsFlow = db.commentDao().getAll() берёт изменения в дб и отправляет в лайвдата
//    val comments = liveData {
//        commentsFlow.collect {
//            emit(it)
//        }
//    }

}


data class UserData(
    val id: Long,
    val name: String,
    val age: Int,
) : AbstractCoroutineContextElement(UserData) {
    companion object Key : CoroutineContext.Key<UserData>
}