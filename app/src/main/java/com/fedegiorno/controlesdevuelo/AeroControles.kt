package com.fedegiorno.controlesdevuelo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.fedegiorno.controlesdevuelo.databinding.ActivityAeroControlesBinding


//const val MAX_RADIO: Int = 500

class AeroControles : AppCompatActivity() {

    var dirBT = ""
    lateinit var aeroControlesBinding: ActivityAeroControlesBinding
    lateinit var BT: FedeBT

    var estadoConexion = FedeBT.Conexion.False

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aeroControlesBinding = ActivityAeroControlesBinding.inflate(layoutInflater)
        val mivista = aeroControlesBinding.root
        setContentView(mivista)

        dirBT = intent.getStringExtra("dirBT").toString()

        BT = FedeBT(this)

        BT.setDataLoadFinishedListener(object:FedeBT.ConnectedBluetooth{
            override fun onConnectState(state: FedeBT.Conexion) {
                when (state) {
                    FedeBT.Conexion.True -> {
                        Toast.makeText(applicationContext, "True", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                    }
                    FedeBT.Conexion.Pending -> {
                        Toast.makeText(applicationContext, "Pending", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                        rxReceived()
                    }
                    FedeBT.Conexion.False -> {
                        Toast.makeText(applicationContext, "False", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                    }
                    FedeBT.Conexion.Disconnect -> {
                        Toast.makeText(applicationContext, "Disconnect", Toast.LENGTH_SHORT).show()
                        estadoConexion = state
                        startActivity(Intent(applicationContext,MainActivity::class.java))
                    }
                }
            }
        })

        aeroControlesBinding.btnSend.setOnClickListener {
            BT.bluTx(aeroControlesBinding.edtSend.text.toString())
        }

    }
    /**
     * Se llama al siguiente método cuando cambia el foco de la ventana.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Toast.makeText(applicationContext, "Entro", Toast.LENGTH_SHORT).show()
        if (estadoConexion != FedeBT.Conexion.True){
            BT.connect(dirBT)
        }
    }

    private fun rxReceived() {
        BT.loadDateRx(object:FedeBT.ReceivedData{
            override fun rxDate(rx: String) {
                var datoRx = aeroControlesBinding.txtConsola.text.toString() + rx
                    aeroControlesBinding.txtConsola.text = datoRx
            }
        })
    }
}