package com.tonetag.ivr.client;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class L {
	
	public static void e(String tag, String params) {
		StringBuffer stringBuffer = new StringBuffer();
		Date now = new Date();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		simpleDateFormat.format(now, stringBuffer, new FieldPosition(0));
		
		System.out.println(stringBuffer + " : " + tag + " : " + params);
	}

}
