package com.mindpin.android.filedownloader;

import android.content.Context;

import com.sinosoft.gyicPlat.R;

public class Resources
{
	public static String getLibraryName(Context pContext)
	{
		return pContext.getString(R.string.lib_name);
	}

	public static String getLibraryVersion(Context pContext)
	{
		return pContext.getString(R.string.lib_version);
	}
}
