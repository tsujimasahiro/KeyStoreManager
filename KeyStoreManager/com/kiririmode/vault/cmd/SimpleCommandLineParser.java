package com.kiririmode.vault.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * �R�}���h���C�������̊ȈՃp�[�T�B
 * �ȉ���O��Ƃ��āA�R�}���h���C������͂���B
 * <ul>
 *   <li> �I�v�V�������͈�x�̂ݏo������ (����̃I�v�V��������������o�����Ȃ�)
 *  </ul>
 *
 * @author kiririmode
 *
 */
public class SimpleCommandLineParser {

	List<String> args;

	/**
	 * �^����ꂽ�L�[�ƒl�̃y�A�Ń��X�g���쐬����
	 * �L�[�ɂ̓n�C�t��������
	 */
	public SimpleCommandLineParser(String ... args) {
		this.args = new ArrayList<String>(Arrays.asList(args));
	}


	/**
	 * �����ŗ^����ꂽ�L�[�Ɉ�v����L�[�ƒl�̃y�A���}�b�v�ŕԂ�
	 *
	 */
	public Map<String, String> parseOption(String... optKeys) {

		Map<String, String> optMap = new HashMap<String, String>();

		for (String optKey : optKeys) {
			for (int i = 0; i < args.size(); i++) {
				if (args.get(i).equals("-" + optKey)) {
					args.remove(i);

					String value = null;
					if (i < args.size() && ! args.get(i).startsWith("-")) {
						value = args.remove(i);
					}
					optMap.put(optKey, value);
					break;
				}
			}

		}
		return optMap;
	}

	/**
	 * �R�}���h���C�������̊ȈՃp�[�T�B
	 * �ȉ���O��Ƃ��āA�R�}���h���C������͂���B
	 *
	 * @author kiririmode
	 *
	 */
	public String[] remainingArguments() {
		return args.toArray(new String[] {});
	}
}
