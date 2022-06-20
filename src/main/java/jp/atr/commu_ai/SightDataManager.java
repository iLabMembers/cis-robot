package jp.atr.commu_ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SightDataManager {
	private static final String SIGHT_ID_KEY = "SightID";
	private static final String SIGHT_NAME_KEY = "SightName";
	private List<String> sightDataTitleList = new ArrayList<>();
	private Map<String, Map<String, String>> sightData = new HashMap<>();
	
	public SightDataManager(String filePath) {
		readSightData(filePath);
		//System.out.println(sightData);
	}
	
	/**
	 * 観光地データが格納されたCSVファイルの読み込み。
	 * @param filePath
	 */
	private void readSightData(String filePath) {
		try {
			URL url = CompetitionSample.class.getClassLoader().getResource(filePath);
			FileInputStream file = new FileInputStream(new File(url.toURI()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(file, "SJIS"));
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				if (i == 0) {
					String[] titles = line.split(",", -1);
					sightDataTitleList.addAll(Arrays.asList(titles));
				}
				else {
					Map<String, String> dataMap = new HashMap<>();
					String[] data = line.split(",", -1);
					for (int j = 0; j < data.length; j++) {
						dataMap.put(sightDataTitleList.get(j), data[j]);
					}
					sightData.put(dataMap.get(SIGHT_ID_KEY), dataMap);
				}
				i++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	public String getSightName(String sightID) {
		if(!sightData.containsKey(sightID))
			return null;
		return sightData.get(sightID).get(SIGHT_NAME_KEY);
	}
}
