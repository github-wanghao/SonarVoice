package com.example.ddvoice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ddvoice.util.SystemUiHider;




import com.iflytek.cloud.*;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import com.example.ddvoice.JsonParser;









import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
//import android.speech.SpeechRecognizer;//�������
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;



/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements OnItemClickListener ,OnClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
	
	//�����ϳ�
	// �����ϳɶ���
		private SpeechSynthesizer mTts;

		// Ĭ�Ϸ�����
		private String voicer="xiaoyan";
	
	
	
	//����
	
	
	
	public static boolean serviceFlag=false;//��ʾ�Ƿ���һ������� 
	private int mainServiceID=0;//��ʾĿǰ�Ի���������������һ����������
	private int branchServiceID=0;//��ʾĿǰ�Ի��������������������е���һ���֧������
	
	public static JSONObject semantic = null,slots =null,answer=null,datetime=null,location=null,data=null;public static String operation = null,service=null;
	public static JSONArray result=null;
	public static String receiver=null, name = null,price=null,code=null,song = null,keywords=null,content=null,
			url=null,text=null,time=null,date=null,city=null,sourceName=null,target=null,source=null;
	public static String[] weatherDate=null,weather=null,tempRange=null,airQuality=null,wind=null,humidity=null,windLevel=null;

	
	
	
	private TextUnderstander mTextUnderstander;// �����������ı������壩��
	
	
	//from SiriCN
	private ProgressDialog mProgressDialog;//������ʾ��
	private MediaPlayer player;//��������
	
	private ListView mListView;
	private ArrayList<SiriListItem> list;
	ChatMsgViewAdapter mAdapter;



	public static  String SaveResult="";	//����ʶ����
	public static  String SRResult="";	//ʶ����
	private static String SAResult="";//����ʶ����
	public static  String ResultTag = ""; //ʶ������ʶ
	private static String TAG = MainActivity.class.getSimpleName();
	//Toast��ʾ��Ϣ
	private Toast info;
	//�ı�����
	private TextView textView;
	//����ʶ��
	private SpeechRecognizer mIat;
	// ������дUI
	private RecognizerDialog mIatDialog;
	// ��HashMap�洢��д���
		private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// ��������
		private String mEngineType = SpeechConstant.TYPE_CLOUD;
		private String mEngineTypeTTS = SpeechConstant.TYPE_CLOUD;
		private SharedPreferences mSharedPreferences;
		private SharedPreferences mSharedPreferencesTTS;
		
		
	//����ʶ�������
		private RecognizerListener recognizerListener = new RecognizerListener() { 
			public void onBeginOfSpeech() {
				//info.makeText(getApplicationContext(), "��ʼ˵��", 100).show();
				Log.v("qweqweqeqw","qweqweqweqeasdadas");
				System.out.print("???????????");
			}	 
			public void onError(SpeechError error) {
				// Tips��
				// �����룺10118(��û��˵��)��������¼����Ȩ�ޱ�������Ҫ��ʾ�û���Ӧ�õ�¼��Ȩ�ޡ�
				// ���ʹ�ñ��ع��ܣ�����+����Ҫ��ʾ�û���������+��¼��Ȩ�ޡ�
				//info.makeText(getApplicationContext(), error.getPlainDescription(true), 1000).show();
				System.out.print("???????????");
				showTip(error.getPlainDescription(true));
				speak("û��������˵����",false);
				//startSpeenchRecognition();
			} 
			public void onEndOfSpeech() {
				//info.makeText(getApplicationContext(), "����˵��", 100).show();
				System.out.print("66966666");
				showTip("����˵��");
			} 
			public void onResult(RecognizerResult results, boolean isLast) {
				//Log.d("dd", results.getResultString());
				printResult(results, isLast);
				if (isLast) {
					// TODO ���Ľ��
				}
			} 
			public void onVolumeChanged(int volume, byte[] data) {
				showTip("�����mic˵������ǰ������С��" + volume);
				//info.makeText(getApplicationContext(), "��ǰ����˵����������С��" + volume, 100).show();
			} 
			public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			}
		};



	
	/**
	 * ��ʼ����������
	 */
	private InitListener mInitListener = new InitListener() {
	
		 
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("��ʼ��ʧ�ܣ������룺" + code);
			}
		}
	};

	//��ʼ�����������ı������壩��
    private InitListener textUnderstanderListener = new InitListener() {
		public void onInit(int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		//showTip("��ʼ��ʧ��,�����룺"+code);
				Log.d("dd","��ʼ��ʧ��,�����룺"+code);
        	}
		}
    };


	//private SemanticAnalysis semanticAnalysis;//�������ʵ��
	
	
  
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		info=Toast.makeText(this, "", Toast.LENGTH_SHORT);
			/*mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("���ڳ�ʼ�������Ժ򡭡� ^_^");
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();*/
			showTip("��ʼ����...");
			//info.makeText(getApplicationContext(), "��ʼ����...", 5).show();
	       // showTip("hai");
			initIflytek();
			initUI();
			speechRecognition();
			//mProgressDialog.dismiss();
			showTip("��ʼ�����");
			//info.makeText(getApplicationContext(), "��ʼ�����", 5).show();
			player = MediaPlayer.create(MainActivity.this, R.raw.lock);
			player.start();
			speak("��ã�����Sonar�����������������֡�", false);
			//runOnUiThread(new Runnable() {
				//@Override
				//public void run() {
					// TODO Auto-generated method stub
					//xiaoDReaction();//�����Ի�����ϵͳ
				//}
			//});
			
		/*	new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(true)
					xiaoDReaction();
				}});*/
    }
  

    public void getJsonData() {
    			//speak("here",false);
    			
				try {
					JSONObject SAResultJson = new JSONObject(SAResult);
					operation=SAResultJson.optString("operation");
					service=SAResultJson.optString("service");
					semantic=SAResultJson.optJSONObject("semantic");
					answer=SAResultJson.optJSONObject("answer");
					data=SAResultJson.optJSONObject("data");
					
					if(data==null){
					}else result=data.optJSONArray("result");
					
					if(result==null){
					}else{
						//����Ҫ��ʼ�����鲻Ȼ���еò����κν��������
						airQuality=new String[10];
						weatherDate=new String[10];
						wind=new String[10];
						humidity=new String[10];
						windLevel=new String[10];
						weather=new String[10];
						tempRange=new String[10];
						for(int i=1;i<7;i++){
							airQuality[i-1]=result.getJSONObject(i).optString("airQuality");
							weatherDate[i-1]=result.getJSONObject(i).optString("date");
							wind[i-1]=result.getJSONObject(i).optString("wind");
							humidity[i-1]=result.getJSONObject(i).optString("humidity");
							windLevel[i-1]=result.getJSONObject(i).optString("windLevel");
							weather[i-1]=result.getJSONObject(i).optString("weather");
							tempRange[i-1]=result.getJSONObject(i).optString("tempRange");
							sourceName=result.getJSONObject(i).optString("sourceName");
						}
						
					}
					
					if(answer==null){
					}else text=answer.optString("text");
					
					if(semantic==null){
					}else slots=semantic.optJSONObject("slots");
					
					if(slots==null){	
					}else{
						receiver=slots.optString("receiver");
						location=slots.optJSONObject("location");
						name = slots.optString("name");
						price= slots.optString("price");
						code = slots.optString("code");
						song = slots.optString("song");
						keywords=slots.optString("keywords");
						content=slots.optString("content");
						url=slots.optString("url");
						target=slots.optString("target");
						source=slots.optString("source");
					}
					
					if(location==null){
					}else{
						city=location.optString("city");
					}
					
				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					speak("����json����������",false);
					e.printStackTrace();
				}
				xiaoDReaction();
}
    public void xiaoDReaction(){
		SRResult=null;//�ÿ�
		SAResult=null;
		//speak("service:"+service+" operation:"+operation,false);
		//speak("serviceFlag",serviceFlag);
	if(serviceFlag==false){//�������һ������вŽ��з�����ж�
		//speak("�жϷ�������",false);
		switch(service){
		
		
		case "telephone":{//1 �绰��ط���
			
			switch(operation){
			
				case "CALL":{	//��绰
								//��Ҫ�������绰����code��
								//��ѡ����������name��������category�������������location������Ӫ��operator�����Ŷ�head_num����β��tail_num��
								//���ɶ����ѡ����ȷ����Ҫ����
					//speak("name:"+name+"code:"+code,false);
					CallAction callAction=new CallAction(name,code,this);//Ŀǰ�ɸ������ֻ�绰���벦��绰
					callAction.start();
					break;
				}
				
				case "VIEW":{	//�鿴�绰�����¼
								//��Ҫ������
								//��ѡ������δ�ӵ绰�����Ѳ��绰�����ѽӵ绰��
					CallView callview =new CallView(this);
					callview.start();
					break;
				}
				
				default :break;
			
			}
			
			break;
		}
		
		case "message":{//2 ������ط���
			
			switch(operation){
			
				case "SEND":{//���Ͷ���
				
					SendMessage sendMessage = new SendMessage(name,code,content,MainActivity.this);
					sendMessage.start();
					break;
				}
				
				case "VIEW":{//�鿴���Ͷ���ҳ��
				
					MessageView messageView=new MessageView(this);
					messageView.start();
					break;
				}
				
				
				
				case "SENDCONTACTS":{//������Ƭ,Ŀǰֻ��ʶ�����ַ�������
					SendContacts sendContacts = new SendContacts(name,receiver,this);
					sendContacts.start();
					break;
				}
				default :break;
			}
	
			break;
		}
		
		case "app":{//3 Ӧ����ط���
			
			switch(operation){
				
				case "LAUNCH":{//��Ӧ��
					OpenAppAction openApp = new OpenAppAction(name,MainActivity.this);
					openApp.start();
					break;
				}
				
				case "QUERY":{//Ӧ����������Ӧ��
					SearchApp searchApp = new SearchApp(price,name,this);
					searchApp.start();
					break;
				}
				
				default:break;
			
			}
			break;
		}
		
		case "website":{//4 ��վ��ط���
			
			switch(operation){
			
				case "OPEN":{//��ָ����ַ
				
					OpenWebsite openWebsite=new OpenWebsite(url,name,this);
					openWebsite.start();
					break;
				}
			
				default:break;
			}
			
			break;
		}
		
		case "websearch":{//5 ������ط���
			
			switch(operation){
			
				case "QUERY":{//����
					
					SearchAction searchAction =new SearchAction(keywords,MainActivity.this);
					searchAction.Search();
					break;
				}
				
				default:break;
			
			}
					
			
			break;
		}
		
		case "faq":{//6 �����ʴ���ط���
			
			switch(operation){
			
			case "ANSWER":{//�����ʴ�
				
				OpenQA openQA = new OpenQA(text,this);
				openQA.start();
				
				break;
			}
			
			default:break;
			}
			
			break;
		}
		
		case "chat":{//7 ������ط���
			
			switch(operation){
			
			case "ANSWER":{//����ģʽ
				
				OpenQA openQA = new OpenQA(text,this);
				openQA.start();
				
				break;
			}
			
			default:break;
			}
			
			break;
		}
		
			case "openQA":{//8 �����ʴ���ط���
						
						switch(operation){
						
						case "ANSWER":{//�����ʴ�
							
							OpenQA openQA = new OpenQA(text,this);
							openQA.start();
							
							break;
						}
						
						default:break;
						}
						
						break;
			}
			
			case "baike":{//9 �ٿ�֪ʶ��ط���
				
				switch(operation){
				
				case "ANSWER":{//�ٿ�
					
					OpenQA openQA = new OpenQA(text,this);
					openQA.start();
					
					break;
				}
				
				default:break;
				}
				
				break;
			}
			
			case "schedule":{//10 �ճ���ط���
				
				switch(operation){
				
				case "CREATE":{//�����ճ�/����(ֱ����ת��Ӧ���ý���)
					
					ScheduleCreate scheduleCreate=new ScheduleCreate(name,time,date,content,this);
					scheduleCreate.start();
					
					break;
				}
				
				case "VIEW":{//�鿴����/����(δʵ��)
					
					ScheduleView scheduleView = new ScheduleView(name,time,date,content,this);
					scheduleView.start();
					break;
				}
				
				
				default:break;
				}
				
				break;
			}
		
			case "weather":{//11 ������ط���
				
				switch(operation){
				
				case "QUERY":{//��ѯ����
					
					SearchWeather searchWeather= new SearchWeather(city,sourceName,weatherDate,weather,tempRange,airQuality,wind,humidity,windLevel,this);
					searchWeather.start();
					
					break;
				}
				
				default:
					break;
				
				}
				
				break;
			}
			
			case "translation":{//12 ������ط���
				
				switch(operation){
				
				case "TRANSLATION":{//����
					
					Translation translation=new Translation(target,source,content,this);
					translation.start();
					
					break;
				}
				
				default:
					break;
				
				}
				
				break;
			}
			
		default:{
			try {
				JSONObject JSONNews = new JSONObject(SaveResult);
				String FunctionText = JSONNews.getString("text");
				String[] Function = getParagraph(FunctionText);
				WifiManager wifiManager = (WifiManager) super
						.getSystemService(Context.WIFI_SERVICE);

				for(int i = 0; i < Function.length;i++){
					if((i + 1) < Function.length){
						String GetNews = Function[i] + Function[i + 1];
//						if(GetNews.equals("����")){
						switch (GetNews) {
							case "����":

							for (int n = 0; n < Function.length; n++) {
								if ((n + 1) < Function.length) {
									String keyword = Function[n] + Function[n + 1];
									Log.v("keyword", keyword);
									switch (keyword) {
										case "���":
//											String httpUrl = "http://apis.baidu.com/txapi/social/social";
											Log.v("1", "1");
											Jump("http://apis.baidu.com/txapi/social/social");
											break;
										case "����":
//											String httpUrl ="http://apis.baidu.com/txapi/world/world";
											Log.v("2", "2");
											Jump("http://apis.baidu.com/txapi/world/world");
											break;
										case "����":
//											String httpUrl ="http://apis.baidu.com/txapi/world/world";
											Log.v("4", "4");
											Jump(" http://apis.baidu.com/txapi/tiyu/tiyu");
											break;
										default:
											Log.v("12", "12");
											break;
									}
								}
							}
								break;
							case "����":
								for (int n = 0; n < Function.length; n++) {
									if ((n + 1) < Function.length) {
										String keyword = Function[n] + Function[n + 1];
										int BrightnessNow = screenBrightness_check();
										switch (keyword){
											case "����":
												int increase = BrightnessNow + 52;
												setScreenBritness(increase);
												break;
											case "����":
												int lower = BrightnessNow - 52;
												setScreenBritness(lower);
												break;
										}
									}
								}
								break;
							case "����":

								for (int n = 0; n < Function.length; n++) {
									if ((n + 1) < Function.length) {
										String keyword = Function[n] + Function[n + 1];
										switch (keyword){
											case "ý��":
												for (int m = 0; m < Function.length; m++) {

													if ((m + 1) < Function.length) {
														String keywordType = Function[m] + Function[m + 1];
														if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_MUSIC,
																	AudioManager.ADJUST_RAISE,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}else if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_MUSIC,
																	AudioManager.ADJUST_LOWER,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}
													}
												}
												break;
											case "��ʾ":
												for (int m = 0; m < Function.length; m++) {
													if ((m + 1) < Function.length) {
														String keywordType = Function[m] + Function[m + 1];
														if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_ALARM,
																	AudioManager.ADJUST_RAISE,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}else if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_ALARM,
																	AudioManager.ADJUST_LOWER,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}
													}
												}

												break;
											case "����":
												for (int m = 0; m < Function.length; m++) {
													if ((m + 1) < Function.length) {
														String keywordType = Function[m] + Function[m + 1];
														if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_RING,
																	AudioManager.ADJUST_RAISE,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}else if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_RING,
																	AudioManager.ADJUST_LOWER,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}
													}
												}
												break;
											case "ͨ��":
												for (int m = 0; m < Function.length; m++) {
													if ((m + 1) < Function.length) {
														String keywordType = Function[m] + Function[m + 1];
														if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_VOICE_CALL,
																	AudioManager.ADJUST_RAISE,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}else if(keywordType.equals("����")){
															ChangeVolume(
																	AudioManager.STREAM_VOICE_CALL,
																	AudioManager.ADJUST_LOWER,
																	AudioManager.FX_FOCUS_NAVIGATION_UP);
														}
													}
												}
												break;
										}
									}
								}
								break;
							case "��":
								for (int n = 0; n < Function.length; n++) {
									if ((n + 3) < Function.length) {
										String keyword = Function[n] + Function[n + 1] + Function[n + 2] + Function[n + 3];
										if(keyword.equals("wifi")) {
											wifiManager.setWifiEnabled(true);
											Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
											startActivity(intent);
										}
									}
								}
								break;
							case "�ر�":
								for (int n = 0; n < Function.length; n++) {
									if ((n + 3) < Function.length) {
										String keyword = Function[n] + Function[n + 1] + Function[n + 2] + Function[n + 3];
										if(keyword.equals("wifi")){
											Log.v("keyword","in");
											wifiManager.setWifiEnabled(false);
										}
									}
								}
								break;
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
	}
}//����ĳ����������
    }
				
    	//});
			
			
			
	
		/*	if(operation.equals("LAUNCH")){//��Ӧ��
				speak("�õģ�Ϊ������"+name+"...",false);
				OpenAppAction openApp = new OpenAppAction(name,MainActivity.this);
				openApp.runApp();
			}
			if(operation.equals("PLAY")){//�������ֻ���Ƶ
				speak("����֪����ô��...",false);
				if(service.equals("music")){
					PlayAction playAction= new PlayAction(song,MainActivity.this);
					playAction.Play();
				}
				if(service.equals("video")){
					PlayAction playAction= new PlayAction(keywords,MainActivity.this);
					playAction.Play();
				}
			}
			if(operation.equals("QUERY")){//����
				speak("�õģ���������"+keywords+"...",false);
				SearchAction searchAction =new SearchAction(keywords,MainActivity.this);
				searchAction.Search();
			}*/
			
		
	//}

	// ת��
	public String[] getParagraph(String inputString) {
		char[] temp = new char[inputString.length()];
		temp = inputString.toCharArray();
		String[] paragraph = new String[temp.length];
		for (int i = 0; i < inputString.length(); i++) {
			paragraph[i] = String.valueOf(temp[i]);
		}
		return paragraph;
	}


	//������ҳ��
	public  void Jump(String Url){
		Bundle bundle = new Bundle();
		bundle.putSerializable("Url",Url);
		Intent intent = new Intent(MainActivity.this ,NewsActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}


	public void ChangeVolume(int streamType,int direction,int flages){
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.adjustStreamVolume(streamType, direction, flages);
	}

	public int screenBrightness_check() {
		// �ȹر�ϵͳ�������Զ�����

		try {
			if (android.provider.Settings.System.getInt(getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
				android.provider.Settings.System
						.putInt(getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			}
		} catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}

		// ��ȡ��ǰ����,��ȡʧ���򷵻�255
		int intScreenBrightness = (int) (android.provider.Settings.System
				.getInt(getContentResolver(),
						android.provider.Settings.System.SCREEN_BRIGHTNESS, 255));
		String stringScreenBrightness = String.valueOf(intScreenBrightness);
		Log.d("test", stringScreenBrightness);
		return intScreenBrightness;
	}

	// ��Ļ����
	public void setScreenBritness(int brightness) {
		String StringBrightness = String.valueOf(brightness);
		Log.d("BrightnessBefore", StringBrightness);
		// ������Ļȫ��
		if (brightness <= 1) {
			brightness = 1;
		} else if (brightness >= 255) {
			brightness = 255;
		}
		// ���õ�ǰactivity����Ļ����
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		// 0��1,�������Ȱ���ȫ��
		lp.screenBrightness = Float.valueOf(brightness / 255f);
		this.getWindow().setAttributes(lp);
		Log.d("BrightnessAfter", StringBrightness);

		// ����Ϊϵͳ���ȷ���1
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

	}






    public void initIflytek(){//��ʼѶ������
    	
    	//�ҵ�Siri����
    	findViewById(R.id.voice_input).setOnClickListener(MainActivity.this);
    	//�����û��������ö����ſ���ʹ���������񣬽����ڳ�����ڴ����á�����appid��Ҫ�Լ�ȥ�ƴ�Ѷ����վ���룬����ʹ��Ĭ�ϵĽ�����ҵ��;��
    	SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=57120c0f");
    
    }
    
    public void initUI(){//��ʼ��UI�Ͳ���
    	SRResult="";
    	list = new ArrayList<SiriListItem>();
		mAdapter = new ChatMsgViewAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setFastScrollEnabled(true);
		registerForContextMenu(mListView);
    }
    
    public void speechRecognition(){//��ʼ��
    
    	//1.����SpeechRecognizer���󣬵ڶ��������� ������дʱ��InitListener
    	mIat= SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
    	// ��ʼ����дDialog�����ֻʹ����UI��д���ܣ����贴��SpeechRecognizer
    	mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
    	//���������ʼ��
    	mTextUnderstander = TextUnderstander.createTextUnderstander(MainActivity.this, textUnderstanderListener);
    	
    	// ��ʼ���ϳɶ���
    			mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
    }
    
    public void startSpeenchRecognition(){//����ʶ��
    	player = MediaPlayer.create(MainActivity.this, R.raw.begin);
		player.start();
    	// ��ʾ��д�Ի���
    	mIatDialog.setListener(recognizerDialogListener);
		//mIatDialog.show();
		ret = mIat.startListening(recognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			Log.d(TAG, ""+ret);
			showTip("��дʧ��,�����룺" + ret);
			//info.makeText(getApplicationContext(), "��дʧ��,�����룺" + ret, 100).show();
		}
		
    }
    
    //����ʶ����������
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results,isLast);//�õ�ʶ���� 
		}

		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
			speak(error.getPlainDescription(true),true);
			info.makeText(getApplicationContext(), "error.getPlainDescription(true)", 1000).show();
			//showTip(error.getPlainDescription(true));
		}

	};
	
    
    
   /* //��ʼ����ʶ��
    private void startSemanticAnalysis(){
    	//semanticAnalysis=new SemanticAnalysis();
    	//SAResult=semanticAnalysis.getSAResult("����������");//��ʼ�������
    	//UnderstanderDemo testSA=new UnderstanderDemo();
    	//SAResult=testSA.startSA("����������");
    	
    	
    	
    	Intent SAActivity = new Intent(MainActivity.this,SemanticAnalysis.class);
    	SAActivity.putExtra("SRResult", SRResult);
    	Log.d("dd","ʶ������"+SRResult);
    	startActivityForResult(SAActivity,0 );
    
    	//onActivityResult(0, 0, SAActivity);
    	
    	Intent SAActivity = new Intent(MainActivity.this,SemanticAnalysis.class);
    	startActivity(SAActivity);
    	
    	SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
    	SAResult=semanticAnalysis.SAResult;
    	speak(SAResult, false);
    	
    	
    	// SRResult=MainActivity.SRResult;
 	
 		//Log.d("dd","SRResult:"+SRResult);
 		ret=0 ;
 		
 		mTextUnderstander = TextUnderstander.createTextUnderstander(MainActivity.this, textUnderstanderListener);
 		
 		startAnalysis();
    }*/
    
  //��ʼ�������
  	private void startAnalysis(){


  		mTextUnderstander.setParameter(SpeechConstant.DOMAIN,  "iat");
  		if(mTextUnderstander.isUnderstanding()){
  			mTextUnderstander.cancel();
  			//showTip("ȡ��");
  			Log.d("dd","ȡ��");
  		}else {
  			//SRResult="�����������";

  			ret = mTextUnderstander.understandText(SRResult, textListener);
			Log.v("SRResult",SRResult);
  			if(ret != 0)
  			{
  				//showTip("�������ʧ��,������:"+ ret);
  				Log.d("dd","�������ʧ��,������:"+ ret);
  			}
  		}
  		/*ret = mTextUnderstander.understandText(SRResult, textListener);
  		if(ret != 0)
  		{
  			showTip("�������ʧ��,������:"+ ret);
  			
  		}*/
  	}
  	 //ʶ��ص�
      private TextUnderstanderListener textListener = new TextUnderstanderListener() {

  		public void onResult(final UnderstanderResult result) {
  	       	runOnUiThread(new Runnable() {

  					public void run() {
						Log.v("textListener","in");
  						if (null != result) {
  			            	// ��ʾ
  							//Log.d(TAG, "understander result��" + result.getResultString());
  							String text = result.getResultString();
  							SAResult=text;
							SaveResult = SAResult;
  							Log.d("SAResult","SAResult:"+SAResult);
  							/*CreateTXT createTXT=new CreateTXT();
  							File SAResultTXT = new File("SAResultTXT.txt");
  							try {
								createTXT.createFile(SAResultTXT);
								createTXT.writeTxtFile(SAResult, SAResultTXT);
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
  							
  							if (TextUtils.isEmpty(text)) {
  								//Log.d("dd", "understander result:null");
  								//showTip("ʶ��������ȷ��");
  							}
  							//mainActivity.speak();
  							//speak(SAResult,false);
  							getJsonData();
  							//finish();
  			            }
  					}

					

					/*private void dialogueManagement(int mainServiceID,int branchServiceID) {//�Ի�������
						// TODO Auto-generated method stub
						if(mainServiceID==1){
							if(branchServiceID==1){//�����˴�绰���񣬱�Ҫ�����ǡ��绰���롿,��ѡ�����С���������ء�����Ӫ�̡����ŶΡ���β�š���
								//���ɶ����ѡ����ȷ����Ҫ����
								
							}
							if(branchServiceID==2){//�����˲鿴�绰���ż�¼
								
							}
							
						}
						if(mainServiceID==2){//�����˷����ŷ��񣬱�Ҫ�����ǵ绰����Ͷ�������
							
						}
					}*/
  				});
  		}
  		
  		public void onError(SpeechError error) {
  			//showTip("onError Code��"	+ error.getErrorCode());
  			Log.d("dd","onError Code��"	+ error.getErrorCode());
  		}
  	};
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){//��дonActivityResult
        if(requestCode == 0){
            //System.out.println("REQUESTCODE equal");
            if(resultCode == 0){
                 //    System.out.println("RESULTCODE equal");
            	SAResult = data.getStringExtra("SRResult");
            }
        }
    }
    
   
    
    private void printResult(RecognizerResult results,boolean isLast) {
		String text = JsonParser.parseIatResult(results.getResultString());

		//Log.d("dd","text:"+text);
		String sn = null;
		// ��ȡjson����е�sn�ֶ�
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			Log.d("dd","json:"+results.getResultString());

			sn = resultJson.optString("sn");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
			Log.d("????", "123132131");
		}
		SRResult=resultBuffer.toString();
		if(isLast==true){
		speak(SRResult, true);
		//�������ݿ�
		AddLike addLike=new AddLike(SRResult,this);
		addLike.start();
		
		startAnalysis();
		/*startSemanticAnalysis();*/
		}
	}
    
    int ret = 0; // �������÷���ֵ
    
	@SuppressWarnings("static-access")
	@Override
	public void onClick(View view) {//����ʶ�����

		startSpeenchRecognition();

		//GetLocation getLocation =new GetLocation(this);
		//getLocation.start();
		/*Thread mThreadTest= new Thread(){
			public void run(){
				try {
					final NewsService test=new NewsService();
					//speak("hi",false);
					Log.d("dd","ok"+test.start());
				} catch (Exception e) {
					Log.d("dd",e.toString());
					//speak(e.toString(),false);
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			};
			mThreadTest.start();*/
		//startSemanticAnalysis();



		//���´��벻��ֱ�ӷ��������Ȼ����������
		/*info.makeText(getApplicationContext(), "5", 1000).show();
		// TODO Auto-generated method stub
		if(view.getId()==R.id.voice_input){
			//3.��ʼ��д

			setParam();
			info.makeText(getApplicationContext(), "��ʼ��д", 1000).show();
				// ����ʾ��д�Ի���
				ret = mIat.startListening(recognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					Log.d(TAG, ""+ret);
					//showTip("��дʧ��,�����룺" + ret);
				} else {
					//showTip("�ɹ�");
				}
			}*/

	}

	public void setParam(){
		// ��ղ���
				mIat.setParameter(SpeechConstant.PARAMS, null);

				// ������д����
				mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// ���÷��ؽ����ʽ
				mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

				String lag = mSharedPreferences.getString("iat_language_preference",
						"mandarin");
				if (lag.equals("en_us")) {
					// ��������
					mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
				} else {
					// ��������
					mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
					// ������������
					mIat.setParameter(SpeechConstant.ACCENT, lag);
				}
				// ��������ǰ�˵�
				mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
				// ����������˵�
				mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
				// ���ñ�����
				mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
				// ������Ƶ����·��
				mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()
						+ "/iflytek/wavaudio.pcm");
				// ������д����Ƿ�����̬������Ϊ��1��������д�����ж�̬�����ط��ؽ��������ֻ����д����֮�󷵻����ս��
				// ע���ò�����ʱֻ��������д��Ч
				mIat.setParameter(SpeechConstant.ASR_DWA, mSharedPreferences.getString("iat_dwa_preference", "0"));
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * ��ʼ��������
	 */
	private InitListener mTtsInitListener = new InitListener() {
		
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("��ʼ��ʧ��,�����룺"+code);
        	} else {
				// ��ʼ���ɹ���֮����Ե���startSpeaking����
        		// ע���еĿ�������onCreate�����д�����ϳɶ���֮�����Ͼ͵���startSpeaking���кϳɣ�
        		// ��ȷ�������ǽ�onCreate�е�startSpeaking������������
			}		
		}
	};
	
	/**
	 * ��������
	 * @param param
	 * @return 
	 */
	private void setParamTTS(){
		// ��ղ���
		mTts.setParameter(SpeechConstant.PARAMS, null);
		//���úϳ�
		if(mEngineTypeTTS.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			//���÷�����
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
			//��������
			//mTts.setParameter(SpeechConstant.SPEED,mSharedPreferencesTTS.getString("speed_preference", "50"));
			//��������
			//mTts.setParameter(SpeechConstant.PITCH,mSharedPreferencesTTS.getString("pitch_preference", "50"));
			//��������
			//mTts.setParameter(SpeechConstant.VOLUME,mSharedPreferencesTTS.getString("volume_preference", "50"));
			//���ò�������Ƶ������
			//mTts.setParameter(SpeechConstant.STREAM_TYPE,mSharedPreferencesTTS.getString("stream_preference", "3"));
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			//���÷����� voicerΪ��Ĭ��ͨ������+����ָ�������ˡ�
			mTts.setParameter(SpeechConstant.VOICE_NAME,"");
		}
	}
	
	/**
	 * �ϳɻص�������
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		public void onSpeakBegin() {
			showTip("��ʼ����");
		}

		
		public void onSpeakPaused() {
			showTip("��ͣ����");
		}

		
		public void onSpeakResumed() {
			showTip("��������");
		}

		
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// �ϳɽ���
			//mPercentForBuffering = percent;
			//showTip(String.format(getString(R.string.tts_toast_format),
				//	mPercentForBuffering, mPercentForPlaying));
		}

		
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// ���Ž���
			//mPercentForPlaying = percent;
			//showTip(String.format(getString(R.string.tts_toast_format),
				//	mPercentForBuffering, mPercentForPlaying));
		}

		
		public void onCompleted(SpeechError error) {
			if (error == null) {
				showTip("�������");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			
		}
	};
	
	private void textToSpeach(String text){//�����ϳ�
		
		// ���ò���
		setParamTTS();
		int code = mTts.startSpeaking(text, mTtsListener);
		Log.v("code",text);
		if (code != ErrorCode.SUCCESS) {
				showTip("�����ϳ�ʧ��,������: " + code);	
		}
	}
	
	
    //from SiriCN
	public void speak(String msg, boolean isSiri) {//
		//info.makeText(getApplicationContext(), "here", 1000).show();

		Log.v("asdasdadsad",String.valueOf(isSiri));
		if(isSiri==false){
			textToSpeach(msg);
		}
		//if(isHasTTS)
		//mSiriEngine.SiriSpeak(msg);
	}
	
	private void addToList(String msg, boolean isSiri) {
		//
		list.add(new SiriListItem(msg, isSiri));
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(list.size() - 1);
	}
    
	public class SiriListItem {
		String message;
		boolean isSiri;

		public SiriListItem(String msg, boolean siri) {
			message = msg;
			isSiri = siri;
		}
	}
	
	private void showTip(final String str) {
		info.setText(str);
		info.show();
	}
	
	
}
