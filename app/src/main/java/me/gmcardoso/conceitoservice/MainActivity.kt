package me.gmcardoso.conceitoservice

import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import me.gmcardoso.conceitoservice.LifetimeStartedService.Companion.EXTRA_LIFETIME
import me.gmcardoso.conceitoservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val lifetimeServiceIntent: Intent by lazy {
        //Intent(this, LifetimeStartedService::class.java)
        Intent(this, LifetimeBoundService::class.java)
    }

    private lateinit var lifetimeBoundService: LifetimeBoundService
    private var connected = false
    private val serviceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            lifetimeBoundService =
                (binder as LifetimeBoundService.LifetimeBoundServiceBinder)
                    .getService()
            connected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            connected = false
        }
    }

    private inner class LifetimeServiceHandler(lifetimeServiceLooper: Looper): Handler(lifetimeServiceLooper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(connected) {
                runOnUiThread {
                    activityMainBinding.serviceLifetimeTv.text =
                        lifetimeBoundService.lifetime.toString()
                }
                obtainMessage().also {
                    sendMessageDelayed(it, 1000)
                }
            }
        }
    }

    private lateinit var lifetimeServiceHandler: LifetimeServiceHandler

    // BroadcastReceiver que recebe o lifetime do serviço
//    private val receiveLifetimeBr: BroadcastReceiver by lazy {
//        object: BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                intent?.getIntExtra(EXTRA_LIFETIME, 0).also { lifetime ->
//                    activityMainBinding.serviceLifetimeTv.text = lifetime.toString()
//                }
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        HandlerThread("LifetimeHandlerThread").apply {
            start()
            lifetimeServiceHandler = LifetimeServiceHandler(looper)
        }

        with(activityMainBinding) {
            startServiceBtn.setOnClickListener {
                //startService(lifetimeServiceIntent)
                bindService(lifetimeServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                lifetimeServiceHandler.obtainMessage().also {
                    lifetimeServiceHandler.sendMessageDelayed(it, 1000)
                }
            }

            stopServiceBtn.setOnClickListener {
                //stopService(lifetimeServiceIntent)
                unbindService(serviceConnection)
                //connected = false
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        //registerReceiver(receiveLifetimeBr, IntentFilter("ACTION_RECEIVE_LIFETIME"))
//    }
//
//    override fun onStop() {
//        super.onStop()
//        //unregisterReceiver(receiveLifetimeBr)
//    }
}