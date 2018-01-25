package com.sinosoft.mobileshop.appwidget;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.fragment.IndexGridViewFragment;
import com.sinosoft.mobileshop.fragment.ManagerListViewFragment;
import com.sinosoft.mobileshop.fragment.MoreViewFragment;
import com.sinosoft.mobileshop.fragment.NewsListViewFragment;

public enum MainTab {

	INDEX(0, R.string.main_tab_name_index, R.drawable.tab_icon_index,
			IndexGridViewFragment.class),

	MANAGER(1, R.string.main_tab_name_manager, R.drawable.tab_icon_manager,
			ManagerListViewFragment.class),

	NEWS(2, R.string.main_tab_name_news, R.drawable.tab_icon_news,
			NewsListViewFragment.class),
	
	MORE(3, R.string.main_tab_name_more, R.drawable.tab_icon_more,
			MoreViewFragment.class);


	private int idx;
	private int resName;
	private int resIcon;
	private Class<?> clz;

	private MainTab(int idx, int resName, int resIcon, Class<?> clz) {
		this.idx = idx;
		this.resName = resName;
		this.resIcon = resIcon;
		this.clz = clz;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getResName() {
		return resName;
	}

	public void setResName(int resName) {
		this.resName = resName;
	}

	public int getResIcon() {
		return resIcon;
	}

	public void setResIcon(int resIcon) {
		this.resIcon = resIcon;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}
	
}
