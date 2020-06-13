package bg.unisofia.fmi;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class ICD10WordReader {
	
	private static final String MS_WORD_PART_1 = ".\\data\\index_a_m.docx";
	private static final String MS_WORD_PART_2 = ".\\data\\index_n_z.docx";
	private static final String CSV_FILE_NAME = ".\\dataset\\ICD10-MSWord.csv";
	
	
	private ICD10WordReader() {
	}
	
	public static Map<String, List<ICD10Instance>> read(final String... fileNames) throws IOException {
		Map<String, List<ICD10Instance>> icd10Map = new HashMap<String, List<ICD10Instance>>();
		
		String letterKey = null;
		List<ICD10Instance> icd10List = null;
		
		int level = 0;
		String[] levelTexts = new String[7];
		
		XWPFDocument document = null;
		try {
			for (final String fileName : fileNames) {
				Path msWordPath = Paths.get(fileName);
				document = new XWPFDocument(Files.newInputStream(msWordPath));
				List<XWPFParagraph> paragraphs = document.getParagraphs();
				for (int i = 0; i < paragraphs.size(); i++) {
					String text = paragraphs.get(i).getText().strip();
					if (text.length() == 0) {
						continue;
					} else if (text.length() == 1 && Character.isUpperCase(text.charAt(0))) {
						if (letterKey != null) {
							icd10Map.put(letterKey, icd10List);
						}
						letterKey = text;
						icd10List = new ArrayList<ICD10Instance>();
					} else {
						String nextText = i + 1 < paragraphs.size() ? paragraphs.get(i+1).getText().strip() : "";
						level = ICD10Utils.textProcessing(levelTexts, level, text, nextText, letterKey, icd10List);
					}
				}
				document.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (document != null) {
		        try {
		            document.close();
		        } catch (IOException ex) {}
		    }
		}
		
		return icd10Map;
	}
	
	public static void setReferences(Map<String, List<ICD10Instance>> map) {
		for (Map.Entry<String,List<ICD10Instance>> entry : map.entrySet()) {
			List<ICD10Instance> icd10List = entry.getValue();
			for (ICD10Instance icd10Instance : icd10List) {
				if (icd10Instance.isReferable()) {
					String[] refTerms = icd10Instance.getRefTerms();
					if (refTerms.length != 1 || !refTerms[0].equals("състояние")) {
						boolean onlyCodeRefs = ICD10Utils.isTextContainingOnlyCodes(refTerms[0]);
						if (!onlyCodeRefs) {
							String refKey = refTerms[0].substring(0, 1);
							if (refKey.equals(refKey.toUpperCase())) {
								List<ICD10Instance> refInstances = map.get(refKey);
								for (ICD10Instance refInstance : refInstances) {
									if (refInstance.getDiseaseName().equals(refTerms[0])) {
										for (int i = 1; i < refTerms.length; i++) {
											if (!refInstance.getText().contains(refTerms[i])) {
												break;
											} else if (i == refTerms.length - 1) {
												icd10Instance.setRefId(refInstance.getId());
											}
										}
									}
									if (icd10Instance.isReferred()) break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void save(final Map<String, List<ICD10Instance>> map, final String fileName) throws IOException {
		final String NEW_LINE = System.lineSeparator();
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
			writer.append(ICD10Instance.toStringHeaders()).append(NEW_LINE);
			for (Map.Entry<String, List<ICD10Instance>> entry : map.entrySet()) {  
				List<ICD10Instance> instances = entry.getValue();
				for (ICD10Instance icd10Instance : instances) {
					writer.append(icd10Instance.toString()).append(NEW_LINE);
				}
			}
			writer.flush();
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (writer != null) {
		        try {
		            writer.close();
		        } catch (IOException ex) {}
		    }
		}
	}

	public static void main(String[] args) {
		try {
			System.out.println("Running ICD10WordReader:");
			Instant start = Instant.now();
			
			Map<String, List<ICD10Instance>> map = ICD10WordReader.read(MS_WORD_PART_1, MS_WORD_PART_2);
			setReferences(map);
			save(map, CSV_FILE_NAME);
			
			System.out.println(Duration.between(start, Instant.now()).toSeconds() + " seconds.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
