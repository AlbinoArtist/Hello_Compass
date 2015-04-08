package albinoartist.hellocompass;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

public class CompassActivity extends Activity implements SensorEventListener {
    Float azimut;  // View to draw a compass
    float SmoothFactorCompass = 0.5f;
    float SmoothThresholdCompass = 10.0f;
    float oldCompass = 0.0f;

    public class CustomDrawableView extends View {
        Paint paint = new Paint();

        public CustomDrawableView(Context context) {
            super(context);
            paint.setColor(0xff00ff00);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setAntiAlias(true);
            paint.setTextSize(40f);
        }

        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int centerx = width / 2;
            int centery = height / 2;

            paint.setColor(Color.BLUE);
            canvas.drawLine(centerx - 120, centery, centerx + 120, centery, paint);
            canvas.drawLine(centerx + 120, centery, centerx, centery - 300, paint);
            canvas.drawLine(centerx, centery - 300, centerx - 120, centery, paint);


            // Rotate the canvas with the azimut
            if (azimut != null){
                    canvas.rotate(testCompass(-azimut * 360 / (2 * 3.14159f)), centerx, centery);

            }
            paint.setColor(Color.GRAY);
            canvas.drawLine(-10000, centery, 10000, centery, paint);
            canvas.drawLine(centerx, -10000, centerx, 10000, paint);
            canvas.drawCircle(centerx,centery,5,paint);
            canvas.drawCircle(centerx,centery,300,paint);
            paint.setColor(Color.RED);
            canvas.drawText("N",(float)centerx,(float)centery-315,paint);
            canvas.drawText("S",(float)centerx+5,(float)centery+330,paint);
            canvas.drawText("W",(float)centerx-335,(float)centery,paint);
            canvas.drawText("E",(float)centerx+310,(float)centery,paint);




        }
    }

    CustomDrawableView mCustomDrawableView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomDrawableView = new CustomDrawableView(this);
        setContentView(mCustomDrawableView);    // Register the sensor listeners
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
        mCustomDrawableView.invalidate();
    }
    public float testCompass(float newCompass){
        if (Math.abs(newCompass - oldCompass) < 180) {
            if (Math.abs(newCompass - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newCompass;
            }
            else {
                oldCompass = oldCompass + SmoothFactorCompass * (newCompass - oldCompass);
            }
        }
        else {
            if (360.0 - Math.abs(newCompass - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newCompass;
            }
            else {
                if (oldCompass > newCompass) {
                    oldCompass = (oldCompass + SmoothFactorCompass * ((360 + newCompass - oldCompass) % 360) + 360) % 360;
                }
                else {
                    oldCompass = (oldCompass - SmoothFactorCompass * ((360 - newCompass + oldCompass) % 360) + 360) % 360;
                }
            }
        }
        return oldCompass;
    }
}

