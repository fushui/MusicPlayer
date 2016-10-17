package feiyu.com.musicplayer_util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import feiyu.com.musicplayer.PlayActivity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class LrcProcess {
	private List<LrcContent> lrcList;
	private LrcContent mLrcContent;
	private Context mContext;

	public LrcProcess() {
		mLrcContent = new LrcContent();
		lrcList = new ArrayList<LrcContent>();
	}

	public LrcProcess(Context context) {
		this.mContext = context;
		mLrcContent = new LrcContent();
		lrcList = new ArrayList<LrcContent>();
	}

	/**
	 * 读取歌词
	 * 
	 * @param path
	 * @return
	 */
	public String readLRC(String path) {
		// 定义一个StringBuilder对象，用来存放歌词内容
		StringBuilder stringBuilder = new StringBuilder();
		System.out.println(path);
		// path = path.replace("song","lyric");

		// File f = new File(path.replace(".m4a", ".trc"));
		File f = new File(path);
		String name1 = f.getName();
		name1 = name1.replace(".", "@");
		// Toast.makeText(mContext, name1, Toast.LENGTH_LONG).show();

		String name;
		String tail;
		String Data[] = name1.split("@");
		if (Data.length > 1) {
			name = Data[0];
			tail = "." + Data[1];
			String parentDir = f.getAbsoluteFile().getParent();
			File trcFiles;
			trcFiles = new File(parentDir + "//" + name + ".trc");
			File lrcFiles;
			lrcFiles = new File(parentDir + "//" + name + ".lrc");
			if (trcFiles.exists()) {
				try {
					f = new File(path.replace(tail, ".trc"));
					// 创建一个文件输入流对象
					FileInputStream fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis, "utf-8");
					BufferedReader br = new BufferedReader(isr);
					String s = "";
					while ((s = br.readLine()) != null) {
						// 替换字符

						s = s.replace("[", "");
						s = s.replace("]", "@");
						 s = s.replaceAll("<[0-9]{3,5}>", "");
						// 分离“@”字符
						String splitLrcData[] = s.split("@");
						if (splitLrcData.length > 1) {
							mLrcContent.setLrcStr(splitLrcData[1]);

							// 处理歌词取得歌曲的时间
							int lrcTime = time2Str(splitLrcData[0]);

							mLrcContent.setLrcTime(lrcTime);

							// 添加进列表数组
							lrcList.add(mLrcContent);

							// 新创建歌词内容对象
							mLrcContent = new LrcContent();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					stringBuilder.append("木有歌词文件，赶紧去下载！...");
					return stringBuilder.toString();
				} catch (IOException e) {
					e.printStackTrace();
					stringBuilder.append("木有读取到歌词哦！");
					return stringBuilder.toString();
				}
				 
			} else if (lrcFiles.exists()) {
				try {
					f = new File(path.replace(tail, ".lrc"));
					// 创建一个文件输入流对象
					FileInputStream fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis, "utf-8");
					BufferedReader br = new BufferedReader(isr);
					String s = "";
					while ((s = br.readLine()) != null) {
						// 替换字符

						s = s.replace("[", "");
						s = s.replace("]", "@");
						// s = s.replaceAll("<[0-9]{3,5}>", "");
						// 分离“@”字符
						String splitLrcData[] = s.split("@");
						if (splitLrcData.length > 1) {
							mLrcContent.setLrcStr(splitLrcData[1]);

							// 处理歌词取得歌曲的时间
							int lrcTime = time2Str(splitLrcData[0]);

							mLrcContent.setLrcTime(lrcTime);

							// 添加进列表数组
							lrcList.add(mLrcContent);

							// 新创建歌词内容对象
							mLrcContent = new LrcContent();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					stringBuilder.append("木有歌词文件，赶紧去下载！...");
					return stringBuilder.toString();
				} catch (IOException e) {
					e.printStackTrace();
					stringBuilder.append("木有读取到歌词哦！");
					return stringBuilder.toString();
				}
			}
		}

		stringBuilder.append("木有读取到歌词哦！");
		return stringBuilder.toString();

	}

	public int time2Str(String timeStr) {
		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@"); // 将时间分隔成字符串数组

		// 分离出分、秒并转换为整型
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// 计算上一行与下一行的时间转换为毫秒数
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
		return currentTime;
	}

	public List<LrcContent> getLrcList() {
		return lrcList;
	}
}
