package com.fedegiorno.controlesdevuelo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class FedeBT(val context: Context) {

    enum class Conexion {
        False, Pending, True, Disconnect
    }

    private var msjEntrante: InputStream? = null
    private var msjSaliente: OutputStream? = null

    val miUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    var btSocket: BluetoothSocket? = null
    val btAdapter = (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    var permisosRequeridos = listOf<String>()
    private var btPermisos = false

    private var conexion = Conexion.False

    fun onBluetooth(){
        if (!btPermisos){
            verifyPermission()
        }else{
            initializeBluetooth()
        }
    }

    private fun verifyPermission() {

        permisosRequeridos = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = permisosRequeridos.filter { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            initializeBluetooth()
        } else {
            (context as Activity).requestPermissions(missingPermissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun initializeBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        (context as Activity).startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),100)
    }

    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999

    }

    fun stateBluetoooth() = btAdapter.isEnabled

    fun checkPermissions(requestCode: Int, grantResults: IntArray):Boolean {
        return when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    btPermisos = true
                    // all permissions are granted
                    initializeBluetooth()
                    return true
                } else {
                    btPermisos = true
                    (context as Activity).requestPermissions(
                        permisosRequeridos.toTypedArray(),
                        BLUETOOTH_PERMISSION_REQUEST_CODE
                    )
                    return false
                }
            }
            // else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            else -> false
        }

    }

    fun deviceBluetooth():ArrayList<String>{
        val arrayListDevice = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            )== PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                var pairedDevices = btAdapter.bondedDevices

                for (i in pairedDevices) {
                    arrayListDevice.add(i.name+"\n"+i.address)
                }
            } else {
                Toast.makeText(context, "Conexion rechazada", Toast.LENGTH_LONG).show()
            }
        return arrayListDevice
    }

    interface ConnectedBluetooth{
        fun onConnectState(state: Conexion)
    }

    private var mConnectedFinish: ConnectedBluetooth? = null

    fun setDataLoadFinishedListener(date: ConnectedBluetooth) {
        this.mConnectedFinish = date
    }

    fun updateStateConnectBluetooth(state:Conexion) {
        mConnectedFinish!!.onConnectState(state)
    }


    /**
     * Conectar dispositivo bluetooth
     */
    fun connect(address:String){
        val dirAddres = address.subSequence(address.length-17,address.length).toString()
        val device = btAdapter.getRemoteDevice(dirAddres)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        btSocket = device.createInsecureRfcommSocketToServiceRecord(miUUID)
        //cancela el proceso de deteccion de dispositvos actual
        btAdapter.cancelDiscovery()
        thread(start = true){
            updateStateListen(Conexion.Pending)
            Conexion.Pending
            try {
                btSocket!!.connect()
                Conexion.True
                connectThread()
                updateStateListen(Conexion.True)
            }catch(e:Exception){
                updateStateListen(Conexion.False)
            }
        }
    }

    interface ReceivedData{
        fun rxDate(rx: String)
    }

    private var mreceivedDate: ReceivedData? = null

    fun loadDateRx(date: ReceivedData) {
        this.mreceivedDate = date
    }

    fun updateRxConnectBluetooth(message:String) {
        (context as Activity).runOnUiThread {
            mreceivedDate!!.rxDate(message)
        }
    }

    private fun connectThread() {
        var DatosIn: InputStream? = null
        var DatosOut: OutputStream? = null
        try {
            DatosIn = btSocket!!.inputStream
            DatosOut = btSocket!!.outputStream
        } catch (var6: IOException) {
        }
        msjEntrante = DatosIn
        msjSaliente = DatosOut
        bluRx()
    }

    private fun bluRx() {
        thread(start = true){
            while (true){
                try{
                    var input = BufferedReader(InputStreamReader(msjEntrante))
                    var rx = input.readLine()
                    updateRxConnectBluetooth(rx)
                }catch (e:IOException){
                    msjEntrante!!.close()
                    msjSaliente!!.close()
                    updateStateListen(Conexion.Disconnect)
                    break
                }
            }
        }
    }

    fun bluTx(message:String): Boolean {
        return try{
            btSocket!!.outputStream.write(message.toByteArray())
            true
        }catch(e:Exception){
            btSocket!!.close()
            updateStateListen(Conexion.Disconnect)
            false
        }
    }

    private fun updateStateListen(conn:Conexion){
        (context as Activity).runOnUiThread {
            updateStateConnectBluetooth(conn)
        }

    }


    fun closeConnection(){
        if (btSocket!=null){
            try {
                btSocket!!.close()
                updateStateListen(Conexion.Disconnect)
            }catch(e:Exception){
            }
            btSocket = null
        }
    }

/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    // all permissions are granted
                    initializeBluetooth()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permisosRequeridos.toTypedArray(),
                            BLUETOOTH_PERMISSION_REQUEST_CODE
                        )
                    }
                    Toast.makeText(context, "no tienes permisos", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

 */
}