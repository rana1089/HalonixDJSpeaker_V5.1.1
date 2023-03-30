package com.wansnn.csc.wsbulb;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class BluetoothUtils {


	public static final String SERVICE_UUID = "0000e030-0000-1000-8000-00805f9b34fb";


	public static final String CHARACTER_UUID = "0000e031-0000-1000-8000-00805f9b34fb";

	public static String StringToAddress(String str) {
		StringBuffer sb = new StringBuffer(str.subSequence(0, 2));
		for (int i = 1; i <= 5; i++) {
			sb.append(":");
			sb.append(str.subSequence(i * 2, i * 2 + 2));
		}

		Log.e("BluetoothUtils", "StringToAddress: "+sb.toString().toUpperCase() );
		return sb.toString().toUpperCase();
	}



	public static byte[] hexStr2Bytes(String src)
	{
		int m=0,n=0;
		int l=src.length()/2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++)
		{
			m=i*2+1;
			n=m+1;
			ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
		}
		return ret;
	}


	public static String AddressToString(String address) {
		if (address.indexOf(":") < 0) {
			return address;
		}
		String str = address.replace(":", "").toUpperCase();
		Log.e("BluetoothUtils", "AddressToString: " + str);
		return str;
	}


	public static byte[] hexStr2ByteArray(String hexStr) {

		byte[] b = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			String s = hexStr.substring(i * 2, i * 2 + 2);
			b[i] = (byte) Integer.parseInt(s, 16);
		}
		return b;
	}


	public static String byteArray2HexStr(byte[] bs) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (bs == null || bs.length <= 0) {
			return null;
		}
		for (int i = 0; i < bs.length; i++) {
			int v = bs[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static int byte2hex(byte[] buffer) {
		String h = "";
		for (int i = 0; i < buffer.length; i++) {
			String temp = Integer.toHexString(buffer[i] & 0xFF);
			if (temp.length() == 1) {
				temp = "0" + temp;
			}
			h = h + " " + temp;
		}
		String[] xinghao = h.split(" ");
		int x = ((Integer.parseInt(xinghao[7] + "", 16) * 256) + Integer
				.parseInt(xinghao[6] + "", 16));
		return x;
	}

	public static  int byteToInt(byte b, byte c) {//计算总包长，两个字节表示的
		short s = 0;
		int ret;
		short s0 = (short) (c & 0xff);// 最低位
		short s1 = (short) (b & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		ret = s;
		return ret;
	}

	public static byte[] int2byte(int res) {
		byte[] targets = new byte[2];
		targets[1] = (byte) (res & 0xff);// 最低位
		targets[0] = (byte) ((res >> 8) & 0xff);// 次低位
		return targets;
	}
	
	public static String bytetoString(byte bs) {
		StringBuilder stringBuilder = new StringBuilder();
//		StringBuilder stringBuilder = new StringBuilder("0x");
			int v = bs & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		return stringBuilder.toString();
	}


	public static boolean isBluetoothSupported() {
		return BluetoothAdapter.getDefaultAdapter() != null ? true : false;
	}


	public static boolean isBluetoothEnabled() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter != null) {
			return bluetoothAdapter.isEnabled();
		}

		return false;
	}


	public static boolean enableBluetooth() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter != null) {
			return bluetoothAdapter.enable();
		}

		return false;
	}


	public static boolean disableBluetooth() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter != null) {
			return bluetoothAdapter.disable();
		}

		return false;
	}

	
	
	
	  public static char ascii2Char(int ASCII) {  
	        return (char) ASCII;  
	    }  
	  
	    public static int char2ASCII(char c) {  
	        return (int) c;  
	    }  
	  
	    public static String ascii2String(int[] ASCIIs) {
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < ASCIIs.length; i++) {  
	            sb.append((char) ascii2Char(ASCIIs[i]));  
	        }  
	        return sb.toString();  
	    } 
	    
	    

}
