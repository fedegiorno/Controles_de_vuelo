package com.fedegiorno.controlesdevuelo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View

class VistaCockpit (context: Context?) : View(context) {
    //internal class Mivista(context: Context?) : View(context) {
        private var dX : Float = 0.0f
        private var dY : Float = 0.0f
        private var relX : Float = 0.0f
        private var relY : Float = 0.0f
        private var cx : Float = 0.0f
        private var cy : Float = 0.0f
        private var cuadrante: Int = 0
        private var amplitud: Double = 0.0
        private var accion: String = "accion"

        @SuppressLint("DrawAllocation")
        public override fun onDraw(canvas: Canvas) {
            val paint = Paint()
            paint.setColor(Color.BLACK)
            canvas.drawPaint(paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.GREEN
            val ancho = width
            val alto = height
            cx = ancho.div(2f)
            cy = alto.div(2f)
            //Log.i("KIRCHOFFF", "cX = " + cx + " cY = " + cy)
            //circulos concentricos
            for (radio in 100..MAX_RADIO step 100){
                canvas.drawCircle(cx, cy, radio.toFloat(), paint)
            }

            //cuadrantes
            canvas.drawLine(cx- MAX_RADIO,cy,cx + MAX_RADIO,cy,paint)
            canvas.drawLine(cx,cy- MAX_RADIO,cx,cy + MAX_RADIO,paint)

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
                    canvas.drawCircle(dX, dY, TIMON, paint)
                    Log.i("KIRCHOFFF", "DOWN")
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
                    canvas.drawCircle(dX, dY, TIMON, paint)
                    //Log.i("KIRCHOFFF", "Amplitud = " + amplitud.toString())
                    Log.i("KIRCHOFFF", "MOVE")
                }
                "UP" -> {
                    paint.color = Color.YELLOW
                    canvas.drawCircle(cx, cy, TIMON, paint)
                    Log.i("KIRCHOFFF", "UP")
                } else  -> {
                canvas.drawCircle(cx, cy, TIMON, paint)
                Log.i("KIRCHOFFF", "ELSE")
            }
            }
        }

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
