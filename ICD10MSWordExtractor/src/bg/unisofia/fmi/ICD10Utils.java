package bg.unisofia.fmi;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICD10Utils {

	private static final String MATCH_PATTERN_SEQUEL = "^.*—продължение$";
	
	private static final String MATCH_PATTERN_LEVEL_1 = "^–(?! –).*$";
	private static final String MATCH_PATTERN_LEVEL_2 = "^– –(?! –).*$";
	private static final String MATCH_PATTERN_LEVEL_3 = "^– – –(?! –).*$";
	private static final String MATCH_PATTERN_LEVEL_4 = "^– – – –(?! –).*$";
	private static final String MATCH_PATTERN_LEVEL_5 = "^– – – – –(?! –).*$";
	private static final String MATCH_PATTERN_LEVEL_6 = "^– – – – – –(?! –).*$";
	private static final String MATCH_PATTERN_LEVELS = "^(– – – – – – |– – – – – |– – – – |– – – |– – |– )";
	
	private static final String MATCH_PATTERN_CODES_TYPE_1 = ".*([A-ZА-Я][0-9]{2}\\.[0-9]†\\s[A-ZА-Я][0-9]{2}\\.[0-9]\\*)\\s*$";
	private static final String MATCH_PATTERN_CODES_TYPE_2 = ".*(кодира се в [A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]|кодира се в [A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9])\\s*$";
	private static final String MATCH_PATTERN_CODES_TYPE_2_GROUP = ".*([A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2}).*$";
	private static final String MATCH_PATTERN_CODES_TYPE_2_4SIGN = ".*\\s(\\.[0-9])\\s*$";
	private static final String MATCH_PATTERN_CODES_TYPE_3 = ".*([A-ZА-Я][0-9]{2}\\.-|[A-ZА-Я][0-9]{2}\\.–)\\s*$";
	private static final String MATCH_PATTERN_CODES_TYPE_4 = ".*([A-ZА-Я][0-9]{2}\\.[0-9])\\s*$";
	private static final String MATCH_PATTERN_CODES_TYPE_5 = ".*([A-ZА-Я][0-9]{2})\\s*$";
	
	private static final String MATCH_PATTERN_ONLY_CODES = "^\\s*([A-ZА-Я][0-9]{2}\\.[0-9]†\\s[A-ZА-Я][0-9]{2}\\.[0-9]\\*|[A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}\\.-|[A-ZА-Я][0-9]{2}\\.–|[A-ZА-Я][0-9]{2}\\.[0-9]|[A-ZА-Я][0-9]{2})\\s*$";
	private static final String MATCH_PATTERN_STARTS_WITH_CODES = "^\\s*([A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}\\.[0-9]|[A-ZА-Я][0-9]{2}\\.-|[A-ZА-Я][0-9]{2}\\.–|[A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}\\.[0-9]†\\s[A-ZА-Я][0-9]{2}\\.[0-9]\\*|кодира се в [A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]|кодира се в [A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]).*$";
	private static final String MATCH_PATTERN_ENDS_WITH_CODES = "^.*([A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}\\.[0-9]|[A-ZА-Я][0-9]{2}\\.-|[A-ZА-Я][0-9]{2}\\.–|[A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2}|[A-ZА-Я][0-9]{2}\\.[0-9]†\\s[A-ZА-Я][0-9]{2}\\.[0-9]\\*|кодира се в [A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]|кодира се в [A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9])\\s*$";
	
	private static final String MATCH_PATTERN_REF_TYPE_CODES_TYPE_2 = ".*(виж [A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]|виж [A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]).*";
	private static final String MATCH_PATTERN_REF_TYPE_CODES_TYPE_2_REPLACE = "виж [A-ZА-Я][0-9]{2}-[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]|виж [A-ZА-Я][0-9]{2}–[A-ZА-Я][0-9]{2} с четвърти знак \\.[0-9]";
	private static final String MATCH_PATTERN_REF_TYPE_IN_BRACKETS = "\\(виж (.*)\\)";
	private static final String MATCH_PATTERN_REF_TYPE_NO_BRACKETS = "[^(]виж (.*)$";
	
	
	private ICD10Utils() {
	}
	
	/*
	 * ===================================================
	 * === Text processing and creating ICD10Instances ===
	 * ===================================================
	 */
	
	public static int textProcessing(String[] levelTexts, int level, String text, String nextText, String letterKey, List<ICD10Instance> icd10List) {
		if (ICD10Utils.isSequel(text)) return level;
		
		text = ICD10Utils.clearTextBeforeProcessing(text);
		String lastText = levelTexts[level];
		int lastLevel = level;
		
		if (ICD10Utils.isTextNewDisease(text, lastText, letterKey)) {
			level = 0;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel1(text)) {
			level = 1;
			levelTexts[1] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel2(text)) {
			level = 2;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel3(text)) {
			level = 3;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel4(text)) {
			level = 4;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel5(text)) {
			level = 5;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else if (ICD10Utils.isLevel6(text)) {
			level = 6;
			levelTexts[level] = text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		} else {
			levelTexts[level] += " " + text;
			ICD10Utils.textProcessing(levelTexts, level, lastLevel, nextText, icd10List);
		}
		return level;
	}
	
	private static boolean isTextNewDisease(String text, String lastText, String letterKey) {
		boolean startsWithLettersKey =  text.startsWith(letterKey);
		boolean notReferred = lastText == null ? true : !lastText.matches(".*виж\\s*$");
		boolean notStartingWithCode = !isTextStartingWithCodes(text);
		return startsWithLettersKey && notReferred && notStartingWithCode;
	}
	
	private static void textProcessing(String[] levelTexts, int level, int lastLevel, String nextText, List<ICD10Instance> icd10List) {
		if (level != lastLevel && isReferable(levelTexts[lastLevel])) {
			levelTexts[lastLevel] = removeRefTerms(levelTexts[lastLevel]);
		}
		
		levelTexts[level] = ICD10Utils.removeLevel(levelTexts[level]);
		levelTexts[level] = connectSplittedWords(levelTexts[level]);
		if (ICD10Utils.isTextReadyForCreatingInstance(levelTexts[level], nextText)) {
			createICD10Instances(levelTexts, level, icd10List);
		}
	}
	
	private static boolean isTextReadyForCreatingInstance(String text, String nextText) {
		return isTextEndingWithCodes(text) && !isTextStartingWithCodes(nextText);
	}
	
	private static void createICD10Instances(String[] levelTexts, int level, List<ICD10Instance> icd10List) {
		int codesType = getCodesType(levelTexts[level]);
		String[] codes = getCodes(levelTexts[level], codesType);
		levelTexts[level] = removeCodes(levelTexts[level], codesType);
		
		String text = ICD10Utils.concatenateLevelTexts(levelTexts, level);
		String diseaseName = getDiseaseName(text);
		
		boolean isReferable = isReferable(text);
		if (isReferable) {
			String[] refTerms = getRefTerms(text);
			for (int i = 0; i < codes.length; i++) {
				icd10List.add(new ICD10Instance(codes[i], clearText(text), diseaseName, level, refTerms));
			}
		} else {
			for (int i = 0; i < codes.length; i++) {
				icd10List.add(new ICD10Instance(codes[i], clearText(text), diseaseName, level));
			}
		}
	}
	
	private static String concatenateLevelTexts(String[] levelTexts, int level) {
		String text = levelTexts[0];
		for (int i = 1; i <= level; i++) {
			text += " " + levelTexts[i];
		}
		return text;
	}
	
	private static String getDiseaseName(String text) {
		Pattern pattern = Pattern.compile("^([A-Za-zА-Яа-я]+).*");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new RuntimeException("Unexpected error: there isn't a disease name - " + text);
		}
	}
	
	private static boolean isReferable(String text) {
		boolean hasSeeRef = text.contains(" виж ") || text.contains("(виж ");
		boolean hasSeeStateRef = text.contains(" виж състояние ") || text.contains("(виж състояние ");
		boolean hasSeeRubricRef = text.contains(" виж рубриките ") || text.contains("(виж рубриките ");
		return hasSeeRef && !hasSeeStateRef && !hasSeeRubricRef;
	}
	
	private static String[] getRefTerms(String text) {
		Pattern pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_CODES_TYPE_2);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) return getCodesByGroupAnd4Sign(matcher.group(1).strip());
		
		pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_IN_BRACKETS);
		matcher = pattern.matcher(text);
		if (matcher.find()) return getRefTermsByMatcherGroup(matcher.group(1).strip());
		
		pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_NO_BRACKETS);
		matcher = pattern.matcher(text);
		if (matcher.find()) return getRefTermsByMatcherGroup(matcher.group(1).strip());
		
		throw new RuntimeException("Unexpected error: there aren't references - " + text);
	}
	
	private static String[] getRefTermsByMatcherGroup(String matcherGroup) {
		return matcherGroup.split(", |,| ");
	}
	
	private static String removeRefTerms(String text) {
		Pattern pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_CODES_TYPE_2);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) return clearText(text.replaceFirst(MATCH_PATTERN_REF_TYPE_CODES_TYPE_2_REPLACE, ""));
		
		pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_IN_BRACKETS);
		matcher = pattern.matcher(text);
		if (matcher.find()) return clearText(text.replaceFirst(MATCH_PATTERN_REF_TYPE_IN_BRACKETS, ""));
		
		pattern = Pattern.compile(MATCH_PATTERN_REF_TYPE_NO_BRACKETS);
		matcher = pattern.matcher(text);
		if (matcher.find()) return clearText(text.replaceFirst(MATCH_PATTERN_REF_TYPE_NO_BRACKETS, ""));
		
		throw new RuntimeException("Unexpected error: there aren't references - " + text);
	}
	
	/* 
	 * =========================
	 * ===== Text Cleaning =====
	 * =========================
	 */
	
	private static boolean isSequel(String text) {
		return text.matches(MATCH_PATTERN_SEQUEL);
	}
	
	private static String clearTextBeforeProcessing(String text) {
		text = text.replaceAll("([“”\"\"‘’''])", "");
		text = text.replaceAll("НКД", "");
		text = text.replaceAll("виж също", "виж");
		text = text.replaceAll("(\\(-\\s?[A-Za-zА-Яа-я]{0,3}\\))", "");
		text = text.replaceAll("(\\(със\\))", "");
		text = text.replaceAll("(\\(-)", "(");
		text = connectSplittedWords(text);
		return text.strip();
	}
	
	private static String clearText(String text) {
		text = connectSplittedWords(text);
		text = text.replaceAll("\\(|\\)", " ");
		text = text.replaceAll("\\s-\\s|\\s–\\s", " ");
		text = text.replaceAll("\\.\\s|,\\s|;\\s", " ");
		text = text.replaceAll("\\s{2,}", " ");
		return text;
	}
	
	private static String connectSplittedWords(String text) {
		Pattern pattern = Pattern.compile("([А-Яа-яA-Za-z]{2,}-\\s\\s?[А-Яа-яA-Za-z]{2,})");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			String match = matcher.group(1);
			return text.replace(match, match.replaceAll("(-\\s\\s?)", ""));
		} else {
			return text;
		}
	}
	
	/*
	 * =========================
	 * === Levels Processing ===
	 * =========================
	 */
	
	private static boolean isLevel1(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_1);
	}
	
	private static boolean isLevel2(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_2);
	}
	
	private static boolean isLevel3(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_3);
	}
	
	private static boolean isLevel4(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_4);
	}
	
	private static boolean isLevel5(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_5);
	}
	
	private static boolean isLevel6(String text) {
		return text.matches(MATCH_PATTERN_LEVEL_6);
	}
	
	private static String removeLevel(String text) {
		return text.replaceAll(MATCH_PATTERN_LEVELS, "");
	}
	
	/*
	 * ========================
	 * === Codes Processing ===
	 * ========================
	 */
	
	public static boolean isTextContainingOnlyCodes(String text) {
		return text.matches(MATCH_PATTERN_ONLY_CODES);
	}
	
	private static boolean isTextStartingWithCodes(String text) {
		return text.matches(MATCH_PATTERN_STARTS_WITH_CODES);
	}
	
	private static boolean isTextEndingWithCodes(String text) {
		return text.matches(MATCH_PATTERN_ENDS_WITH_CODES);
	}
	
	/**
	 * The returned value represents the type of the code.
	 * If the value is 0 then there isn't a code in the text. 
	 * @param text
	 * @return
	 */
	private static int getCodesType(String text) {
		if (text.matches(MATCH_PATTERN_CODES_TYPE_1)) {
			return 1;
		} else if (text.matches(MATCH_PATTERN_CODES_TYPE_2)) {
			return 2;
		} else if (text.matches(MATCH_PATTERN_CODES_TYPE_3)) {
			return 3;
		} else if (text.matches(MATCH_PATTERN_CODES_TYPE_4)) {
			return 4;
		} else if (text.matches(MATCH_PATTERN_CODES_TYPE_5)) {
			return 5;
		} else {
			return 0;
		}
	}
	
	private static String[] getCodes(final String text, final int codesType) {
		switch (codesType) {
	    	case 1: return getCodesOfType1(text);
	    	case 2: return getCodesOfType2(text);
	    	case 3: return getCodesOfType3(text);
	    	case 4: return getCodesOfType4(text);
	    	case 5: return getCodesOfType5(text);
	    	default: throw new IllegalArgumentException(String.format("Invalid 'codesType': %d!", codesType));
		}
	}
	
	private static String[] getCodesOfType1(String text) {
		String codesAsPattern = getCodesAsPattern(text, MATCH_PATTERN_CODES_TYPE_1);
		codesAsPattern = codesAsPattern.replaceAll("†|\\*", "");
		return codesAsPattern.split(" ");
	}
	
	private static String[] getCodesOfType2(String text) {
		String codesAsPattern = getCodesAsPattern(text, MATCH_PATTERN_CODES_TYPE_2);
		return getCodesByGroupAnd4Sign(codesAsPattern);
	}
	
	private static String[] getCodesOfType3(String text) {
		String codesAsPattern = getCodesAsPattern(text, MATCH_PATTERN_CODES_TYPE_3);
		String patternToReplace = codesAsPattern.contains("-") ? "-" : "–";
		String[] codes = new String[10];
		for (int i = 0; i < codes.length; i++) {
			codes[i] = codesAsPattern.replace(patternToReplace, String.valueOf(i));
		}
		return codes;
	}
	
	private static String[] getCodesOfType4(String text) {
		String codesAsPattern = getCodesAsPattern(text, MATCH_PATTERN_CODES_TYPE_4);
		return new String[] {codesAsPattern};
	}
	
	private static String[] getCodesOfType5(String text) {
		String codesAsPattern = getCodesAsPattern(text, MATCH_PATTERN_CODES_TYPE_5);
		return new String[] {codesAsPattern};
	}
	
	private static String[] getCodesByGroupAnd4Sign(String codesAsPattern) {
		String codesGroup = getCodesAsPattern(codesAsPattern, MATCH_PATTERN_CODES_TYPE_2_GROUP);
		String[] codesRange = codesGroup.contains("-") ? codesGroup.split("-") : codesGroup.split("–");
		
		String[] firstCodeSplitted = codesRange[0].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		String[] lastCodeSplitted = codesRange[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		
		String codes4Sign = getCodesAsPattern(codesAsPattern, MATCH_PATTERN_CODES_TYPE_2_4SIGN);
		
		int first = Integer.parseInt(firstCodeSplitted[1]);
		int last = Integer.parseInt(lastCodeSplitted[1]);
		
		String[] codes = new String[last - first + 1];
		for (int i = 0; i <= last - first; i++) {
			codes[i] = String.format("%s%02d%s", firstCodeSplitted[0], i + first, codes4Sign);
		}
		return codes;
	}
	
	private static String getCodesAsPattern(String text, final String codesPattern) {
		Pattern pattern = Pattern.compile(codesPattern);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1).strip();
		} else {
			throw new IllegalArgumentException(String.format("The text \"%s\" has no pattern \"%s\"!", text, codesPattern));
		}
	}
	
	private static String removeCodes(final String text, final int codesType) {
		switch (codesType) {
	    	case 1: return removeCodesByTypePattern(text, MATCH_PATTERN_CODES_TYPE_1);
	    	case 2: return removeCodesByTypePattern(text, MATCH_PATTERN_CODES_TYPE_2);
	    	case 3: return removeCodesByTypePattern(text, MATCH_PATTERN_CODES_TYPE_3);
	    	case 4: return removeCodesByTypePattern(text, MATCH_PATTERN_CODES_TYPE_4);
	    	case 5: return removeCodesByTypePattern(text, MATCH_PATTERN_CODES_TYPE_5);
	    	default: throw new IllegalArgumentException(String.format("Invalid 'codesType': %d!", codesType));
		}
	}
	
	private static String removeCodesByTypePattern(String text, String codesPattern) {
		codesPattern = codesPattern.startsWith(".*") ? codesPattern.replaceFirst("\\.\\*", "") : codesPattern;
		return text.replaceAll(codesPattern, "").strip();
	}
	
}
