package t01;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author tsuji
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ExclusiveLogParser extends JFrame {

	/** エラーコードの正規表現オブジェクト */
	protected static final Pattern ptnErrCd = Pattern.compile("printExclusiveErrLog ERRCD=(.+)");
	/** 画面番号の正規表現オブジェクト */
	protected static final Pattern ptnScrNo = Pattern.compile("printExclusiveErrLog scrNo=(\\w+)");
	/** テーブル名の正規表現オブジェクト */
	protected static final Pattern ptnTbl = Pattern.compile("printExclusiveErrLog.*SQL=.*(?:FROM|INTO|UPDATE)\\s+(TM_\\w+)\\s");
	/** キーの正規表現オブジェクト */
	protected static final Pattern ptnKey = Pattern.compile("printExclusiveErrLog (C_.+)");

	protected int cntItem = 0;
	protected String lineKey = "";
	Map<String, BigDecimal> sumMap = new HashMap<String, BigDecimal>();

	Map<String, BigDecimal> sumKeyMap = new HashMap<String, BigDecimal>();


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	JTextArea ta;
	JScrollPane sp;
	Container cnt;
	/**
	 * Constructor Lister.
	 * @param f
	 */
	public ExclusiveLogParser(File f) throws Exception {
		cnt = getContentPane();
		addWindowListener(new ExWindowListener());
		setSize(300, 450);
		ta = new JTextArea();
		sp = new JScrollPane(ta);
		ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
		cnt.add(sp, BorderLayout.CENTER);
		recurse(f, 0);
		//      pack();

        printResult(sumMap);
        printResult(sumKeyMap);

		System.out.println(ta.getText());
		setVisible(true);
	}

	/**
	 *
	 */
	protected void printResult(Map<String,BigDecimal> result) {
		List<Map.Entry<String,BigDecimal>> entries =
              new ArrayList<Map.Entry<String,BigDecimal>>(result.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String,BigDecimal>>() {
        	@Override public int compare(Entry<String,BigDecimal> entry1, Entry<String,BigDecimal> entry2) {
                return (entry2.getValue()).compareTo(entry1.getValue());
            }
        });

        Collections.sort(entries, new Comparator<Map.Entry<String,BigDecimal>>() {
        	@Override public int compare(Entry<String,BigDecimal> entry1, Entry<String,BigDecimal> entry2) {
                String key1 = entry1.getKey().substring(0, entry1.getKey().indexOf("\t")>-1 ? entry1.getKey().indexOf("\t") : 0);
                String key2 = entry2.getKey().substring(0, entry2.getKey().indexOf("\t")>-1 ? entry2.getKey().indexOf("\t") : 0);
        		return (key1).compareTo(key2);
            }
        });

		for(Map.Entry<String, BigDecimal> ent : entries) {
			String key = ent.getKey();
			ta.append(key + " " + ent.getValue() + "\n");
		}
	}

	public static void main(String[] args) throws Exception {

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new ExFileFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int res = fc.showOpenDialog(null);

		String path = ".";
		//		if (args.length >= 1)
		//			path = args[0];

		//		File f = new File(path);

		File f = new File(path);
		if (res == JFileChooser.APPROVE_OPTION) {
			 f = fc.getSelectedFile();
		}
		if (!f.isDirectory()) {
			System.out.println(path + "doesn't exists or not dir");
			System.exit(0);
		}

		ExclusiveLogParser lister = new ExclusiveLogParser(f);
		lister.setVisible(true);
	}

	public void recurse(File dirfile, int depth) throws Exception {
		String contents[] = dirfile.list();
		String line = "";

		for (int i = 0; i < contents.length; i++) {
			File child = new File(dirfile, contents[i]);
			System.out.println(child.getAbsolutePath());
			cntItem = 0;
			lineKey = "";

			if (child.getPath().endsWith(".log")) {
				// リーダーの生成
	            FileInputStream is = new FileInputStream(child);
				// リーダの準備
	            InputStreamReader in = new InputStreamReader(is, "MS932");
				// バッファリーダーの生成
				BufferedReader br = new BufferedReader(in);

				while ((line = br.readLine()) != null) {

					if (line.indexOf("ERRCD") > -1 && !(cntItem == 0 || cntItem == 3)) {
						throw new Exception("ログファイルが不正");
					}

					if (cntItem == 3) {
						cntItem = 0;
						// マップにあればカウント、なければ1をput
						BigDecimal sum = sumMap.get(lineKey);
						if (sum != null) {
							sumMap.put(lineKey, sum.add(BigDecimal.valueOf(1)));
						}else{
							sumMap.put(lineKey, BigDecimal.valueOf(1));
						}
						lineKey = "";
					}
					addLine(line);
				}
				br.close();
			}
			if (child.isDirectory()) recurse(child, depth + 1);
		}

	}

	/**
	 * @param line
	 */
	private void addLine(String line) {

//		ta.append("line:" + line + "\n");
		Matcher mtch = ptnErrCd.matcher(line);
		if (mtch.find()) {
//			ta.append("errcd:" + mtch.group(1) + "\n");
			lineKey += mtch.group(1);
			cntItem++;
		}
		mtch = ptnScrNo.matcher(line);
		if (mtch.find()) {
//			ta.append("scrNo:" + mtch.group(1) + "\n");
			lineKey += "\t" + mtch.group(1);
			cntItem++;
		}
		mtch = ptnTbl.matcher(line);
		if (mtch.find()) {
//			ta.append("tbl:" + mtch.group(1) + "\n");
			lineKey += "\t" + mtch.group(1);
			cntItem++;
		}
		mtch = ptnKey.matcher(line);
		if (mtch.find()) {
//			String key = null;
//			for (Entry<String,BigDecimal> e : sumKeyMap.entrySet()) {
//				if (e.getKey().equals(mtch.group(1))) {
//					key = e.getKey();
//				}
//			}
			BigDecimal sum = sumKeyMap.get(mtch.group(1));
			if (sum != null) {
				sumKeyMap.put(mtch.group(1), sum.add(BigDecimal.valueOf(1)));
			}else{
				sumKeyMap.put(mtch.group(1), BigDecimal.valueOf(1));
			}
		}
	}
}

	class ExFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return true;
		}
		public String getDescription() {
			return "ALL";
		}
	}

	class ExWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

