package com.android.chatty.AsyncTasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.chatty.ChatActivity;
import com.android.chatty.MainActivity;
import com.android.chatty.Entities.Message;
import com.android.chatty.InitThreads.ServerInit;

public class SendMessageServer extends AsyncTask<Message, Message, Message>{
	private static final String TAG = "SendMessageServer";
	private Context mContext;
	private static final int SERVER_PORT = 4446;
	private boolean isMine;


	public SendMessageServer(Context context, boolean mine){
		mContext = context;
		isMine = mine;
	}
	
	@Override
	protected Message doInBackground(Message... msg) {
//		Log.v(TAG, "doInBackground");
		
		//Display le message on the sender before sending it
		publishProgress(msg);
		
		//Send the message to clients
		try {			
			ArrayList<InetAddress> listClients = ServerInit.clients;
//			Log.e(TAG, "doInBackground: number of clients: "+ listClients.size() +" ");
			for(InetAddress addr : listClients){

				//// MARK: 16/06/2018 servers send msg to clients, so recording this instance IS necessary
				// before it leaves server
				msg[0].setUser_record(MainActivity.loadChatName(mContext));

				if(msg[0].getSenderAddress()!=null && addr.getHostAddress().equals(msg[0].getSenderAddress().getHostAddress())){
					return msg[0];
				}			
				
				Socket socket = new Socket();
				socket.setReuseAddress(true);
				socket.bind(null);
//				Log.e(TAG,"Connect to client: " + addr.getHostAddress());
				socket.connect(new InetSocketAddress(addr, SERVER_PORT));

//				Log.e(TAG, "doInBackground: connect to "+ addr.getHostAddress() +" succeeded");

				OutputStream outputStream = socket.getOutputStream();
				
				new ObjectOutputStream(outputStream).writeObject(msg[0]);
				
//			    Log.e(TAG, "doInBackground: write to "+ addr.getHostAddress() +" succeeded");
			    socket.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return msg[0];
	}

	@Override
	protected void onProgressUpdate(Message... values) {
		super.onProgressUpdate(values);
		
		if(isActivityRunning(MainActivity.class)){
			ChatActivity.refreshList(values[0], isMine);
		}
	}

	@Override
	protected void onPostExecute(Message result) {
//		Log.v(TAG, "onPostExecute");
		super.onPostExecute(result);
	}
	
	@SuppressWarnings("rawtypes")
	public Boolean isActivityRunning(Class activityClass)
	{
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
	}
}