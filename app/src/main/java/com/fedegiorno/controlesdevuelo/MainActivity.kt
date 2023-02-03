package com.fedegiorno.controlesdevuelo

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.fedegiorno.controlesdevuelo.FedeBT
import com.fedegiorno.controlesdevuelo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    lateinit var BT: FedeBT
    lateinit var devicesBluetooth: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val mivista = mainBinding.root
        setContentView(mivista)

        BT = FedeBT(this)
        BT.onBluetooth()

        mainBinding.listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()){
                BT.connect(devicesBluetooth[i])
                BT.setDataLoadFinishedListener(object:FedeBT.ConnectedBluetooth{
                    override fun onConnectState(state: FedeBT.Conexion) {
                        when(state){

                            FedeBT.Conexion.True->{
                                Toast.makeText(applicationContext,"True",Toast.LENGTH_SHORT).show()
                                mainBinding.listDeviceBluetooth.visibility = View.GONE
                                mainBinding.viewConn.visibility = View.VISIBLE
                                rxReceived()
                            }

                            FedeBT.Conexion.Pending->{
                                Toast.makeText(applicationContext,"Pending",Toast.LENGTH_SHORT).show()

                            }

                            FedeBT.Conexion.False->{
                                Toast.makeText(applicationContext,"False",Toast.LENGTH_SHORT).show()
                            }

                            FedeBT.Conexion.Disconnect->{
                                Toast.makeText(applicationContext,"Disconnect",Toast.LENGTH_SHORT).show()
                                mainBinding.listDeviceBluetooth.visibility = View.VISIBLE
                                mainBinding.viewConn.visibility = View.GONE
                            }

                        }
                    }
                })
            }
        }
        mainBinding.buttonSend.setOnClickListener {
            BT.bluTx(mainBinding.edtTx.text.toString())
        }

        mainBinding.buttonSend.setOnLongClickListener {
            BT.closeConnection()
            true
        }
    }

    private fun rxReceived() {
        BT.loadDateRx(object:FedeBT.ReceivedData{
            override fun rxDate(rx: String) {
                mainBinding.consola.text = mainBinding.consola.text.toString()+rx
            }
        })
    }

    /**
     * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes BLUETOOTH_SCAN y BLUETOOTH_CONNECT
     * en android 12 o superior se requieren permisos adicionales
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (BT.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            BT.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                BT.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!BT.stateBluetoooth() && requestCode == 100){
            BT.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = BT.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    mainBinding.listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
