package my.code.repository.utils;

import android.os.AsyncTask;

/**
 * A async task util, may not be used very much.
 *
 * @author djh on  2018/8/3 10:26
 * @E-Mail 1544579459@qq.com
 */
public class AsyncTaskUtil {


    public static AsyncTask startTask(Runnable preExecute, Runnable doInBackground, Runnable postExecute) {
        InnerAsyncTask asyncTask = new InnerAsyncTask(preExecute, doInBackground, postExecute);
        asyncTask.execute();
        return asyncTask;
    }

    static class InnerAsyncTask extends AsyncTask<Void, Void, Void> {
        private Runnable mPreExecute;
        private Runnable mDoInBackground;
        private Runnable mPostExecute;

        InnerAsyncTask(Runnable preExecute, Runnable doInBackground, Runnable postExecute) {
            mPreExecute = preExecute;
            mDoInBackground = doInBackground;
            mPostExecute = postExecute;
        }

        @Override
        protected void onPreExecute() {
            mPreExecute.run();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDoInBackground.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mPostExecute.run();
        }
    }
}
