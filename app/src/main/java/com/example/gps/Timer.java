package com.example.gps;

import android.os.Handler;
import android.widget.TextView;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Timer {

    public void getTimer() {
        final java.util.Timer myTimer = new java.util.Timer();
        //Handler - это механизм, который позволяет работать с очередью сообщений.
        final Handler uiHandler = new Handler();

        //final TextView txtResult = findViewById(R.id.lastTrack);

        myTimer.schedule(new TimerTask() { // Определяем задачу


            @Override
            public void run() {
              /*  if (myAsyncTask != null) {
                    myAsyncTask.cancel(true);
                }
                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (track != null) txtResult.setText("" + track.trackName);
                        else
                            txtResult.setText("Ошибка выберите меньше API");
                    }
                });

                long answer = (long) -1;
                try {
                    answer = myAsyncTask.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (answer == -1) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMessage();
                        }
                    });

                    myTimer.cancel();
                }*/
            }

            ;

        }, 1000L, 20L * 1000); // интервал - 20000 миллисекунд,  миллисекунд до первого запуска.
    }
}
