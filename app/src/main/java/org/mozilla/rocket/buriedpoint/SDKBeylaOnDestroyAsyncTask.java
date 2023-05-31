package org.mozilla.rocket.buriedpoint;


import com.anysitebrowser.base.core.stats.Stats;
import com.anysitebrowser.taskdispatcher.task.ExecutorType;
import com.anysitebrowser.taskdispatcher.task.impl.AsyncTaskJob;

/**
 * SDKBeylaOnDestroyAsyncTask 子线程任务
 * 加入到任务调度器里后被执行
 */
public class SDKBeylaOnDestroyAsyncTask extends AsyncTaskJob {
    @Override
    public void run() {

        Stats.onAppDestroy();
    }

    @Override
    public int executeType() {
        return ExecutorType.IO;
    }
}
