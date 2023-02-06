package com.fedegiorno.controlesdevuelo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.fedegiorno.controlesdevuelo.databinding.ActivityAeroControlesBinding

const val MAX_RADIO: Int = 360
const val AMPLITUD_ALERON: Int = 30
const val AMPLITUD_TIMON_PROFUNDIDAD: Int = 35
const val TIMON: Float = 70f
const val TAG: String = "KIRCHOFFF"

class AeroControles : AppCompatActivity() {

    var dirBT = ""
    lateinit var BT: FedeBT
    var estadoConexion = FedeBT.Conexion.False

    lateinit var miVistaCockpit: VistaCockpit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        miVistaCockpit = VistaCockpit(this)
        setContentView(miVistaCockpit)

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
                        Toast.makeText(applicationContext, "Pendiente", Toast.LENGTH_SHORT).show()
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
    } /** fin del onCreate **/

     override fun onWindowFocusChanged(hasFocus: Boolean) {
        /**
         * Se llama a este método cuando cambia el foco de la ventana.
         */
        super.onWindowFocusChanged(hasFocus)
        Toast.makeText(applicationContext, "Entro", Toast.LENGTH_SHORT).show()
        if (estadoConexion != FedeBT.Conexion.True){
            BT.connect(dirBT)
        }
    }

    private fun rxReceived() {
        BT.loadDataRx(object:FedeBT.ReceivedData{
            override fun rxDate(rx: String) {
//                var datoRx = aeroControlesBinding.txtConsola.text.toString() + rx
//                    aeroControlesBinding.txtConsola.text = datoRx
            }
        })
    }
    /* al final de la activity */
    inner class VistaCockpit (context: Context?) : View(context) {
        private var dX : Float = 0.0f
        private var dY : Float = 0.0f
        var relX : Float = 0.0f
        var relY : Float = 0.0f
        private var cx : Float = 0.0f
        private var cy : Float = 0.0f
        private var cuadrante: Int = 0
        private var amplitud: Double = 0.0
        private var accion: String = "accion"

        @SuppressLint("DrawAllocation")
        public override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val paint = Paint()
            val ancho = width
            val alto = height

            if (canvas != null) {
                canvas.drawPaint(paint)
            }
            paint.setColor(Color.BLACK)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.GREEN
            cx = ancho.div(2f)
            cy = alto.div(2f)

            //circulos concentricos
            for (radio in 60..MAX_RADIO step 60){
                if (canvas != null) {
                    canvas.drawCircle(cx, cy, radio.toFloat(), paint)
                }
            }

            //cuadrantes
            if (canvas != null) {
                canvas.drawLine(cx- MAX_RADIO,cy,cx + MAX_RADIO,cy,paint)
            }
            if (canvas != null) {
                canvas.drawLine(cx,cy- MAX_RADIO,cx,cy + MAX_RADIO,paint)
            }

            //timon
            paint.style = Paint.Style.FILL
            paint.color = Color.YELLOW
            var dx = MAX_RADIO*Math.abs(relX).div(amplitud).toFloat()
            var dy = MAX_RADIO*Math.abs(relY).div(amplitud).toFloat()

            when (accion) {
                "DOWN" -> {
                    Log.i("KIRCHOFFF", "Cuadrante " + cuadrante)
                    paint.color = Color.GREEN

                    if (amplitud >= MAX_RADIO){
                        when (cuadrante) {
                            1 -> {
                                dX = dx + cx
                                dY = cy - dy
                            }
                            2 -> {
                                dX = cx - dx
                                dY = cy - dy
                            }
                            3 -> {
                                dX = cx - dx
                                dY = cy + dy
                            }
                            4 -> {
                                dX = dx + cx
                                dY = dy + cy
                            }
                        }
                    }
                    if (canvas != null) {
                        canvas.drawCircle(dX, dY, TIMON, paint)
                    }
                    Log.i("KIRCHOFFF", "DOWN")
                    transmitirMovimiento(relX,relY)
                }
                "MOVE" -> {
                    paint.color = Color.GREEN
                    if (amplitud >= MAX_RADIO){
                        when (cuadrante) {
                            1 -> {
                                dX = dx + cx
                                dY = cy - dy
                            }
                            2 -> {
                                dX = cx - dx
                                dY = cy - dy
                            }
                            3 -> {
                                dX = cx - dx
                                dY = cy + dy
                            }
                            4 -> {
                                dX = dx + cx
                                dY = dy + cy
                            }
                        }
                    }
                    if (canvas != null) {
                        canvas.drawCircle(dX, dY, TIMON, paint)
                    }
                    Log.i("KIRCHOFFF", "MOVE")
                    transmitirMovimiento(relX,relY)
                }
                "UP" -> {
                    paint.color = Color.YELLOW
                    Thread.sleep(500)
                    if (canvas != null) {
                        canvas.drawCircle(cx, cy, TIMON, paint)
                    }
                    Log.i("KIRCHOFFF", "UP")
                    transmitirMovimiento(0f,0f)
                }
                else  -> {
                    Thread.sleep(500)
                    if (canvas != null) {
                        canvas.drawCircle(cx, cy, TIMON, paint)
                    }
                    Log.i("KIRCHOFFF", "ELSE")
                }
            }
        } /** fin del onDraw() **/

        private fun calcularAbsY(relY: Float): Float {
            dY = relY + cy
            return dY
        }

        private fun calcularAbsX(relX: Float): Float {
            dX = relX + cx
            return dX
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            dX = e.x
            dY = e.y
            relX = dX - cx
            relY = cy - dY
            amplitud = Math.sqrt(Math.pow(dY.toDouble()-cy.toDouble(),2.0)+Math.pow(dX.toDouble()-cx.toDouble(),2.0))

            //Log.i("KIRCHOFFF", "Punto absoluto X = " + dX + " Y = " + dY)
            //Log.i("KIRCHOFFF", amplitud.toString())

            if ((relX >= 0) && (relY >= 0)) {cuadrante = 1
            } else {
                if ((relX <= 0) && (relY >= 0)) {cuadrante = 2
                } else {
                    if ((relX <= 0) && (relY <= 0)) {cuadrante = 3
                    } else {
                        if ((relX >= 0) && (relY <= 0)) {cuadrante = 4}
                    }
                }
            }
            //Log.i("KIRCHOFFF", "Cuadrante " + cuadrante.toString())

            when(e.action) {
                MotionEvent.ACTION_DOWN -> {
                    accion = "DOWN"
                }
                MotionEvent.ACTION_MOVE -> {
                    accion = "MOVE"
                }
                MotionEvent.ACTION_UP -> {
                    accion = "UP"
                }
                else -> {
                    return false
                }
            }
            invalidate()
            return true
        }

    }
    private fun transmitirMovimiento(relX: Float, relY: Float) {
        var X = 0f
        var Y = 0f

        //Alerones
        if (relX >= 0) {
            if (relX < MAX_RADIO.toFloat()){
                X = 90f - relX.div(MAX_RADIO.toFloat())*(AMPLITUD_ALERON.toFloat())
            } else {
                X = 90f - AMPLITUD_ALERON.toFloat()
            }
        } else {
            if (relX > -MAX_RADIO.toFloat()){
                X = 90f - relX.div(MAX_RADIO.toFloat())*(AMPLITUD_ALERON.toFloat())
            } else {
                X = 90f + AMPLITUD_ALERON.toFloat()
            }
        }

        //Timon de profundidad
        if (relY >= 0) {
            if (relY < MAX_RADIO.toFloat()){
                Y = 90f - relY.div(MAX_RADIO.toFloat())*(AMPLITUD_TIMON_PROFUNDIDAD.toFloat())
            } else {
                Y = 90f - AMPLITUD_TIMON_PROFUNDIDAD.toFloat()
            }
        } else {
            if (relY > -MAX_RADIO.toFloat()){
                Y = 90f - relY.div(MAX_RADIO.toFloat())*(AMPLITUD_TIMON_PROFUNDIDAD.toFloat())
            } else {
                Y = 90f + AMPLITUD_TIMON_PROFUNDIDAD.toFloat()
            }
        }

        Log.i(TAG, "relX = " + relX.toString())
        Log.i(TAG, "relY = " + relY.toString())
        Log.i(TAG, "Inclinacion alerones = " + X.toString())
        Log.i(TAG, "Inclinacion timon profundidad = " + Y.toString())
        BT.bluTx("@" + X.toString())    //inclinacion alerones
        BT.bluTx("#" + Y.toString())    //inclinacion timon profundidad
    }


}