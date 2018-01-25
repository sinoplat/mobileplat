/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package com.sinosoft.phoneGapPlugins.absFilesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.file.EncodingException;
import org.apache.cordova.file.FileExistsException;
import org.apache.cordova.file.FileHelper;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.file.Filesystem;
import org.apache.cordova.file.Filesystem.ReadFileCallback;
import org.apache.cordova.file.InvalidModificationException;
import org.apache.cordova.file.NoModificationAllowedException;
import org.apache.cordova.file.TypeMismatchException;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.ParseException;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class AbsFilesystem extends CordovaPlugin {
	private static final String LOG_TAG = "absFilesystem";

	public static int NOT_FOUND_ERR = 1;
	public static int SECURITY_ERR = 2;
	public static int ABORT_ERR = 3;

	public static int NOT_READABLE_ERR = 4;
	public static int ENCODING_ERR = 5;
	public static int NO_MODIFICATION_ALLOWED_ERR = 6;
	public static int INVALID_STATE_ERR = 7;
	public static int SYNTAX_ERR = 8;
	public static int INVALID_MODIFICATION_ERR = 9;
	public static int QUOTA_EXCEEDED_ERR = 10;
	public static int TYPE_MISMATCH_ERR = 11;
	public static int PATH_EXISTS_ERR = 12;

	public static int UNKNOWN_ERR = 1000;

	private boolean configured = false;
	//鍥剧墖杞寲鎴恇ase64瀛楃涓� 
    public static String GetImageStr( String imgFile)  
    {//灏嗗浘鐗囨枃浠惰浆鍖栦负瀛楄妭鏁扮粍瀛楃涓诧紝骞跺鍏惰繘琛孊ase64缂栫爜澶勭悊  
        InputStream in = null;  
        byte[] data = null;  
        //璇诲彇鍥剧墖瀛楄妭鏁扮粍  
        try   
        {  
            in = new FileInputStream(imgFile);          
            data = new byte[in.available()];  
            in.read(data);  
            in.close();  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }  
        return null;
    }  
    
	@Override
	public boolean execute(String action, JSONArray args,
			 CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub
		final CallbackContext cb=callbackContext;
		if ("getImgBase64".equals(action)) {
			final String filePath = args.getString(0);
			String data=GetImageStr(filePath);
			System.out.println(data);
			callbackContext.success(data);
			Uri fileUri=Uri.fromFile(new File(filePath));
			Uri.parse("file:///mnt/sdcard/.com.sinosoft.gyicPlat/imageCache/http.jpg");
			return true;
		}

		return false;
	}
	public boolean execute2(String action, JSONArray args,
			 CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub
		final CallbackContext cb=callbackContext;
		if ("getImgBase64".equals(action)) {
			final String filePath = args.getString(0);
			File file = new File(filePath);
			if (!file.isFile()) {
				callbackContext.success("null");
			}
			final int length=Integer.parseInt(String.valueOf(file.length())) ;

					try {
						readFileAs(filePath, 0, length, cb, null, -1);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		

		}

		return true;
	}
	/*public String readFileToBase64(File file)
	{
		
	}*/
    /**
     * Read the contents of a file.
     * This is done in a background thread; the result is sent to the callback.
     *
     * @param filename          The name of the file.
     * @param start             Start position in the file.
     * @param end               End position to stop at (exclusive).
     * @param callbackContext   The context through which to send the result.
     * @param encoding          The encoding to return contents as.  Typical value is UTF-8. (see http://www.iana.org/assignments/character-sets)
     * @param resultType        The desired type of data to send to the callback.
     * @return                  Contents of file.
     * @throws MalformedURLException 
     */
    public void readFileAs(final String srcURLstr, final int start, final int end, final CallbackContext callbackContext, final String encoding, final int resultType) throws MalformedURLException {
        try {
        
      
        
            readFileAtURL(srcURLstr, start, end, new Filesystem.ReadFileCallback() {
                public void handleData(InputStream inputStream, String contentType) {
            		try {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        final int BUFFER_SIZE = 8192;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        
                        for (;;) {
                            int bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE);
                            
                            if (bytesRead <= 0) {
                                break;
                            }
                            os.write(buffer, 0, bytesRead);
                        }
                                
            			PluginResult result;
            			switch (resultType) {
            			case PluginResult.MESSAGE_TYPE_STRING:
                            result = new PluginResult(PluginResult.Status.OK, os.toString(encoding));
            				break;
            			case PluginResult.MESSAGE_TYPE_ARRAYBUFFER:
                            result = new PluginResult(PluginResult.Status.OK, os.toByteArray());
            				break;
            			case PluginResult.MESSAGE_TYPE_BINARYSTRING:
                            result = new PluginResult(PluginResult.Status.OK, os.toByteArray(), true);
            				break;
            			default: // Base64.
                        byte[] base64 = Base64.encode(os.toByteArray(), Base64.NO_WRAP);
            			String s = "data:" + contentType + ";base64," + new String(base64, "US-ASCII");
            			System.out.println(s);
            			result = new PluginResult(PluginResult.Status.OK, s);
            			}

            			callbackContext.sendPluginResult(result);
            		} catch (IOException e) {
            			Log.d(LOG_TAG, e.getLocalizedMessage());
            			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, NOT_READABLE_ERR));
                    }
            	}
            });


        } catch (IllegalArgumentException e) {
        	throw new MalformedURLException("Unrecognized filesystem URL");
        } catch (FileNotFoundException e) {
        	callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, NOT_FOUND_ERR));
        } catch (IOException e) {
        	Log.d(LOG_TAG, e.getLocalizedMessage());
        	callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, NOT_READABLE_ERR));
        }
    }
	public void readFileAtURL(String filePath, long start, long end,
			ReadFileCallback readFileCallback) throws IOException {

		File file = new File(filePath);
        String contentType = FileHelper.getMimeTypeForExtension(file.getAbsolutePath());
		
        if (end < 0) {
            end = file.length();
        }
        long numBytesToRead = end - start;

        InputStream rawInputStream = new FileInputStream(file);
		try {
			if (start > 0) {
                rawInputStream.skip(start);
			}
            LimitedInputStream inputStream = new LimitedInputStream(rawInputStream, numBytesToRead);
            readFileCallback.handleData(inputStream, contentType);
		} finally {
            rawInputStream.close();
		}
	}
	   protected class LimitedInputStream extends FilterInputStream {
	        long numBytesToRead;
	        public LimitedInputStream(InputStream in, long numBytesToRead) {
	            super(in);
	            this.numBytesToRead = numBytesToRead;
	        }
	        @Override
	        public int read() throws IOException {
	            if (numBytesToRead <= 0) {
	                return -1;
	            }
	            numBytesToRead--;
	            return in.read();
	        }
	        @Override
	        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
	            if (numBytesToRead <= 0) {
	                return -1;
	            }
	            int bytesToRead = byteCount;
	            if (byteCount > numBytesToRead) {
	                bytesToRead = (int)numBytesToRead; // Cast okay; long is less than int here.
	            }
	            int numBytesRead = in.read(buffer, byteOffset, bytesToRead);
	            numBytesToRead -= numBytesRead;
	            return numBytesRead;
	        }
	    }


	/*
	 * helper to execute functions async and handle the result codes
	 */
	private void threadhelper(final FileOp f,
			final CallbackContext callbackContext) {

		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					f.run();
				} catch (Exception e) {
					e.printStackTrace();
					if (e instanceof EncodingException) {
						callbackContext.error(FileUtils.ENCODING_ERR);
					} else if (e instanceof FileNotFoundException) {
						callbackContext.error(FileUtils.NOT_FOUND_ERR);
					} else if (e instanceof FileExistsException) {
						callbackContext.error(FileUtils.PATH_EXISTS_ERR);
					} else if (e instanceof NoModificationAllowedException) {
						callbackContext
								.error(FileUtils.NO_MODIFICATION_ALLOWED_ERR);
					} else if (e instanceof InvalidModificationException) {
						callbackContext
								.error(FileUtils.INVALID_MODIFICATION_ERR);
					} else if (e instanceof MalformedURLException) {
						callbackContext.error(FileUtils.ENCODING_ERR);
					} else if (e instanceof IOException) {
						callbackContext
								.error(FileUtils.INVALID_MODIFICATION_ERR);
					} else if (e instanceof EncodingException) {
						callbackContext.error(FileUtils.ENCODING_ERR);
					} else if (e instanceof TypeMismatchException) {
						callbackContext.error(FileUtils.TYPE_MISMATCH_ERR);
					} else {
						callbackContext.error(FileUtils.UNKNOWN_ERR);
					}
				}
			}
		});
	}

	private interface FileOp {
		void run() throws Exception;
	}

}
