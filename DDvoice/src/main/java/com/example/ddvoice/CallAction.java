package com.example.ddvoice;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CallAction {
	private String mPerson = null;
    private String number = null;
    MainActivity mActivity;
	public List<String> phoneName = new ArrayList<String>();
	public List<String> phoneId = new ArrayList<String>();
  
    Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 100:
				mPerson=(String)msg.obj;
				start();
	         break;			
			}
			super.handleMessage(msg);
		}
	};
	
	public CallAction(String person,String code,MainActivity activity)
	  {
	    mPerson = person;
	    number=code;
	    mActivity=activity;
	  }
	public void start()//打电话
	  {
		if((number==null)||(number.equals(""))){
			 if ((mPerson == null) || (mPerson.equals("")))
			    {
				
				 //mActivity.speak("至少告诉我名字或者号码吧？", false);
				 Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + ""));
				 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 mActivity.startActivity(intent);
			    }else{
			    	 mPerson=mPerson.trim();
			    	// number=getNumberByName(mPerson,mActivity);
				   String GetPinYinName = getPinYin(mPerson);
				   char[] CharPinYin = mPerson.toCharArray();
				   FuzzySearch(String.valueOf(CharPinYin[0]),mActivity);
				 if (phoneName.size() > 0) {
					 for (int i = 0; i < phoneName.size(); i++) {
						 String GetFuzzyPinYin = getPinYin(phoneName.get(i));
						 if (GetPinYinName.equals(GetFuzzyPinYin) && phoneId.size() > 0) {
							 Long PhoneNum = GainPhoneNum(phoneId.get(i), mActivity);
							 number = String.valueOf(PhoneNum);
							 if(number == null)
							 {
								 mActivity.speak("没有在通讯录中找到"+mPerson+"的号码。", false);
							 }else{
								 //打电话
								 mActivity.speak("即将拨给"+phoneName.get(i)+"...", false);
								 Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + number));
								 mActivity.startActivity(intent);
							 }

						 }
					 }
				 }

			    }
		}
		else{
			mActivity.speak("即将拨给"+number+"...", false);
       	 	Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + number));    
	        mActivity.startActivity(intent);
			 
		}
	 
	  }



	


	public Long GainPhoneNum(String phoneNameId, Context context) {
		int i;
		Long phone = null;
		String phoneNumber = null;
		/*
		 * 查找该联系人的phone信息
		 */
		Cursor phones = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
						+ phoneNameId, null, null);
		int phoneIndex = 0;
		if (phones.getCount() > 0) {
			phoneIndex = phones
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		}
		while (phones.moveToNext()) {
			phoneNumber = phones.getString(phoneIndex);
			Log.i("null", phoneNumber);
		}
		phone = PhoneToNum(phoneNumber);
		return phone;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public Long PhoneToNum(String Num) {
		String No = null;
		char[] temp = new char[Num.length()];
		temp = Num.toCharArray();
		char[] copyPhone = Arrays.copyOf(temp, temp.length);
		String[] copy = new String[copyPhone.length];
		for (int n = 0; n < copyPhone.length; n++) {
			copy[n] = String.valueOf(copyPhone[n]);

			if (!copy[n].endsWith(" ")) {
				if (!copy[n].equals("-")) {

					if (No == null) {
						No = copy[n];
					} else {
						No = No + copy[n];
					}
				}
			}
		}
		return Long.parseLong(No.trim());
	}




	//转化成拼音
	public  String getPinYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
		format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);

		char[] input = inputString.trim().toCharArray();
		StringBuffer output = new StringBuffer("");

		try {
			for (int i = 0; i < input.length; i++) {
				if (Character.toString(input[i]).matches("[\u4E00-\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);
					output.append(temp[0]);
					output.append(" ");
				} else
					output.append(Character.toString(input[i]));
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output.toString();
	}



	public void FuzzySearch(String key,Context context) {;
		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID };
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				ContactsContract.PhoneLookup.DISPLAY_NAME + " LIKE" + " '%"
						+ key + "%'", null, null); // Sort order.
		String[] SearchName = new String[cursor.getCount()];
		if (cursor == null) {
			mActivity.speak("通讯录不存在" + mPerson, false);
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			String uname = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			phoneName.add(uname);
			String number = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
			phoneId.add(number);
		}
		cursor.close();
	}


}
