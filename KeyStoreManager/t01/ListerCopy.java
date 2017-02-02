package t01;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

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
public class ListerCopy extends JFrame {
	JTextArea ta;
	JScrollPane sp;
	Container cnt;
	Set<String> filePathSet = new HashSet<String>();

	/**
	 * Constructor Lister.
	 * @param f
	 * @throws IOException
	 */
	public ListerCopy(File f) throws IOException {
		cnt = getContentPane();
		addWindowListener(new SmplWindowListener());
		setSize(300, 450);
		ta = new JTextArea();
		sp = new JScrollPane(ta);
		ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
		cnt.add(sp, BorderLayout.CENTER);
		filePathSet.add("2014年9月度勤務報告書【辻雅啓】_PRAS.xls");
		filePathSet.add("2014年8月度勤務報告書【辻雅啓】_PRAS.xls");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		filePathSet.add("");
		System.out.println(filePathSet.contains("2014年8月度勤務報告書【辻雅啓】_PRAS.xls"));
		System.out.println(ta.getText());
		recurse(f, 0);
		pack();
		setVisible(true);
	}

	public static void main(String[] args) throws IOException {

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new MFileFilter());
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

		ListerCopy lister = new ListerCopy(f);
		lister.setVisible(true);
	}

	public void recurse(File dirfile, int depth) throws IOException {
		String contents[] = dirfile.list();
		String dirPath = dirfile.getPath();
		for (String content : contents) {
 			System.out.println(content);
			if (filePathSet.contains(content)) {
				String srcPath = dirPath+"\\"+content;
				String dstPath = content.replace(".xls", "-廃棄.xls");
				dstPath = "C:\\tmp\\"+dstPath;
				copyTransfer(srcPath, dstPath);
				ta.append(dstPath);
 			}
			File child = new File(dirfile, content);
			if (child.isDirectory()) recurse(child, depth + 1);
		}
	}

	/**
	 * コピー元のパス[srcPath]から、コピー先のパス[destPath]へ
	 * ファイルのコピーを行います。
	 * コピー処理にはFileChannel#transferToメソッドを利用します。
	 * 尚、コピー処理終了後、入力・出力のチャネルをクローズします。
	 * @param srcPath    コピー元のパス
	 * @param destPath    コピー先のパス
	 * @throws IOException    何らかの入出力処理例外が発生した場合
	 */
	public static void copyTransfer(String srcPath, String destPath)
	    throws IOException {

	    FileChannel srcChannel = new
	        FileInputStream(srcPath).getChannel();
	    FileChannel destChannel = new
	        FileOutputStream(destPath).getChannel();
	    try {
	        srcChannel.transferTo(0, srcChannel.size(), destChannel);
	    } finally {
	        srcChannel.close();
	        destChannel.close();
	    }
	}

}
	class MFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

//			String fn = f.getName();
//			if (fn.toLowerCase().endsWith(".bin")) {
//				return true;
//			}
			return true;
		}
		public String getDescription() {
			return "ALL";
		}
	}

	class SmplWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

