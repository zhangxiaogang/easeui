/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMConversation.EMConversationType;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.HanziToPinyin;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

public class EaseCommonUtils {
	private static final String TAG = "CommonUtils";
	/**
	 * check if network avalable
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
			}
		}

		return false;
	}

	/**
	 * check if sdcard exist
	 * 
	 * @return
	 */
	public static boolean isSdcardExist() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static EMMessage createExpressionMessage(String toChatUsername, String expressioName, String identityCode){
	    EMMessage message = EMMessage.createTxtSendMessage("["+expressioName+"]", toChatUsername);
        if(identityCode != null){
            message.setAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
        }
        message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        return message;
	}

	/**
     * Get digest according message type and content
     * 
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
        case LOCATION:
            if (message.direct() == EMMessage.Direct.RECEIVE) {
                digest = getString(context, R.string.location_recv);
                EaseUserProfileProvider userProvider = EaseIM.getInstance().getUserProvider();
                String from = message.getFrom();
                if(userProvider != null && userProvider.getUser(from) != null) {
                    EaseUser user = userProvider.getUser(from);
                    if(user != null && !TextUtils.isEmpty(user.getNickname())) {
                        from = user.getNickname();
                    }
                }
                digest = String.format(digest, from);
                return digest;
            } else {
                digest = getString(context, R.string.location_prefix);
            }
            break;
        case IMAGE:
            digest = getString(context, R.string.picture);
            break;
        case VOICE:
            digest = getString(context, R.string.voice_prefix);
            break;
        case VIDEO:
            digest = getString(context, R.string.video);
            break;
        case CUSTOM:
            digest = getString(context, R.string.custom);
            break;
        case TXT:
            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            if(txtBody != null){
                if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
                    digest = getString(context, R.string.voice_call) + txtBody.getMessage();
                }else if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)){
                    digest = getString(context, R.string.video_call) + txtBody.getMessage();
                }else if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                    if(!TextUtils.isEmpty(txtBody.getMessage())){
                        digest = txtBody.getMessage();
                    }else{
                        digest = getString(context, R.string.dynamic_expression);
                    }
                }else{
                    digest = txtBody.getMessage();
                }
            }
            break;
        case FILE:
            digest = getString(context, R.string.file);
            break;
        default:
            EMLog.e(TAG, "error, unknow type");
            return "";
        }
        Log.e("TAG", "message text = "+digest);
        return digest;
    }
    
    static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }
	
	/**
	 * get top context
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}
	
	/**
     * set initial letter of according user's nickname( username if no nickname)
     * 
     * @param user
     */
    public static void setUserInitialLetter(EaseUser user) {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;
        
        final class GetInitialLetter {
            String getLetter(String name) {
                if (TextUtils.isEmpty(name)) {
                    return DefaultLetter;
                }
                char char0 = name.toLowerCase().charAt(0);
                if (Character.isDigit(char0)) {
                    return DefaultLetter;
                }
                ArrayList<HanziToPinyin.Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));
                if (l != null && l.size() > 0 && l.get(0).target.length() > 0)
                {
                    HanziToPinyin.Token token = l.get(0);
                    String letter = token.target.substring(0, 1).toUpperCase();
                    char c = letter.charAt(0);
                    if (c < 'A' || c > 'Z') {
                        return DefaultLetter;
                    }
                    return letter;
                }
                return DefaultLetter;
            }
        }
        
        if ( !TextUtils.isEmpty(user.getNickname()) ) {
            letter = new GetInitialLetter().getLetter(user.getNickname());
            user.setInitialLetter(letter);
            return;
        } 
        if (letter.equals(DefaultLetter) && !TextUtils.isEmpty(user.getUsername())) {
            letter = new GetInitialLetter().getLetter(user.getUsername());
        }
        user.setInitialLetter(letter);
    }
    
    /**
     * change the chat type to EMConversationType
     * @param chatType
     * @return
     */
    public static EMConversationType getConversationType(int chatType) {
        if (chatType == EaseConstant.CHATTYPE_SINGLE) {
            return EMConversationType.Chat;
        } else if (chatType == EaseConstant.CHATTYPE_GROUP) {
            return EMConversationType.GroupChat;
        } else {
            return EMConversationType.ChatRoom;
        }
    }

    /**
     * get chat type by conversation type
     * @param conversation
     * @return
     */
    public static int getChatType(EMConversation conversation) {
        if(conversation.isGroup()) {
            if(conversation.getType() == EMConversationType.ChatRoom) {
                return EaseConstant.CHATTYPE_CHATROOM;
            }else {
                return EaseConstant.CHATTYPE_GROUP;
            }
        }else {
            return EaseConstant.CHATTYPE_SINGLE;
        }
    }

    /**
     * \~chinese
     * 判断是否是免打扰的消息,如果是app中应该不要给用户提示新消息
     * @param message
     * return
     *
     * \~english
     * check if the message is kind of slient message, if that's it, app should not play tone or vibrate
     *
     * @param message
     * @return
     */
    public static boolean isSilentMessage(EMMessage message){
        return message.getBooleanAttribute("em_ignore_notification", false);
    }

    /**
     * 获取屏幕的基本信息
     * @param context
     * @return
     */
    public static float[] getScreenInfo(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        float[] info = new float[5];
        if(manager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            info[0] = dm.widthPixels;
            info[1] = dm.heightPixels;
            info[2] = dm.densityDpi;
            info[3] = dm.density;
            info[4] = dm.scaledDensity;
        }
        return info;
    }

    /**
     * dip to px
     * @param context
     * @param value
     * @return
     */
    public static float dip2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     * @param context
     * @param value
     * @return
     */
    public static float sp2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }

    /**
     * 判断是否是时间戳
     * @param time
     * @return
     */
    public static boolean isTimestamp(String time) {
        if(TextUtils.isEmpty(time)) {
            return false;
        }
        long timestamp = 0L;
        try {
            timestamp = Long.parseLong(time);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return timestamp > 0;
    }

    /**
     * 获取首字母
     * @param name
     * @return
     */
    public static String getLetter(String name) {
        return new GetInitialLetter().getLetter(name);
    }

    private static class GetInitialLetter {
        private String defaultLetter = "#";

        /**
         * 获取首字母
         * @param name
         * @return
         */
        public String getLetter(String name) {
            if(TextUtils.isEmpty(name)) {
                return defaultLetter;
            }
            char char0 = name.toLowerCase().charAt(0);
            if(Character.isDigit(char0)) {
                return defaultLetter;
            }
            ArrayList<HanziToPinyin.Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));
            if(l != null && !l.isEmpty() && l.get(0).target.length() > 0) {
                HanziToPinyin.Token token = l.get(0);
                String letter = token.target.substring(0, 1).toUpperCase();
                char c = letter.charAt(0);
                if(c < 'A' || c > 'Z') {
                    return defaultLetter;
                }
                return letter;
            }
            return defaultLetter;
        }
    }

}