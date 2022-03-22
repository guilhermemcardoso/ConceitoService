package me.gmcardoso.conceitoservice

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LifetimeStartedService : Service() {

    //Contador de segundos
    private var lifetime: Int = 0

    companion object {
        // Para passar o lifetime entre Activity e Service
        val EXTRA_LIFETIME = "EXTRA_LIFETIME"
    }

    // Nossa thread de trabalho que conta segundos em background
    private inner class WorkerThread: Thread() {
        var running = false
        override fun run() {
            running = true
            while(running) {
                // Dorme 1 segundo
                sleep(1000)
                // Envia o lifetime para a Activity
                sendBroadcast(Intent("ACTION_RECEIVE_LIFETIME").also {
                    it.putExtra(EXTRA_LIFETIME, ++lifetime)
                })
            }

        }
    }

    private lateinit var workerThread: WorkerThread

    override fun onCreate() {
        super.onCreate()
        workerThread = WorkerThread()
    }

    // Só faz sentido se for serviço vinculado, senão retorna null
    override fun onBind(intent: Intent): IBinder? = null

    // Chamado quando a Activity executa startService.
    // Executa indefinidamente até que seja chamado o método stopSelf (serviço)
    // ou stopService (activity)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!workerThread.running) {
            workerThread.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        workerThread.running = false
    }
}