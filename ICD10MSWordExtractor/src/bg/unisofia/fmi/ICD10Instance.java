package bg.unisofia.fmi;

import java.util.Arrays;

public class ICD10Instance {
	
	private static int numberOfInstances;

	private final int id;
	
	private final String code;
	private final String text;
	
	private final String diseaseName;
	private final int levelOfTxtDesc;
	
	private final boolean isReferable;
	private final String[] refTerms;
	
	private int refId;
	private boolean isReferred;
	
	
	public ICD10Instance(final String code, final String text) {
		this(code, text, null, -1, false, null);
	}
	
	/**
	 * Constructor used for NOT referable ICD10Instance.
	 * 
	 * @param code ICD10 code.
	 * @param text Text description of the disease.
	 * @param diseaseName Name of the disease.
	 * @param levelOfTxtDesc An integer value which shows the level of text description (how many dashes there are).
	 */
	public ICD10Instance(final String code, final String text, final String diseaseName, final int levelOfTxtDesc) {
		this(code, text, diseaseName, levelOfTxtDesc, false, new String[] {});
	}
	
	/**
	 * Constructor used for referable ICD10Instance.
	 * If the field <code>isReferable</code> is equal to <code>true</code> and the field <code>refLvl0</code> is blank,
	 * the constructor throws {@link IllegalArgumentException} with message:
	 * "There isn't reference name for referable ICD10Instance: ICD10Instance.refLvl0 is empty or null!".
	 * 
	 * @param code ICD10 code.
	 * @param text Text description of the disease.
	 * @param diseaseName Name of the disease.
	 * @param levelOfTxtDesc An integer value which shows the level of text description (how many dashes there are).
	 * @param isReferable A boolean showing if the ICD10Instance is referable.
	 * @param refTerms Array with references. 
	 */
	public ICD10Instance(final String code, final String text, final String diseaseName, final int levelOfTxtDesc, final String[] refTerms) {
		this(code, text, diseaseName, levelOfTxtDesc, true, refTerms);
	}
	
	private ICD10Instance(final String code, final String text, final String diseaseName, final int levelOfTxtDesc, final boolean isReferable, final String[] refTerms) {
		if (isReferable && (refTerms == null || refTerms.length == 0))
			throw new IllegalArgumentException("There isn't reference name for referable ICD10Instance: ICD10Instance.refTerms is empty or null!");
		
		this.id = ++numberOfInstances;
		
		this.code = code;
		this.text = text;
		
		this.diseaseName = diseaseName;
		this.levelOfTxtDesc = levelOfTxtDesc;
		
		this.isReferable = isReferable;
		this.refTerms = refTerms;
	}
	
	public int getId() {
		return id;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getText() {
		return text;
	}
	
	public String getDiseaseName() {
		return diseaseName;
	}
	
	public String[] getRefTerms() {
		return refTerms;
	}
	
	public boolean isReferable() {
		return isReferable;
	}
	
	public boolean isReferred() {
		return isReferred;
	}
	
	/**
	 * If ICD10Instance is not referable (<code>isReferable = false</code>) throws {@link IllegalStateException}
	 * with message: "ICD10Instance.refId is not referable!".
	 * 
	 * If the method is referable and it is called for a first time it sets a value to the field <code>refId</code> and 
	 * make the field <code>isReferred</code> equals to <code>true</code>.
	 * Else throw an {@link IllegalStateException} with message "ICD10Instance.refId is already set!".
	 * 
	 * @param refId An integer containing the ICD10Instance’s reference id.
	 */
	public void setRefId(int refId) {
		if (!isReferable) throw new IllegalStateException("ICD10Instance.refId is not referable!");
		if (isReferred) throw new IllegalStateException("ICD10Instance.refId is already set!");
		
		this.refId = refId;
		this.isReferred = true;
	}
	
	@Override
	public String toString() {
		return String.format("%d,%s,\"%s\",%s,%d,\"%s\",%d", id, code, text, diseaseName, levelOfTxtDesc, Arrays.toString(refTerms), refId);
	}
	
	public static String toStringHeaders() {
		return "id,code,text,diseaseName,levelOfTxtDesc,refTerms,refId";
	}

}
