package com.sinosoft.phoneGapPlugins.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.vpn.VPNAddress;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/** */
/**
 * 支持断点续传的FTP实用类
 * 
 * @version 0.1 实现基本断点上传下载
 * @version 0.2 实现上传下载进度汇报
 * @version 0.3 实现中文目录创建及中文文件创建，添加对于中文的支持
 */
public class FTPContinue {

	// 连接配置文件
	public FTPClient ftpClient = new FTPClient();
	public String ftpHost;// 服务器地址
	public int ftpPort;// 端口号
	private String ftpUserName = Constant.ftpUserName;// 用户名
	private String ftpPassword = Constant.ftpPassword;// 密码
	private Thread mThread;
	public SharedPreferences sp;
	String remote; // 远程文件
	String local; // 本地文件
	private int VPNflag;
	private int mflag;
	// 控件
	private Context mContext;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private Button mButton;
	private long process;
	private Handler mParentHandler;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;
	private static final int DOWN_BREAK = 3;
	private static final int DOWN_NOFILE = 4;
	private static final int DOWN_NOCONNTION = 5;
	private static final int TWODWON_OVER = 6;
	
	private boolean interceptFlag = false; // 中断标记

	public FTPContinue(Context context, ProgressBar progressbar,
			TextView textvie, Button button, Handler handler) {
		mContext = context;
		mProgressBar = progressbar;
		mTextView = textvie;
		mButton = button;
		mParentHandler = handler;
		// 设置将过程中使用到的命令输出到控制台
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
		VPNAddress();
	}

	public FTPContinue(Context context, Handler handler, int flag) {
		mContext = context;
		mParentHandler = handler;
		mflag = flag;
		 VPNAddress();
	}

	/** */
	/**
	 * 连接到FTP服务器
	 * 
	 * @param hostname
	 *            主机名
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return 是否连接成功
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		ftpClient.connect(ftpHost, ftpPort);
		System.out.println("ftpHost:" + ftpHost + "ftpPort:" + ftpPort);
		ftpClient.setControlEncoding("GBK");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(ftpUserName, ftpPassword)) {
				return true;
			}
		}
		disconnect();
		return false;
	}

	/** */
	/**
	 * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
	 * 
	 * @param remote
	 *            远程文件路径
	 * @param local
	 *            本地文件路径
	 * @return 上传的状态
	 * @throws IOException
	 */
	public void download(String strRemote, String strLocal) throws IOException {
		this.remote = strRemote;
		this.local = strLocal;

		mThread = new Thread(mRunnable);
		mThread.start();

	}

	public void cancelDownload() {
		if(ftpClient != null) {
			disconnect();
		}
		if(mThread != null) {
			mThread.stop();
			mThread.interrupt();
		}
	}
	
	// 在进程中执行导出操作
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				// ftp连接
				ftpClient.connect(ftpHost, ftpPort);
				ftpClient.setControlEncoding("GBK");
				boolean isLogin = false;
				if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					if (ftpClient.login(ftpUserName, ftpPassword)) {
						isLogin = true;
					}
				}
				if (!isLogin) {
					disconnect();
					Thread.interrupted();
				}

				// 设置被动模式
				ftpClient.enterLocalPassiveMode();
				// InetAddress host = InetAddress.getByName("127.0.0.1");
				// ftpClient.enterRemoteActiveMode(host, 30023);
				// 主动模式
				// ftpClient.enterLocalActiveMode();
				// 设置以二进制方式传输
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				ftpClient.setRemoteVerificationEnabled(false);
				//ftpClient.pasv();
				// DownloadStatus result;
				// 检查远程文件是否存在
				FTPFile[] files = ftpClient.listFiles(new String(remote
						.getBytes("GBK"), "iso-8859-1"));
				if (files.length != 1) {
					System.out.println("远程文件不存在");
					// return DownloadStatus.Remote_File_Noexist;
					mHandler.sendEmptyMessage(DOWN_NOFILE);

				}

				long lRemoteSize = files[0].getSize();
				File f = new File(local);
				// 本地存在文件，进行断点下载
				if (f.exists()) {
					long localSize = f.length();
					// 判断本地文件大小是否大于远程文件大小
					if (localSize >= lRemoteSize) {
						System.out.println("本地文件大于远程文件，下载中止");
						// return DownloadStatus.Local_Bigger_Remote;

					}

					// 进行断点续传，并记录状态
					FileOutputStream out = new FileOutputStream(f, true);
					ftpClient.setRestartOffset(localSize);
					InputStream in = ftpClient.retrieveFileStream(new String(
							remote.getBytes("GBK"), "iso-8859-1"));
					byte[] bytes = new byte[1024];
					long step = lRemoteSize / 100;
					// process = localSize / step;
					int c;
					while ((c = in.read(bytes)) != -1) {
						if (interceptFlag) {
							break;
						}
						out.write(bytes, 0, c);
						localSize += c;
						long nowProcess = localSize / step;
						if (nowProcess > process) {
							process = nowProcess;
							if (process % 1 == 0)
								System.out.println("下载进度：" + process);
							mHandler.sendEmptyMessage(DOWN_UPDATE);
						}
					}
					in.close();
					out.close();
					ftpClient.logout();
					disconnect();
					if (c <= 0) {
						mHandler.sendEmptyMessage(DOWN_OVER);
					}

					boolean isDo = ftpClient.completePendingCommand();
					if (isDo) {
						// result = DownloadStatus.Download_From_Break_Success;
						mHandler.sendEmptyMessage(DOWN_OVER);
						if(mflag==1){
							mHandler.sendEmptyMessage(TWODWON_OVER);
							}
					} else {
						// result = DownloadStatus.Download_From_Break_Failed;
						mHandler.sendEmptyMessage(DOWN_BREAK);
					}
				} else {
					OutputStream out = new FileOutputStream(f);
					InputStream in = ftpClient.retrieveFileStream(new String(
							remote.getBytes("GBK"), "iso-8859-1"));
					byte[] bytes = new byte[1024];
					long step = lRemoteSize / 100;
					process = 0;
					long localSize = 0L;
					int c;
					while ((c = in.read(bytes)) != -1) {
						if (interceptFlag) {
							break;
						}
						out.write(bytes, 0, c);
						localSize += c;
						long nowProcess = localSize / step;
						if (nowProcess > process) {
							process = nowProcess;
							if (process % 1 == 0)
								System.out.println("下载进度：" + process);
							mHandler.sendEmptyMessage(DOWN_UPDATE);
							// TODO 更新文件下载进度,值存放在process变量中
						}
					}
					in.close();
					out.close();
					ftpClient.logout();
					disconnect();
					if (c <= 0) {
						mHandler.sendEmptyMessage(DOWN_OVER);
						
					}

					boolean isDo = ftpClient.completePendingCommand();
					if (isDo) {
						// result = DownloadStatus.Download_From_Break_Success;
						mHandler.sendEmptyMessage(DOWN_OVER);
						if(mflag==1){
							mHandler.sendEmptyMessage(TWODWON_OVER);
							}
					} else {
						// result = DownloadStatus.Download_From_Break_Failed;
						mHandler.sendEmptyMessage(DOWN_BREAK);
					}
				}
				// return result;
			} catch (IOException e) {		
//				Toast.makeText(mContext, "连接下载失败，稍后再试", Toast.LENGTH_SHORT).show();	
				e.printStackTrace();
			} catch (Exception e) {
//				Toast.makeText(mContext, "服务器异常", Toast.LENGTH_SHORT).show();	
				e.printStackTrace();
			}

		}
	};

	/** */
	/**
	 * 断开与远程服务器的连接
	 * 
	 * @throws IOException
	 */
	public void disconnect() {
		if (ftpClient.isConnected()) {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** */
	/**
	 * 递归创建远程服务器目录
	 * 
	 * @param remote
	 *            远程服务器文件绝对路径
	 * @param ftpClient
	 *            FTPClient 对象
	 * @return 目录创建是否成功
	 * @throws IOException
	 */
	public boolean CreateDirecroty(String remote, FTPClient ftpClient)
			throws IOException {
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(new String(directory
						.getBytes("GBK"), "iso-8859-1"))) {
			// 如果远程目录不存在，则递归创建远程服务器目录
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = new String(remote.substring(start, end)
						.getBytes("GBK"), "iso-8859-1");
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						System.out.println("创建目录失败");
						return false;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有目录是否创建完毕
				if (end <= start) {
					break;
				}
			}
		}
		return true;
	}

	public void BreakDownload() {
		// mHandler.sendEmptyMessage(DOWN_BREAK);
		if (interceptFlag) {
			mButton.setText("取消");
			try {

				// 继续下载
				interceptFlag = false;
				Toast.makeText(mContext, "正在继续下载", Toast.LENGTH_SHORT).show();
				download(remote, local);

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			interceptFlag = true;
		}
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				if (mflag == 1||mflag==2) {

				} else {
					mProgressBar.setProgress((int) process);
					mTextView.setText(process + "/100");
				}

				break;
			case DOWN_OVER:			
					downloadOver();	
				break;
			case DOWN_NOFILE:
				Toast.makeText(mContext, "远程文件不存在", Toast.LENGTH_SHORT).show();
				break;
			case DOWN_BREAK:
				disconnect();
				Toast.makeText(mContext, "下载已经取消", Toast.LENGTH_SHORT).show();
				mButton.setText("继续");

				break;
			case DOWN_NOCONNTION:
				Toast.makeText(mContext, "无法连接到服务器", Toast.LENGTH_SHORT).show();
				break;
			case TWODWON_OVER:	
				mParentHandler.sendEmptyMessage(TWODWON_OVER);	
				break;

			default:
				break;
			}
		};
	};

	protected void downloadOver() {
		mParentHandler.sendEmptyMessage(DOWN_OVER);
	}

	private void VPNAddress() {
		sp = mContext.getSharedPreferences("SP", Context.MODE_PRIVATE);
		VPNflag = sp.getInt("VPNFlag", 1);
		VPNAddress vpnaddress = new VPNAddress(mContext);
		if (VPNflag == 1) {
			VPNAddressBean bean = vpnaddress.queryVPN(1);
			String ftpaddress = bean.getFtp();
			String[] tras1 = ftpaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				ftpHost = tras1[0];
				ftpPort = Integer.parseInt(tras1[1]);
			}
		}
		if (VPNflag == 2) {
			// vpnaddress.queryVPN(2);
			VPNAddressBean bean = vpnaddress.queryVPN(2);
			String ftpaddress = bean.getFtp();
			System.out.println("ftpaddress:" + ftpaddress);
			String[] tras1 = ftpaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				ftpHost = tras1[0];
				ftpPort = Integer.parseInt(tras1[1]);
			}
		}
	}

}
