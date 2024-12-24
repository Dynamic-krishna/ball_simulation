package org.krishna.ninja

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometer : Sensor
    private lateinit var sensorEventListener : SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var xAcceleration by mutableFloatStateOf(0f)
        var yAcceleration by mutableFloatStateOf(0f)

//        initialize accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sensorEventListener = object : SensorEventListener{
            override fun onSensorChanged(p0: SensorEvent?) {
                p0?.let {
                    xAcceleration = it.values[0]
                    yAcceleration = it.values[1]
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        enableEdgeToEdge()

        setContent {
//            App()
//            LiquidSimulation(xAcceleration,yAcceleration)
//            LiquidBox()
            MetaballsLiquidBox(
                modifier = Modifier.fillMaxSize(),
                xAcceleration = xAcceleration,
                yAcceleration = yAcceleration
            )
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}